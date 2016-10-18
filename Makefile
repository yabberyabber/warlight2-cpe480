default:
	cd bot && make botjar
	cd eng && make run

zipbot:
	touch notPhaseBot.zip
	rm notPhaseBot.zip
	cd bot/src && zip -r notPhaseBot.zip * && mv notPhaseBot.zip ../..
