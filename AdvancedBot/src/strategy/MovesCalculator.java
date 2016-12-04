package strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.*;

import map.Region;
import map.SuperRegion;
import move.AttackTransferMove;
import move.Moves;
import move.MovesCommitter;
import move.PlaceArmiesMove;
import basicAlgorithms.DistanceCalculator;
import bot.HistoryTracker;
import evaluation.RegionValueCalculator;
import evaluation.SuperRegionValueCalculator;
import evaluation.WorkingMapUpdater;

public class MovesCalculator {

	private static Moves calculatedMoves = new Moves();

	public static Moves getCalculatedMoves() {
		return calculatedMoves;
	}

	/**
	 * Calculates the moves. At present we use 3 armies each turn for expansion
	 * while we try to expand in a fast SuperRegion without opponent presence.
	 * The remaining armies are put against the opponent.
	 */
	public static void calculateMoves() {
		calculatedMoves = new Moves();
		Moves movesSoFar = new Moves();
		calculateExpansionMoves(movesSoFar);
		calculateFightMoves(movesSoFar);

		String myName = HistoryTracker.botState.getMyPlayerName();
		int armies = 1;
		List<Region> regionsToDeploy = new ArrayList<>();
		regionsToDeploy = getSortedDeploymentRegions();
		while (HistoryTracker.botState.getStartingArmies() > movesSoFar.getTotalDeployment()) {
            for (Region region : regionsToDeploy) {
                if (region.deploymentRegionValue > Math.random()) {
                    PlaceArmiesMove pam = new PlaceArmiesMove(myName, region, 1);
                    movesSoFar.addPlaceArmiesMove(pam);
                    MovesCommitter.committPlaceArmiesMove(pam);
                }
            }
		}

		// AttackTransferMoves
		for (Region fromRegion : HistoryTracker.botState.getVisibleMap().getRegions()) {
			if (fromRegion.ownedByPlayer(myName)) {
				List<Region> possibleToRegions = new ArrayList<Region>();
				for (Region nonOwnedNeighbor : fromRegion.getNonOwnedNeighbors()) {
					if (nonOwnedNeighbor.getPlayerName().equals("neutral")) {
						possibleToRegions.add(nonOwnedNeighbor);
					} else if (nonOwnedNeighbor.getArmiesAfterDeployment() * 0.7 <= nonOwnedNeighbor
							.getSurroundingIdleArmies() * 0.6) {
						possibleToRegions.add(nonOwnedNeighbor);
					}
				}
				while (!possibleToRegions.isEmpty()) {
					double rand = Math.random();
					int r = (int) (rand * possibleToRegions.size());
					Region toRegion = possibleToRegions.get(r);

					if (!toRegion.getPlayerName().equals(myName) && fromRegion.getIdleArmies() > 2) {
						AttackTransferMove atm = new AttackTransferMove(myName, fromRegion, toRegion,
								fromRegion.getIdleArmies());
						movesSoFar.attackTransferMoves.add(atm);
						MovesCommitter.committAttackTransferMove(atm);
						break;
					} else
						possibleToRegions.remove(toRegion);
				}
			}
		}

		WorkingMapUpdater.updateWorkingMap();
		DistanceCalculator.calculateDistanceToBorder();
		Moves transferMoves = TransferMovesChooser.calculateTransferMoves();
		MovesCommitter.committMoves(transferMoves);
		movesSoFar.mergeMoves(transferMoves);
		movesSoFar = MovesScheduler.scheduleMoves(movesSoFar);
		calculatedMoves = movesSoFar;
	}

	private static void calculateFightMoves(Moves moves) {
		int armiesForFight = HistoryTracker.botState.getStartingArmies() - moves.getTotalDeployment();
		List<Region> sortedDefenceValueRegions = RegionValueCalculator.getSortedDefenceValueRegions();
		if (sortedDefenceValueRegions.size() == 0) {
			return;
		}
		Region highestPriorityDefenceRegion = sortedDefenceValueRegions.get(0);
		PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(),
				highestPriorityDefenceRegion, armiesForFight);
		MovesCommitter.committPlaceArmiesMove(pam);
		moves.addPlaceArmiesMove(pam);

