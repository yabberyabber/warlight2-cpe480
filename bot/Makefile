botjar: botc
	cd bin && jar cvfm ../NotPhaseBot.jar ../manifest */*.class
	chmod +x NotPhaseBot.jar

botc:
	javac -sourcepath src/ -d bin/ `find src/ -name '*.java' -regex '^[./A-Za-z0-9]*$$'`
