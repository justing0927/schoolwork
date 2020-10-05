# CSCI-251 Concepts of Parallel and Distributed Systems (CoPaDS)

Justin Gonzales

Commands to run Project 1

mvn package to build target directories.

If built csci251:latest with docker:
docker-compose -f docker-compose-project1.yml up

Otherwise:
docker-compose -f docker-compose-project1.yml up --build --remove-orphans

In seperate terminal:

docker exec -it peer1 bash
cd project1
java -cp target/project1-1.0-SNAPSHOT.jar edu.rit.cs.project1.TCPServer

In seperate terminal:
docker exec -it peer2 bash
cd project1
java -cp target/project1-1.0-SNAPSHOT.jar edu.rit.cs.project1.TCPClient peer1

Enter relative path:
/csci251/project1/src/main/java/edu/rit/cs/project1/affr.csv

DOCKER-COMPOSE INTEGRATED COMMAND RUN:

Add to peer1:
	command: java -cp project1/target/project1-1.0-	SNAPSHOT.jar edu.rit.cs.project1.TCPServer

Add to peer2:
	command: java -cp target/project1-1.0-SNAPSHOT.jar 	edu.rit.cs.project1.TCPClient peer1

ALTERNATIVE METHOD:

docker network create csci251network
docker run --name testpeer --network csci251network -it csci251:latest
cd project1
java -cp target/project1-1.0-SNAPSHOT.jar edu.rit.cs.project1.TCPServer

* seperate terminal *
docker run --name testpeer2 --network csci251network -it csci251:latest
cd project1
java -cp target/project1-1.0-SNAPSHOT.jar edu.rit.cs.project1.TCPClient testpeer2

Enter relative path:
/csci251/project1/src/main/java/edu/rit/cs/project1/affr.csv
