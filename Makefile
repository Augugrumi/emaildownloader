SHELL := /bin/bash #Need bash not shell

all: build compile

build:
	mvn compile

compile:
	@mvn exec:java -Dexec.mainClass="com.augugrumi.emaildownloader.Main" -Dexec.args="$(IMAGES_PATH) $(EMAIL) $(PASSWORD)"
