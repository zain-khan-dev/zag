

all: build run


build: 
	javac com/zain/zag/Zag.java

run: 
	java com/zain/zag/Zag ${ARGS}

clean:
	rm -rf com/zain/zag/*.class

