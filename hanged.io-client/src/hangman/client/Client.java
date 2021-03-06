/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman.client;

import hangman.server.ChatMessage;
import hangman.server.Server;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author leonh
 */
public class Client extends javax.swing.JFrame {
    // notification
    private static final String NOTIF = " *** ";

    // for I/O
    private ObjectInputStream sInput;           // to read from the socket
    private ObjectOutputStream sOutput;         // to write on the socket
    private Socket socket;                      // socket object

    private String serverAddress;               // server and username

    private String username;                    // server and username
    private int port;                           //port

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Creates new form ClientGUI
     */
    public Client() {
        this.port = 15000;
        this.serverAddress = "localhost";
        this.username = "Anonymous";
        initComponents();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
                disconnect();
            }
        });
    }
    
    /**
     * Creates new form ClientGUI
     * @param serverAddress
     * @param port
     * @param username
     */
    public Client(String serverAddress, int port, String username) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
        initComponents();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
                disconnect();
            }
        });
    }
    
    public boolean start() {
        // try to connect to the server
        try {
            socket = new Socket(serverAddress, port);
        } 
        // exception handler if it failed
        catch(IOException e) {
            display("Error connectiong to server:" + e);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        /* Creating both Data Stream */
        try {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            display("Exception creating new Input/output Streams: " + e);
            return false;
        }

        // creates the Thread to listen from the server 
        new ListenFromServer().start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be ChatMessage objects
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            display("Exception doing login : " + e);
            disconnect();
            return false;
        }
        // success we inform the caller that it worked
        return true;
    }

    /*
     * To send a message to the console
     */
    private void display(String msg) {
        System.out.println(msg);
    }

    /*
     * To send a message to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    /*
     * When something goes wrong
     * Close the Input/Output streams and disconnect
     */
    private void disconnect() {
        try { 
            if(sInput != null) sInput.close();
            if(sOutput != null) sOutput.close();
            if(socket != null) socket.close();
        } catch(IOException e) {
            display("Exception closing resources: " + e);
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

        gameFrame = new javax.swing.JFrame();
        gamePanel = new javax.swing.JPanel();
        chatScroll = new javax.swing.JScrollPane();
        chatBox = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        gameBox = new javax.swing.JTextArea();
        chatText = new javax.swing.JTextField();
        sendButton = new javax.swing.JButton();
        gameTitle = new javax.swing.JTextField();
        loginPanel = new javax.swing.JPanel();
        usernameText = new javax.swing.JTextField();
        confirmButton = new javax.swing.JButton();

        gameFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        gameFrame.setTitle("Hanged.io");
        gameFrame.setMinimumSize(new java.awt.Dimension(650, 365));
        gameFrame.setResizable(false);

        chatScroll.setFocusable(false);

        chatBox.setEditable(false);
        chatBox.setColumns(20);
        chatBox.setLineWrap(true);
        chatBox.setRows(5);
        chatBox.setWrapStyleWord(true);
        chatBox.setFocusable(false);
        chatScroll.setViewportView(chatBox);

        jScrollPane2.setFocusable(false);

        gameBox.setEditable(false);
        gameBox.setColumns(20);
        gameBox.setFont(new java.awt.Font("Monospaced", 0, 16)); // NOI18N
        gameBox.setLineWrap(true);
        gameBox.setRows(5);
        gameBox.setText("*** -------- ***\n\n--------\n|      |\n|      O\n|     /|\\\n|      |\n|     / \\\n|");
        gameBox.setWrapStyleWord(true);
        gameBox.setFocusable(false);
        jScrollPane2.setViewportView(gameBox);

        chatText.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        chatText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                chatTextKeyPressed(evt);
            }
        });

        sendButton.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        sendButton.setText("Send");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        gameTitle.setEditable(false);
        gameTitle.setBackground(new java.awt.Color(255, 255, 255));
        gameTitle.setFont(new java.awt.Font("Monospaced", 0, 24)); // NOI18N
        gameTitle.setText("                Hanged.io");
        gameTitle.setFocusable(false);

        javax.swing.GroupLayout gamePanelLayout = new javax.swing.GroupLayout(gamePanel);
        gamePanel.setLayout(gamePanelLayout);
        gamePanelLayout.setHorizontalGroup(
            gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameTitle)
                    .addGroup(gamePanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(gamePanelLayout.createSequentialGroup()
                                .addComponent(chatText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(chatScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE))))
                .addContainerGap())
        );
        gamePanelLayout.setVerticalGroup(
            gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gameTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gamePanelLayout.createSequentialGroup()
                        .addComponent(chatScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chatText, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );

        javax.swing.GroupLayout gameFrameLayout = new javax.swing.GroupLayout(gameFrame.getContentPane());
        gameFrame.getContentPane().setLayout(gameFrameLayout);
        gameFrameLayout.setHorizontalGroup(
            gameFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gameFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gamePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        gameFrameLayout.setVerticalGroup(
            gameFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gameFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gamePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hanged.io");
        setAutoRequestFocus(false);
        setResizable(false);

        usernameText.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        usernameText.setForeground(Color.GRAY);
        usernameText.setText("Username");
        usernameText.setToolTipText("");
        usernameText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (usernameText.getText().equals("Username")) {
                    usernameText.setText("");
                    usernameText.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (usernameText.getText().isEmpty()) {
                    usernameText.setForeground(Color.GRAY);
                    usernameText.setText("Username");
                }
            }
        });
        usernameText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                usernameTextKeyPressed(evt);
            }
        });

        confirmButton.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        confirmButton.setText("OK");
        confirmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout loginPanelLayout = new javax.swing.GroupLayout(loginPanel);
        loginPanel.setLayout(loginPanelLayout);
        loginPanelLayout.setHorizontalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(usernameText, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(confirmButton, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                .addContainerGap())
        );
        loginPanelLayout.setVerticalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(confirmButton, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(usernameText))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 385, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(loginPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 79, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(9, 9, 9)
                    .addComponent(loginPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void confirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmButtonActionPerformed
        login();
    }//GEN-LAST:event_confirmButtonActionPerformed

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        chat();
    }//GEN-LAST:event_sendButtonActionPerformed

    private void chatTextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_chatTextKeyPressed
        if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
            chat();
    }//GEN-LAST:event_chatTextKeyPressed

    private void usernameTextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernameTextKeyPressed
        if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
            login();
    }//GEN-LAST:event_usernameTextKeyPressed

    private void login() {
        if (this.usernameText.getText() != null && !this.usernameText.getText().equalsIgnoreCase("") && !this.usernameText.getText().equalsIgnoreCase("username"))
            setUsername(this.usernameText.getText());
        else
            setUsername("Anonymous");
        
        this.port = 15000;
        this.serverAddress = "localhost";
        
        this.dispose();
        gameFrame.setVisible(true);
        
        if(!this.start()) {
            this.disconnect();
            System.exit(0);
        }
    }
    
    private void chat() {
        String msg = this.chatText.getText();
        this.chatText.setText("");
        
        if(msg != null && !msg.equalsIgnoreCase("")) {
            // logout if message is LOGOUT
            if(msg.equalsIgnoreCase("!LOGOUT")) {
                this.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
                this.disconnect();
                System.exit(0);
            }
            // regular text message
            else {
                this.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
            }
        }
        
        this.chatText.requestFocus();
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
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea chatBox;
    private javax.swing.JScrollPane chatScroll;
    private javax.swing.JTextField chatText;
    private javax.swing.JButton confirmButton;
    private javax.swing.JTextArea gameBox;
    private javax.swing.JFrame gameFrame;
    private javax.swing.JPanel gamePanel;
    private javax.swing.JTextField gameTitle;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextField usernameText;
    // End of variables declaration//GEN-END:variables
    
    class ListenFromServer extends Thread {        
        @Override
        public void run() {
            while(true) {
                try {
                    // read the message form the input datastream
                    String msg = (String) sInput.readObject();
                    // print the message
                    System.out.print("> ");
                    System.out.println(msg);
                    
                    boolean found = false;
                    for (String stage : Server.hangmanStage) {
                        if(msg.contains(stage)) {
                            String[] mess = msg.split("  ", 2);
                            gameBox.setText(mess[1]);
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        chatBox.append("> " + msg);
                        chatBox.setCaretPosition(chatBox.getText().length());
                    }
                } catch(IOException | ClassNotFoundException e) {
                    display(NOTIF + "Server has closed the connection: " + e + NOTIF);
                    break;
                }
            }
        }
    }
}
