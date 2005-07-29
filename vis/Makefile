all: stojvis.jar

stojvis.jar: *.java
	javac -deprecation *.java
	rm -f $@
	jar -cvfm $@ MANIFEST.MF *.class

clean:
	rm -f *.class
	rm -f stojvis.jar