		for (Region opponentBorderingRegion : HistoryTracker.botState.getVisibleMap().getOpponentBorderingRegions()) {
			Region bestAttackRegion = RegionValueCalculator.getSortedAttackRegions(opponentBorderingRegion).get(0);
			if (opponentBorderingRegion.getIdleArmies() > 0
					&& bestAttackRegion.getArmiesAfterDeployment() * 0.7 <= bestAttackRegion.getSurroundingIdleArmies() * 0.6) {
				AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
						opponentBorderingRegion, bestAttackRegion, opponentBorderingRegion.getIdleArmies());
				MovesCommitter.committAttackTransferMove(atm);
				moves.attackTransferMoves.add(atm);
			} else if (bestAttackRegion.getArmies() > 1 && opponentBorderingRegion.getIdleArmies() > 0) {
				AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
						opponentBorderingRegion, bestAttackRegion, 1);
				MovesCommitter.committAttackTransferMove(atm);
				moves.attackTransferMoves.add(atm);
			}
			// Block the region so we don't move from there
			opponentBorderingRegion.setRegionBlocked(true);
		}
	}

	private static void calculateExpansionMoves(Moves moves) {
		int armiesForExpansion = 3;
		if (HistoryTracker.botState.getVisibleMap().getOpponentBorderingRegions().size() == 0) {
			armiesForExpansion = HistoryTracker.botState.getStartingArmies();
		}
		List<SuperRegion> sortedAccessibleSuperRegions = SuperRegionValueCalculator.sortAccessibleSuperRegions();
		SuperRegion bestSuperRegion = sortedAccessibleSuperRegions.get(0);
		Region regionToExpand = null;
		Region regionToExpandFrom = null;
		for (Region subRegion : bestSuperRegion.getSubRegions()) {
			if (subRegion.getPlayerName().equals("neutral") && subRegion.getOwnedNeighbors().size() > 0) {
				regionToExpand = subRegion;
				regionToExpandFrom = regionToExpand.getOwnedNeighbors().get(0);
			}
		}
		if (regionToExpand != null) {
			PlaceArmiesMove pam = new PlaceArmiesMove(HistoryTracker.botState.getMyPlayerName(), regionToExpandFrom,
					armiesForExpansion);
			moves.addPlaceArmiesMove(pam);
			MovesCommitter.committPlaceArmiesMove(pam);
			int armiesToMove = regionToExpandFrom.getIdleArmies();
			if (regionToExpandFrom.getOpponentNeighbors().size() > 0) {
				armiesToMove = armiesForExpansion;
			}
			AttackTransferMove atm = new AttackTransferMove(HistoryTracker.botState.getMyPlayerName(),
					regionToExpandFrom, regionToExpand, armiesToMove);
			moves.attackTransferMoves.add(atm);
			MovesCommitter.committAttackTransferMove(atm);
		}
	}

	private static List<Region> getSortedDeploymentRegions() {
        double maxValue = -999;
        for (Region region : HistoryTracker.botState.getVisibleMap().getOwnedRegions()) {
            double bordering_armies = region.getSurroundingOpponentArmies();
            double my_armies = region.getArmies();
            double others_in_super_region = region.getAmountOfArmiesToOwnSuperRegion();

            region.deploymentRegionValue = 
                    0.0
                     + bordering_armies * 1.0
                     + my_armies * -1.0
                     + (1.0 / others_in_super_region) * 5.0;
            if (region.deploymentRegionValue > maxValue) {
                maxValue = region.deploymentRegionValue;
            }
        }

        List<Region> regions = HistoryTracker.botState.getVisibleMap().getOwnedRegions();
        Collections.sort(regions, new Comparator<Region>() {
            @Override
            public int compare(Region me, Region that) {
                return roundAwayFromZero(me.deploymentRegionValue - that.deploymentRegionValue);
            }
        });

        //normalize
        for (Region region : regions) {
            region.deploymentRegionValue /= maxValue;
        }

        return regions;
	}

    private static int roundAwayFromZero(double in) {
        if (in > 0) {
            return (int) Math.ceil(in);
        }
        else {
            return (int) Math.floor(in);
        }
    }
}
