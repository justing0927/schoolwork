package edu.rit.cs.project2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.util.Scanner;

public class PassiveBroadcastThread implements Runnable {
    private IPeer gpeer;
    private String name;
    private DatagramSocket socket;

    public PassiveBroadcastThread(IPeer peer, String peerName) {
        gpeer = peer;
        name = peerName;
        try {
            socket = new DatagramSocket(4445, InetAddress.getByName("0.0.0.0"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Receives a broadcast from a peer on the socket. Checks to see if that name is stored
     * as a peer in the peer's local cache. Stores it if it is unknown, ignored and continues to receive otherwise.
     */
    @Override
    public void run() {
        // runs as long as thread not interrupted
        while (true) {
            try {
                // receives new Broadcast message as a DatagramPacket
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length,
                        InetAddress.getByName("0.0.0.0"), socket.getLocalPort());
                socket.receive(packet);

                //InetAddress address = packet.getAddress();
                //int port = packet.getPort();
                //packet = new DatagramPacket(buf, buf.length, address, port);

                // Parses packet data to determine name/network of peer that broadcasted
                String received = new String(packet.getData(), 0, packet.getLength());
                //System.out.println("Passive listener received: " + received);
                String[] messageLst = received.split(" ");
                String potentialNewName = messageLst[0];
                if(!potentialNewName.equals(name)) {
                    String nameForStoring = potentialNewName + " " + potentialNewName + " is at " + potentialNewName + ".";
                    //System.out.println("Received Broadcast from: " + potentialNewName);
                    //System.out.println("Attempting to store: " + nameForStoring);
                    /*
                    // poor attempt at synchronization

                    while(gpeer.cacheLock){
                        Thread.currentThread().wait(250);
                    }
                    */
                    gpeer.selectToKeep(nameForStoring);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Here! Passive Broad!");
            }
        }
    }
}


