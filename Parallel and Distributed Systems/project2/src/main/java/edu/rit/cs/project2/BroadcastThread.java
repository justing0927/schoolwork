package edu.rit.cs.project2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class BroadcastThread implements Runnable {
    private String peerName;
    private DatagramSocket socket = null;
    private String broadcastMessage = "";
    private InetAddress address;

    public BroadcastThread(String name) {
        peerName = name;
        try {
            address = InetAddress.getByName("255.255.255.255");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        broadcastMessage = peerName + " is broadcasting.";
    }

    @Override
    public void run() {
        while (true) {
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);

                byte[] buffer = broadcastMessage.getBytes();

                DatagramPacket packet
                        = new DatagramPacket(buffer, buffer.length, address, 4445);
                socket.send(packet); //send broadcast
                //System.out.println(broadcastMessage);
                socket.close();

                // wait 8 seconds to broadcast again
                Thread.sleep(8000);
            } catch (Exception e) {
                socket.close();
                System.out.println(e.getMessage());
                System.out.println("Here! Broadcast!");
            }

        }
    }
}


