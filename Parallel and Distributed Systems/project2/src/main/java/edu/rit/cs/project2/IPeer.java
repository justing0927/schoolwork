package edu.rit.cs.project2;

import java.io.File;

// interface defined based on this paper, https://www.distributed-systems.net/my-data/papers/2007.osr.pdf
public interface IPeer {

    /**
     * Select a neighboring peer to send a message
     * @return Selected peer's name
     */
    public String selectPeer();

    /**
     * Select a message to be sent
     * @param state integer representing what message to select
     * @return Selected message
     */
    public String selectToSend(int state);

    /**
     * Send a selected message to a selected peer
     * @param peer Selected peer's name
     * @param msg Selected message
     */
    public void sendTo(String peer, String msg);

    /**
     * Receive a message from a peer
     * @param peer Name of a peer who sent a message
     * @param msg The message content
     */
    public void receiveFrom(String peer, String msg);

    /**
     * Receive a message from any peer
     * @param msg The message content
     */
    public void receiveFromAny(String msg);

    /**
     * Determine whether to cache the message to a file
     * @param msg The message content
     */
    public void selectToKeep(String msg);

    /**
     * Process the data from a local cache
     * @param file The file containing multiple messages
     */
    public void processData(File file);

}
