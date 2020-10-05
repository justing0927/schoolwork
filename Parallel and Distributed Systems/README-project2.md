# CSCI-251 Concepts of Parallel and Distributed Systems (CoPaDS)

Justin Gonzales

Commands to run Project 2

mvn package to build target directories if not part of built dockerfile.

(If not built csci251:latest) docker build -t csci251:latest -f Dockerfile-project2 .

After building docker image:
docker-compose -f docker-compose-project2.yml up

For the hearbeat protocol:
In five seperate terminals, for each terminal:

docker exec -it peerVARIABLE bash
java -cp project2/target/project2-1.0-SNAPSHOT.jar edu.rit.cs.project2.GossipPeer peerVARIABLE


peerVARIABLE is the unique name (presumed to be name of Docker peers: peerV, peerW, peerX, peerY, and peerZ)


For the numbers protocol:
Follow the same steps, but replace the java command with java -cp project2/target/project2-1.0-SNAPSHOT.jar edu.rit.cs.project2.NumbersPeer peerVARIABLE


Ctrl + C to stop a peer.