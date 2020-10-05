package edu.rit.cs.project2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PassiveThread implements Runnable {

    private IPeer gpeer;
    private String peerName;

    public PassiveThread(IPeer peer, String name){
        gpeer = peer;
        peerName = name;
    }


    @Override
    public void run() {
        try {
            int serverPort = 7896;
            ServerSocket listenSocket = new ServerSocket(serverPort);
            while (true) {
                Socket clientSocket = listenSocket.accept();
                Connection c = new Connection(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("Listen :" + e.getMessage());
        }
    }

    class Connection extends Thread { //Connection class given from TCPServer for basic setup
        DataInputStream in;
        DataOutputStream out;
        Socket clientSocket;

        public Connection(Socket aClientSocket) {
            try {
                clientSocket = aClientSocket;
                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());
                this.start();
            } catch (IOException e) {
                System.out.println("Connection:" + e.getMessage());
            }
        }

        public void run() {
            try {
                // Recieve size of byte array as int
                int data_size = in.readInt();
                // Receive csv as byte array size bytes, blocks until read in N bytes.
                byte[] data = in.readNBytes(data_size);
                String fullmsg = new String(data, StandardCharsets.UTF_8);
                String[] msgsplit = fullmsg.split("\n");
                for( String str : msgsplit) {
                    String[] split = str.split(" ");
                    String incomPeer = split[0];
                    gpeer.receiveFrom(incomPeer, str);
                }


            } catch (EOFException e) {
                System.out.println("EOF:" + e.getMessage());
            } catch (Exception e) {
                System.out.println("IO:" + e.getMessage());
            } finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                } catch (IOException e) {/*close failed*/}
            }
        }
    }
}
