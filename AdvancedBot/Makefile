botjar: botc
	cd bin && jar cvfm ../AdvancedBot.jar ../manifest */*.class
	chmod +x AdvancedBot.jar

botc:
	javac -sourcepath src/ -d bin/ `find src/ -name '*.java' -regex '^[./A-Za-z0-9]*$$'`

run:
	java -jar AdvancedBot.jar
