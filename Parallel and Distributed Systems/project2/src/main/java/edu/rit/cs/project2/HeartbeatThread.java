package edu.rit.cs.project2;

import java.util.Hashtable;

public class HeartbeatThread implements Runnable{

    private GossipPeer peer;
    private String name;
    public HeartbeatThread(GossipPeer gpeer, String peerName){
        peer = gpeer;
        name = peerName;
    }
 // selects I am alive message and a peer and sends every two seconds
    // also calculates how long since a previous message.

    // every two seconds run select peer method, and send an i am alive message to it
    // if a dead peer is calculated, send that message as well and select to keep that msg
    // if a dead peer seems to come back to life, send that message as well
    // process data and cycle again


    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            peer.processData(peer.getCache());
            String selected = peer.selectPeer();
            //System.out.println("Selected peer for heartbeat: " + selected);
            if (selected != null) {
                String msg = name + " " + peer.selectToSend(0);
                //peer.sendTo(selected, msg);
                Hashtable<String, Integer> dict = peer.getDict();
                //System.out.println("Death note: " + dict.toString());
                String tempmsg = "";
                for (String str : dict.keySet()) {
                    if (dict.get(str) == 8) {
                        tempmsg = "\n" + name + " " + str + peer.selectToSend(1);
                        System.out.println("Appending " + str + " death msg to heartbeat msg.");
                    }
                }
                msg = msg + tempmsg;
                System.out.println("Sending heartbeat msg to: " + selected);
                //System.out.println("Message is: " + msg);
                peer.sendTo(selected, msg);
            }
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println("Here! Heart!");
                System.out.println(e.getMessage());
            }
        }
    }
}
