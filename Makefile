BOT?=RandomBot
BOT2?=RandomBot

MAP?=engine/example-map.txt

fight: engine $(BOT) $(BOT2)
	java -cp engine/lib/java-json.jar:engine/bin com.theaigames.game.warlight2.Warlight2 $(MAP) "java -jar $(BOT)/$(BOT).jar" "java -jar $(BOT2)/$(BOT2).jar"

%Bot: FORCE
	cd $@ && make botjar

zip:
	touch notPhaseBot.zip
	rm notPhaseBot.zip
	cd RandomBot/src && zip -r notPhaseBot.zip * && mv notPhaseBot.zip ../..

engine: engine/bin/com/theaigames/engine/Engine.class

engine/bin/com/theaigames/engine/Engine.class:
	mkdir -p engine/bin
	cd engine && javac -sourcepath src/ -d bin/ -cp lib/java-json.jar `find src/ -name '*.java' -regex '^[./A-Za-z0-9]*$$'`

clean: FORCE
	rm -r */bin

FORCE:
