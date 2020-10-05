package edu.rit.cs.project1;

import java.net.*;
import java.io.*;

public class TCPServer {
    public static void main(String args[]) { //given from TCPServer (handler for running and connections)
        try {
            int serverPort = 7896;
            ServerSocket listenSocket = new ServerSocket(serverPort);
            System.out.println("TCP Server is running and accepting client connections...");
            while (true) {
                Socket clientSocket = listenSocket.accept();
                Connection c = new Connection(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("Listen :" + e.getMessage());
        }
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
        try {   // an echo server

            // Recieve size of byte array as int
            int data_size = in.readInt();
            // Receive csv as byte array size bytes, blocks until read in N bytes.
            byte[] data = in.readNBytes(data_size);
            System.out.println("Received bytes - counting words...");
            File txtfile = WordCount_Seq_Improved.word_count(data); //File returned from wordcount
            byte[] data2 = readBytesFromFile(txtfile); //Covert file to send back to client as bytes

            System.out.println("Word count complete, converting output.");
            System.out.println("Sending back output.");
            out.writeInt(data2.length);
            out.write(data2);

        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {/*close failed*/}
        }
    }

    // Converts a given File object to a byte array representing the information contained in said file.
    private static byte[] readBytesFromFile(File file) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }
}
