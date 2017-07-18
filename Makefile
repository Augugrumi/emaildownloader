SHELL := /bin/bash #Need bash not shell

all: run

run:
	@mvn compile exec:java -Dexec.mainClass="com.augugrumi.emaildownloader.Main" -Dexec.args="$(IMAGES_PATH) $(EMAIL) $(PASSWORD)"
travis:
	mvn compile
