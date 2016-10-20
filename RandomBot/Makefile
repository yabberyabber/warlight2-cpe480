botjar: botc
	cd bin && jar cvfm ../RandomBot.jar ../manifest */*.class
	chmod +x RandomBot.jar

botc:
	javac -sourcepath src/ -d bin/ `find src/ -name '*.java' -regex '^[./A-Za-z0-9]*$$'`
