all: JRclient.jar
	make -C examples

JRclient.jar: *.java
	rm -rf org
	javac -d . *.java
	jar fvc JRclient.jar org
	rm -rf org

doc:
	rm -rf JavaDoc
	mkdir -p JavaDoc
	javadoc -d JavaDoc -author -version -breakiterator -link http://java.sun.com/products/jdk/1.2/docs/api *.java

clean:
	rm -rf JRclient.jar org JavaDoc *~
	make -C examples clean

