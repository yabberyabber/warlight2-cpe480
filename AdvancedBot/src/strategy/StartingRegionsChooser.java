package strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import map.Region;
import map.SuperRegion;
import bot.BotState;

public class StartingRegionsChooser {

	public static List<Integer> pickedStartingRegions = new ArrayList<>();
	public static List<Integer> pickableStartingRegionIDs = new ArrayList<>();

	public static Region getStartingRegion(BotState state) {
		List<SuperRegionInformation> superRegionInformations = getSuperRegionInformations(state);
		List<SuperRegionInformation> sortedSuperRegions = sortSuperRegions(superRegionInformations);
		List<Region> pickableStartingRegions = state.getPickableStartingRegions();
		Region bestRegion = getBestRegion(sortedSuperRegions, pickableStartingRegions);
		pickedStartingRegions.add(bestRegion.getId());
		if (pickableStartingRegionIDs.size() == 0) {
			for (Region region : pickableStartingRegions) {
				pickableStartingRegionIDs.add(region.getId());
			}
		}
		return bestRegion;
	}

	private static Region getBestRegion(List<SuperRegionInformation> sortedSuperRegions,
			List<Region> pickableStartingRegions) {
		for (SuperRegionInformation sri : sortedSuperRegions) {
			for (Region region : pickableStartingRegions) {
				if (region.getSuperRegion().getId() == sri.id) {
					return region;
				}
			}
		}
		// Shouldn't happen
		return null;
	}

   // Sorts the super regions from best region to worst.
   private static List<SuperRegionInformation> sortSuperRegions(List<SuperRegionInformation> superRegionInformations) {
      List<SuperRegionInformation> out = new ArrayList<>();

      // The score for a super region is (army bonus)/(number of neutral armies) * 100.
      // We use this score to decide which super region is the best super region to choose.
     
      // Add all the super regions to a list.
      for (SuperRegionInformation sri : superRegionInformations) {
         out.add(sri);
      }

      // Sort the super regions by the score. In the function, we pass a comparator that
      // we define in the argument. This organizes the super regions by the previously discussed
      // algorithm.
      Collections.sort(out, new Comparator<SuperRegionInformation>() {
         public int compare(SuperRegionInformation sri1, SuperRegionInformation sri2) {
            int scoreSri1 = (int)(sri1.armiesReward / (float)sri1.neutrals * 100);
            int scoreSri2 = (int)(sri2.armiesReward / (float)sri2.neutrals * 100); 
            // List from largest score to smallest
            return scoreSri2 - scoreSri1;
         }
      });

      return out;
   }

	private static List<SuperRegionInformation> getSuperRegionInformations(BotState state) {
		List<SuperRegionInformation> out = new ArrayList<>();
		for (SuperRegion superRegion : state.getFullMap().getSuperRegions()) {
			SuperRegionInformation superRegionInformation = new SuperRegionInformation();
			superRegionInformation.id = superRegion.getId();
			superRegionInformation.armiesReward = superRegion.getArmiesReward();
			superRegionInformation.neutrals = 2 * superRegion.getSubRegions().size();
			for (Region wasteland : state.getWastelands()) {
				if (wasteland.getSuperRegion().getId() == superRegionInformation.id) {
					superRegionInformation.neutrals += 8;
				}
			}
			out.add(superRegionInformation);
		}
		return out;
	}

   private static class SuperRegionInformation {
      int id;
      int armiesReward;
      int neutrals;
   }
}
