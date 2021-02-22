package hangman.server;

import java.io.*;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server. 
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no 
 * need to count bytes or to wait for a line feed at the end of the frame
 */

public class ChatMessage implements Serializable {
    // The different types of message sent by the Client
    // MESSAGE an ordinary text message
    // LOGOUT to disconnect from the Server
    public static final int MESSAGE = 1, LOGOUT = 2;
    private final int type;
    private final String message;

    // constructor
    public ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    int getType() {
        return type;
    }

    String getMessage() {
        return message;
    }
}