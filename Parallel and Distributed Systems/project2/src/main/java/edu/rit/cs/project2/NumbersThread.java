package edu.rit.cs.project2;

import java.util.Hashtable;
import java.util.Random;
import java.io.*;

public class NumbersThread implements Runnable{
    private NumbersPeer peer;
    private String name;
    public NumbersThread(NumbersPeer npeer, String peerName){
        peer = npeer;
        name = peerName;
    }
    // Generates a random number for a 'round.'
    // Write it to the cache.
    // Sends their current number (and which peer it is from) to a random peer.
    // Receives a number from a random peer.
    // Stores the higher of the two numbers.
    // Repeats

    @Override
    public void run() {

        Random rand = new Random();
        int startInt = rand.nextInt(100) + 1;
        peer.getDict().put(name, startInt);
        synchronized(peer.lock){
            try {
                FileWriter writer = new FileWriter(peer.getCache());
                String msg = name + peer.selectToSend(0) + startInt;
                System.out.println(msg);
                writer.write(msg);
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        //peer.processData(peer.getCache());
        int i = 0;
        int highestNum = 0;
        try {
            Thread.sleep(10000); // wait for other peers to start and connect
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        while(!Thread.currentThread().isInterrupted()) {

            String selected = peer.selectPeer();
            //System.out.println("Selected peer for heartbeat: " + selected);
            if (selected != null) {
                String msg = "";
                Hashtable<String, Integer> dict = peer.getDict();
                highestNum = peer.getHighestNumber(dict);
                if(peer.getHighestPeer(peer.getDict()).equals(name)){

                    msg = name + peer.selectToSend(0) + highestNum;
                }
                else{
                    msg = peer.getHighestPeer(dict) + peer.selectToSend(0) + highestNum;
                }

                //peer.sendTo(selected, msg);
                System.out.println("Sending number msg to: " + selected);
                //System.out.println("Message is: " + msg);
                peer.sendTo(selected, msg);
            }
            try {
                Thread.sleep(2500);
                i++;
            } catch (Exception e) {
                System.out.println("Here! Num!");
                System.out.println(e.getMessage());
            }
            if( i == 5){
                break;
            }
        }

        System.out.println("Round over!");
        System.out.println("Values " + name + " had were: " + peer.getDict().toString());
        System.out.println("The highest number this round was from: "
                + peer.getHighestPeer(peer.getDict()) + " with the number: " + highestNum);
    }
}