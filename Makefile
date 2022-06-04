

all: build run


build: 
	javac com/zain/Zag.java

run: 
	java com/zain/Zag ${ARGS}

clean:
	rm -rf com/zain/*.class

