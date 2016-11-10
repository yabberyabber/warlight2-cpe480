BOT?=RandomBot
BOT2?=RandomBot

fight: build_engine $(BOT) $(BOT2)
	cd engine && node run_game.js "java -jar ../$(BOT)/$(BOT).jar" "java -jar ../$(BOT2)/$(BOT2).jar"

%Bot: FORCE
	cd $@ && make botjar

zip:
	touch notPhaseBot.zip
	rm notPhaseBot.zip
	cd RandomBot/src && zip -r notPhaseBot.zip * && mv notPhaseBot.zip ../..

build_engine: 
	mkdir -p engine/bin
	cd engine && javac -sourcepath src/ -d bin/ -cp lib/java-json.jar `find src/ -name '*.java' -regex '^[./A-Za-z0-9]*$$'`

server:
	cd engine/replay && http-server

clean: FORCE
	rm -r */bin

FORCE:
