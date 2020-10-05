package edu.rit.cs.project2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class NumbersPeer implements IPeer {

    // Name for the peer object, how it references itself and is referenced. SHOULD BE UNIQUE.
    private static String name;

    // Creates local cache.txt file for storing and pulling peer info
    private static File cache;

    // Create the four threads that run the peer - documented in project2systemdiagram.png
    private static BroadcastThread brd;
    private static PassiveBroadcastThread psvbrd;
    private static NumbersThread nmbr;
    private static PassiveThread psv;

    public final Object lock = new Object();

    private Hashtable<String, Integer> dict = new Hashtable<String, Integer>();

    /**
     * Constructor for a GossipPeer object, initializes threads and info for object
     * stores cache state and updates it
     * @param name Name for the peer (should be unique, match yml peer, and not have spaces)
     */
    public NumbersPeer(String name){
        // create unique local cache for gossip.
        String pathName = name + "_cache.txt";
        cache = new File(pathName);
        try {
            if (cache.createNewFile()) {
                System.out.println("Created local cache for: " + name + " at: " + pathName);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

        // broadcast and passives should be started for communication
        brd = new BroadcastThread(name);
        psvbrd = new PassiveBroadcastThread(this, name);
        psv = new PassiveThread(this, name);
        // create thread objects
        Thread brdcstThread = new Thread(brd);
        Thread pasvbrdcstThread = new Thread(psvbrd);
        Thread psvThread = new Thread(psv);
        // start threads
        brdcstThread.start();
        pasvbrdcstThread.start();
        psvThread.start();

        // Start up heartbeat thread
        nmbr = new NumbersThread(this, name);

        Thread nmbrThread = new Thread(nmbr);

        nmbrThread.start();

    }

    /**
     * Starts the running of a Peer object, starting each of the threads that make it up so
     * it can begin to compile cache information and gossip.
     * @param args Command-line arguments. should be unique yml peer peerid.
     */
    public static void main(String[] args){

        // parse command line arg
        name = args[0];

        // construct peer object which inits threads based on state
        NumbersPeer peer = new NumbersPeer(name);

        // Threads now started and running for program.

    }

    /**
     * Select a neighboring peer to send a message
     * @return Selected peer
     */
    public String selectPeer(){
        synchronized (this.lock) {
            try {
                Scanner reader = new Scanner(cache);
                String[] listOfPeers = new String[20];

                // determines how many different peers are known and puts them into an array
                int i = 0;
                //System.out.println("Known living peers: ");
                while (reader.hasNextLine() && i < 10) {
                    final String lineFromFile = reader.nextLine();
                    if (lineFromFile.contains(" is at ")) {
                        // e.g. Peer1 is at Peer1 -> Peer1
                        String peer = lineFromFile.split(" ")[0];
                        listOfPeers[i] = peer;
                        //System.out.println(peer);
                        i++;
                    }
                }

                // select random peer
                Random rand = new Random();
                int peer_pos = rand.nextInt(i);
                String selected_peer = listOfPeers[peer_pos];
                return selected_peer;

            } catch (Exception e){
                //System.out.println(e.getMessage());
                //System.out.println("Here! SelectPeer!");
            }
        }
        return null;
    }

    /**
     * Select a message to be sent
     * @return Selected message
     */
    public String selectToSend(int state){
        if(state == 0){
            return " has the current highest number with: ";
        }
        else {
            return " Error.";
        }
    }

    /**
     * Send a selected message to a selected peer
     * @param peer Selected peer
     * @param msg Selected message
     */
    public void sendTo(String peer, String msg){
        try {
            int serverPort = 7896;
            Socket s = new Socket(peer, serverPort);
            DataOutputStream out =
                    new DataOutputStream(s.getOutputStream());
            //send msg length
            byte[] b = msg.getBytes();
            out.writeInt(b.length);
            //send msg
            out.write(b);
            //System.out.println("Sent selected message to: " + peer);
        } catch(Exception e){
            //System.out.println("Here! Send to!");
            System.out.println(e.getMessage() + " " + peer + " may be dead.");
        }
    }

    /**
     * Receive a message from a peer
     * @param peer A peer who sent a message
     * @param msg The message content
     */
    public void receiveFrom(String peer, String msg){
        //Passive thread calls this function.
        System.out.println(name + " received number from: " + peer);
        selectToKeep(msg);
        //sendTo(peer, name + " " + selectToSend(0));
    }

    /**
     * Receive a message from any peer
     * @param msg The message content
     */
    public void receiveFromAny(String msg){
        //Passive thread acts as implementation of this function.
    }

    /**
     * Determine whether to cache the message to a file
     * @param fullmsg The message content
     */
    public void selectToKeep(String fullmsg){
        /**
         * Keep a message if:
         * It is new information (new Peer, peer has died, or thought dead peer alive)
         * Don't keep a message if:
         * The exact message already exists that isn't a heartbeat message.
         */
        // write msg to cache otherwise exit
        synchronized (lock) {
            try {

                String[] split = fullmsg.split(" ");
                //System.out.println("Full message: " + fullmsg);
                String incomPeer = split[0];
                String msg = "";
                for(int i = 1; i < split.length; i++){
                    if( i == split.length - 1){
                        msg = msg + split[i];
                    }
                    else {
                        msg = msg + split[i] + " ";
                    }
                }
                //System.out.println("Select to keep: " + msg);
                Scanner reader = new Scanner(cache);
                Boolean hasHigherNumber = false;
                Boolean alreadyContainsMsg = false;
                int i = 0;
                // determines if msg is already in cache
                while (reader.hasNextLine()) {
                    final String lineFromFile = reader.nextLine();
                    //System.out.println("Printing Cache Line: " + lineFromFile);
                    if(msg.contains("number")){
                        // refreshes counter for living message
                        String[] spl = msg.split(" ");
                        i = Integer.parseInt(spl[spl.length - 1]);
                        dict.put(incomPeer, i);

                        if(i > getHighestNumber(dict)){
                            hasHigherNumber = true;
                        }
                    }
                    if (lineFromFile.contains(msg) || lineFromFile.equals(msg)) {
                        //System.out.println("Message: " + msg);
                        //System.out.println("Was compared to: " + lineFromFile);
                        alreadyContainsMsg = true;
                    }
                }
                //System.out.println(msg);
                //System.out.println(msg + "Found? " + alreadyContainsMsg.toString());
                if ((hasHigherNumber || msg.contains(" is at ")) && !alreadyContainsMsg)  {
                    System.out.println("Writing to Cache: " + msg);

                    BufferedReader filer = new BufferedReader(new FileReader(cache));
                    StringBuffer inputBuffer = new StringBuffer();
                    String line;

                    while ((line = filer.readLine()) != null) {
                        inputBuffer.append(line);
                        inputBuffer.append('\n');
                    }
                    filer.close();
                    inputBuffer.append(msg);
                    System.out.println(msg);
                    inputBuffer.append('\n');

                    FileOutputStream fileOut = new FileOutputStream(cache);
                    fileOut.write(inputBuffer.toString().getBytes());
                    fileOut.close();
                }
            }catch(Exception e){
                System.out.println(e.getMessage());
                System.out.println("Here! Select!");
            }
        }
    }

    /**
     * Process the data from a local cache. Unneeded as long as cache simply
     * contains the highest number known, and dict contains everything.
     * @param file The file containing multiple messages
     */
    public void processData(File file) {
        /**
        // determine if any nodes have died via dict
        // change their entry to dead
        try {
            // increment each dict value by 1.
            for (String str : dict.keySet()) {
                int i = dict.get(str);
                //System.out.println("Processing. " + str + " is at: " + i + ".");
                i += 1;
                if ((i >= 8 && i < 12) || i == 1) {
                    String search1 = str + " is alive."; //a now dead node needs to be classified as such
                    String search2 = str + " is dead."; // a thought dead node needs to be considered alive
                    String search3 = str + " is at "; // a now dead node needs to be removed from peer list
                    if( i == 8) {
                        System.out.println("Processing. " + str + " has not responded for 10 seconds.");
                    }
                    synchronized (lock) {
                        // input the (modified) file content to the StringBuffer "input"
                        BufferedReader filer = new BufferedReader(new FileReader(cache));
                        StringBuffer inputBuffer = new StringBuffer();
                        String line;

                        while ((line = filer.readLine()) != null) {
                            if((line.equals(search1) || line.contains(search3)) && (i >= 8)){
                                line = str + " is dead.";
                            }
                            if((line.equals(search3) || line.contains(search3)) && (i >= 8)){
                                line = "";
                            }
                            if((line.equals(search2) || line.contains(search2)) && i == 1){
                                line = str + " is alive.";
                            }
                            inputBuffer.append(line);
                            inputBuffer.append('\n');
                        }
                        filer.close();

                        // write the new string with the replaced line OVER the same file
                        FileOutputStream fileOut = new FileOutputStream(cache);
                        fileOut.write(inputBuffer.toString().getBytes());
                        fileOut.close();
                    }
                }
                dict.put(str, i);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Here! Process!");
        }
         */
    }

    /**
     * @return the File objext for the local cache.
     */
    public File getCache(){
        return cache;
    }

    /**
     * @return the dictionary representing the life of peers.
     */
    public Hashtable<String, Integer> getDict(){
        return dict;
    }

    /**
     * @return the highest number in the dictionary.
     */
    public int getHighestNumber(Hashtable<String, Integer> dict){
        int temp = 0;
        for(int i : dict.values()){
            if (temp < i) {
                temp = i;
            }
        }
        return temp;
    }

    /**
     * @return the String representing the peer with the highest number.
     */
    public String getHighestPeer(Hashtable<String, Integer> dict){
        int temp = 0;
        String tempStr = "";
        for(String s : dict.keySet()){
            if (temp < dict.get(s)) {
                temp = dict.get(s);
                tempStr = s;
            }
        }
        return tempStr;
    }

}
