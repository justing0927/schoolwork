package edu.rit.cs.project1;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TCPClient {

    // Main: Reads in csv from terminal. Prints read in success
    // Sends to Server. Prints progress.
    // Accepts results file from server, and prints.
    public static void main(String args[]) throws IOException {

        //Enter data using BufferReader
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        // Print input request
        System.out.println("Please enter relative path to .csv file: ");
        // Reading data using readLine
        String filePath = reader.readLine();

        // convert input csv file to byte[], wrong path exception otherwise
        byte[] bFile = readBytesFromFile(filePath);

        //server address - should be container name or network alias.
        String server_address = args[0];

        Socket s = null;
        try {
            int serverPort = 7896;
            s = new Socket(server_address, serverPort);
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out =
                    new DataOutputStream(s.getOutputStream());
            //send size of byte array
            out.writeInt(bFile.length);
            //send byte array to server
            out.write(bFile);
            System.out.println("Sent: csv file as bytes");
            // receive size of wordcount byte array
            int data_size = in.readInt();
            //receive wordcount byte array
            byte[] data = in.readNBytes(data_size);
            System.out.println("Received: Word count file");
            // Create "clientoutput.txt" file based on byte array received from server
            outputFile(data);

        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (s != null)
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
        }
    }

    // given a file path, creates a byte array representing the contents of the file at said path.
    private static byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
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

    // Creates clientoutput.txt, writing the byte array data to it.
    // Uses the written file to also write to terminal word count information.
    private static void outputFile(byte[] data){
        try {

            File file = new File("clientoutput.txt");

            // Initialize a pointer
            // in file using OutputStream
            OutputStream
                    os
                    = new FileOutputStream(file);

            // Starts writing the bytes in it
            os.write(data);

            // Close the file
            os.close();
        }

        catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        File file = new File("clientoutput.txt");
        FileInputStream fin = null;
        try {
            // create FileInputStream object
            fin = new FileInputStream(file);

            byte fileContent[] = new byte[(int)file.length()];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent);
            //create string from byte array that prints the file contents to terminal
            String str = new String(fileContent);
            System.out.println(str);
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        }
        finally {
            // close the streams using close method
            try {
                if (fin != null) {
                    fin.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
    }

}



