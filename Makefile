BOT?=RandomBot
BOT2?=RandomBot

MAP?=engine/example-map.txt

fight: $(BOT) $(BOT2)
	java -cp engine/lib/java-json.jar:engine/bin com.theaigames.game.warlight2.Warlight2 $(MAP) "java -jar $(BOT)/$(BOT).jar" "java -jar $(BOT2)/$(BOT2).jar"

%Bot: FORCE
	cd $@ && make botjar

zip:
	touch notPhaseBot.zip
	rm notPhaseBot.zip
	cd RandomBot/src && zip -r notPhaseBot.zip * && mv notPhaseBot.zip ../..

FORCE:
