/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 *
 * @author leonh
 */
public class Server extends javax.swing.JFrame {
    // a unique ID for each connection
    private static int uniqueId;
    // an ArrayList to keep the list of the Client
    private final ArrayList<ClientThread> clientThreads;
    // declare a serversocket
    private ServerSocket serverSocket;
    // to display time
    private final SimpleDateFormat sdf;
    // the port number to listen for connection
    private int port;
    // to check if server is running
    private boolean keepGoing;
    // guess word
    private String guessWord;
    // empty word for guessing
    private String emptyWord;
    // number of guesses
    private int guessCount;
    // number of hint
    private int hintCount;
    // notification
    private static final String NOTIF = " *** ";
    // stages for hangman
    public static final String[] hangmanStage = {
            "--------\n" +
            "|      |\n" +
            "|\n" +
            "|\n" +
            "|\n" +
            "|\n" +
            "|",
        
            "--------\n" +
            "|      |\n" +
            "|      O\n" +
            "|\n" +
            "|\n" +
            "|\n" +
            "|",
            
            "--------\n" +
            "|      |\n" +
            "|      O\n" +
            "|      |\n" +
            "|\n" +
            "|\n" +
            "|",
            
            "--------\n" +
            "|      |\n" +
            "|      O\n" +
            "|      |\n" +
            "|      |\n" +
            "|\n" +
            "|",
    
            "--------\n" +
            "|      |\n" +
            "|      O\n" +
            "|      |\\\n" +
            "|      |\n" +
            "|\n" +
            "|",
    
            "--------\n" +
            "|      |\n" +
            "|      O\n" +
            "|     /|\\\n" +
            "|      |\n" +
            "|\n" +
            "|",
    
            "--------\n" +
            "|      |\n" +
            "|      O\n" +
            "|     /|\\\n" +
            "|      |\n" +
            "|       \\\n" +
            "|",
    
            "--------\n" +
            "|      |\n" +
            "|      O\n" +
            "|     /|\\\n" +
            "|      |\n" +
            "|     / \\\n" +
            "|"
    };

    /**
     * Creates new form ServerGUI
     */
    public Server() {
        // the port
        this.port = 15000;
        // to display HH:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");
        // an ArrayList to keep the list of the Client
        clientThreads = new ArrayList<>();
        
        guessWord = emptyWord = null;
        guessCount = 0;
        hintCount = 3;
        
        initComponents();
    }

    /**
     * Creates new form ServerGUI
     * @param port
     */
    public Server(int port) {
        // the port
        this.port = port;
        // to display HH:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");
        // an ArrayList to keep the list of the Client
        clientThreads = new ArrayList<>();
        
        guessWord = emptyWord = null;
        guessCount = 0;
        hintCount = 3;
        
        initComponents();
    }

    public void start() {
        try {
            keepGoing = true;
            // the socket used by the server
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            // start listener thread
            new ListenForClient(serverSocket).start();
        } catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e;
            display(msg);
        }
    }

    // to send stop message
    protected void stop() {
        keepGoing = false;
        // try to stop the server
        try {
            serverSocket.close();
            for(int i = 0; i < clientThreads.size(); ++i) {
                ClientThread tc = clientThreads.get(i);
                try {
                    // close all data streams and socket
                    tc.sInput.close();
                    tc.sOutput.close();
                    tc.socket.close();
                }
                catch(IOException e) {
                    display("Exception closing clients connection: " + e);
                }
            }
            clientThreads.clear();
        } catch(IOException e) {
            display("Exception closing the server and clients: " + e);
        }
    }

    // Display an event to the console
    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        System.out.println(time);
        updateTerminalBox(time);
    }

    // to broadcast a message to all Clients
    private synchronized int broadcast(String message) {
        // add timestamp to the message
        String time = sdf.format(new Date());

        // to check the type of message
        String[] messageSplit = message.split(" ",2);
        
        boolean isGuessing = false;
        boolean isCommand = false;
        
        if(messageSplit[1].charAt(0) == '~') 
            isGuessing = true;
        
        if(messageSplit[1].charAt(0) == '!')
            isCommand = true;

        // if the message is a command
        if(isCommand) {
            String sender = messageSplit[0].substring(0, messageSplit[0].length() - 1);
            String messageLf = commands(messageSplit[1].substring(1, messageSplit[1].length()));
            
            // if command doesn't exist
            if (messageLf.equals("-1")) {
                return 1;
            }
            // if command exist
            else {
                // we loop in reverse order to find the sender
                switch(messageLf) {
                    case "START" -> {
                        broadcast(NOTIF + sender + " started the Game!" + NOTIF);
                        broadcast(NOTIF + "Type \'!HOWTOPLAY\' without quotes if you need any help with the game." + NOTIF);
                        printWord();
                    }
                    case "STOP" -> {
                        if (guessWord != null) {
                            broadcast(NOTIF + sender + " stopped the Game." + NOTIF);
                            finishingGame();
                        } else return 2;
                    }
                    case "HINT" -> {
                        if (guessWord != null) {
                            if (hintCount > 0) {
                                int random;
                                do {
                                    random = (int)(Math.random() * (((guessWord.length() - 1) - 0) + 1)) + 0;
                                } while (emptyWord.charAt(random) != '-');

                                StringBuilder sb = new StringBuilder(emptyWord);
                                sb.setCharAt(random, guessWord.charAt(random));
                                emptyWord = sb.toString();

                                broadcast(NOTIF + sender + " asked for a hint. Current number of hints: " + hintCount + NOTIF);
                                printWord();
                                
                                hintCount--;
                            } else return 4;
                        } else return 2;
                    }
                    default -> {
                        for(int y = clientThreads.size(); --y >= 0;) {
                            ClientThread ct1=clientThreads.get(y);
                            String check=ct1.getUsername();
                            if(check.equals(sender)) {
                                if(!ct1.writeMsg(messageLf)) {
                                    clientThreads.remove(y);
                                    display("Disconnected Client " + ct1.username + " removed from list.");
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        // if the message is guessing for the game's word
        else if(isGuessing) {
            if (guessWord != null) {
                if (guessCount < hangmanStage.length) {
                    String sender = messageSplit[0].substring(0, messageSplit[0].length() - 1);
                    String toCheck = messageSplit[1].substring(1, messageSplit[1].length());
                    String messageLf;
                    
                    messageLf = time + " " + sender + ": " + toCheck + '\n';
                    // display message
                    System.out.print(messageLf);
                    updateTerminalBox(messageLf);

                    // we loop in reverse order in case we would have to remove a Client
                    // because it has disconnected
                    for(int i = clientThreads.size(); --i >= 0;) {
                        ClientThread ct = clientThreads.get(i);
                        // try to write to the Client if it fails remove it from the list
                        if(!ct.writeMsg(messageLf)) {
                            clientThreads.remove(i);
                            display("Disconnected Client " + ct.username + " removed from list.");
                        } 
                    }

                    // check if the guessed word is correct or not
                    printWord();
                    boolean correct = guessWord.equalsIgnoreCase(toCheck);
                    
                    guessCount++;
                    if(correct) {
                        broadcast(NOTIF + sender + " has guessed the correct word!" + NOTIF);
                        finishingGame();
                    } else return 3;
                } else {
                    finishingGame();
                }
            } else return 2;
        }
        // if message is a broadcast message
        else {
            String messageLf = time + " " + message + '\n';
            // display message
            System.out.print(messageLf);
            updateTerminalBox(messageLf);

            // we loop in reverse order in case we would have to remove a Client
            // because it has disconnected
            for(int i = clientThreads.size(); --i >= 0;) {
                ClientThread ct = clientThreads.get(i);
                // try to write to the Client if it fails remove it from the list
                if(!ct.writeMsg(messageLf)) {
                    clientThreads.remove(i);
                    display("Disconnected Client " + ct.username + " removed from list.");
                } 
            }
        }
        return 0; // if 0 = SAVE CALL, if 1 = WRONG COMMAND, if 2 = GAME HAVEN'T STARTED, if 3 = WRONG ANSWER/GUESS, if 4 = NO MORE HINTS
    }

    // if client sent LOGOUT message to exit
    synchronized void removeThread(int id) {
        String disconnectedClient = "";
        // scan the array list until we found the Id
        for(int i = 0; i < clientThreads.size(); ++i) {
            ClientThread ct = clientThreads.get(i);
            // if found remove it
            if(ct.id == id) {
                disconnectedClient = ct.getUsername();
                clientThreads.remove(i);
                break;
            }
        }
        
        broadcast(NOTIF + disconnectedClient + " has left the chat room." + NOTIF);
    }
    
    // preparing for game start
    private void prepareGameStart() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("resources/words.txt");
        Scanner output = new Scanner(stream);

        int random = (int)(Math.random() * ((872 - 0) + 1)) + 0;
        String line = null;
        for (int i = 0; i <= random; i++) {
            line = output.nextLine();
        }
        
        guessWord = line;
        StringBuilder sb = new StringBuilder(guessWord.length());
        for (int i = 0; i < guessWord.length(); i++) {
            if (guessWord.charAt(i) == ' ')
                sb.append(' ');
            else
                sb.append('-');
        }
        emptyWord = sb.toString();

        hintCount = guessWord.length() - 3;
    }
    
    // wrapping things up
    private void finishingGame() {
        if (guessCount > hangmanStage.length - 1) {
            broadcast(NOTIF + "All of you have lost, the correct word is: " + guessWord + NOTIF);
            guessCount = hangmanStage.length - 1;
        }
        
        emptyWord = guessWord;
        printWord();
        
        guessCount = 0;
        hintCount = 3;
        guessWord = null;
        emptyWord = null;
        
        broadcast(NOTIF + "Thank you for playing the Game!" + NOTIF);
    }
    
    // send a unique message for the hangman stage
    private void printWord() {
        broadcast(NOTIF + " " + emptyWord + " " + NOTIF + "\n\n" + hangmanStage[guessCount]);
    }
    
    // individual commands function
    private String commands(String command) {
        String time = sdf.format(new Date());
        switch(command.toUpperCase()) {
            case "HELP" -> {
                return "List of available commands: " + '\n' +
                        "START - Start the game." + '\n' +
                        "STOP - Stop ongoing game." + '\n' +
                        "HINT - Show hint for the current game." + '\n' +
                        "HELP - Show list of avaialable commands." + '\n' +
                        "HOWTOPLAY - Help with playing the game." + '\n' +
                        "LOGOUT - Exit the client." + '\n';
            }
            case "START" -> {
                prepareGameStart();
                return "START";
            }
            case "STOP" -> {
                return "STOP";
            }
            case "HINT" -> {
                return "HINT";
            }
            case "HOWTOPLAY" -> {
                return "How to play: " + '\n' +
                        "Type \'~any_word\' without quotes and your word of choice to guess the word" + '\n' +
                        "You only have 8 chances to guess the word." + '\n' +
                        "Type \'!HINT\' without quotes to show hints." + '\n' +
                        "You can use hint only for a certain amount of time." + '\n' +
                        "Type \'!HELP\' without quotes for additional help." + '\n';
            }
            default -> {
                return "-1";
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        terminalBox = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hanged.io Server");
        setAutoRequestFocus(false);
        setResizable(false);

        startButton.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        stopButton.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        stopButton.setText("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        jTextField1.setEditable(false);
        jTextField1.setBackground(new java.awt.Color(255, 255, 255));
        jTextField1.setFont(new java.awt.Font("Monospaced", 0, 24)); // NOI18N
        jTextField1.setText("     Hanged.io Server");
        jTextField1.setFocusable(false);

        terminalBox.setEditable(false);
        terminalBox.setColumns(20);
        terminalBox.setLineWrap(true);
        terminalBox.setRows(5);
        terminalBox.setWrapStyleWord(true);
        terminalBox.setFocusable(false);
        jScrollPane1.setViewportView(terminalBox);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(111, 111, 111)
                        .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        this.port = 15000;
        this.start();
        this.stopButton.setEnabled(true);
        this.startButton.setEnabled(false);
    }//GEN-LAST:event_startButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        this.port = -1;
        this.stop();
        this.stopButton.setEnabled(false);
        this.startButton.setEnabled(true);
    }//GEN-LAST:event_stopButtonActionPerformed
    
    // Display an event to the terminalBox
    private void updateTerminalBox(String msg) {
        terminalBox.append("> " + msg + '\n');
        terminalBox.setCaretPosition(terminalBox.getText().length());
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Server().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JTextArea terminalBox;
    // End of variables declaration//GEN-END:variables

    class ClientThread extends Thread {
        // the socket to get messages from client
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        // my unique id (easier for deconnection)
        int id;
        // the Username of the Client
        String username;
        // message object to recieve message and its type
        ChatMessage cm;
        // timestamp
        String date;

        // Constructor
        public ClientThread(Socket socket) {
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
            //Creating both Data Stream
            String temp = "Thread trying to create Object Input/Output Streams";
            System.out.println(temp);
            updateTerminalBox(temp);
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                // read the username
                username = (String) sInput.readObject();
                broadcast(NOTIF + username + " has joined the chat room." + NOTIF);
            } catch (IOException | ClassNotFoundException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            date = new Date().toString() + "\n";
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        // infinite loop to read and forward message
        @Override
        public void run() {
            writeMsg(NOTIF + "Welcome " + getUsername() + ", to the chat room!" + NOTIF + '\n');
            writeMsg(NOTIF + "Feel free to chat anything in this chat room!" + NOTIF + '\n');
            writeMsg(NOTIF + "Type \'!HELP\' without quotes for any help with the program." + NOTIF + '\n');
            writeMsg(NOTIF + "Type \'!START\' without quotes to start the game." + NOTIF + '\n');
            
            // to loop until LOGOUT
            boolean keepGoing = true;
            while(keepGoing) {
                if (guessWord != null) {
                    if (guessCount > hangmanStage.length - 1) {
                        finishingGame();
                    }
                }
                
                // read a String (which is an object)
                try {
                    cm = (ChatMessage) sInput.readObject();
                }
                catch (IOException | ClassNotFoundException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;				
                }
                // get the message from the ChatMessage object received
                String message = cm.getMessage();

                // different actions based on type message
                switch(cm.getType()) {
                    case ChatMessage.LOGOUT -> {
                        display(username + " disconnected with a LOGOUT message.");
                        keepGoing = false;
                    }

                    case ChatMessage.MESSAGE -> {
                        int confirmation =  broadcast(username + ": " + message);
                        switch(confirmation){
                            // if no more hints
                            case 4 -> {
                                String msg = NOTIF + "Sorry, there are no more hints for the current game." + NOTIF + '\n';
                                writeMsg(msg);
                            }
                            // if guessed the wrong word
                            case 3 -> {
                                String msg = NOTIF + "Sorry, that's not the correct word." + NOTIF + '\n';
                                writeMsg(msg);
                            }
                            // if game not started yet
                            case 2 -> {
                                String msg = NOTIF + "Sorry, the game hasn't been started or it's already over." + NOTIF + '\n';
                                writeMsg(msg);
                            }
                            // if the comand doesn't exist
                            case 1 -> {
                                String msg = NOTIF + "Sorry, that command doesn't exist, type \"!HELP\" for help." + NOTIF + '\n';
                                writeMsg(msg);
                            }
                            // if save
                            case 0 -> {
                                // do nothing
                            }
                        }
                    }
                }
            }
            // if out of the loop then disconnected and remove from client list
            removeThread(id);
            close();
        }

        // close everything
        private void close() {
            try {
                if(sOutput != null) sOutput.close();
                if(sInput != null) sInput.close();
                if(socket != null) socket.close();
            } catch (IOException e) {
                display(NOTIF + "Unable to disconnect " + username + NOTIF);
                display(e.toString());
            }
        }

        // write a String to the Client output stream
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                display(NOTIF + "Error sending message to " + username + NOTIF);
                display(e.toString());
            }
            return true;
        }
    }
    
    class ListenForClient extends Thread {     
        private final ServerSocket serverSocket;
        
        public ListenForClient(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }
        
        @Override
        public void run() {
            //create socket server and wait for connection requests 
            try {
                // infinite loop to wait for connections ( till server is active )
                while(keepGoing) {
                    display("Server waiting for Clients on port " + port + ".");
                    // accept connection if requested from client
                    Socket socket = serverSocket.accept();
                    // if client is connected, create its thread
                    ClientThread t = new ClientThread(socket);
                    //add this client to arraylist
                    clientThreads.add(t);

                    t.start();
                }                
            } catch (SocketException e) {
                String msg = "Server is successfully closed.";
                display(msg);
            } catch (IOException e) {
                String msg = "Exception on new ServerSocket: " + e;
                display(msg);
            }
        }
    }
}
