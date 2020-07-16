package com.umarabdul.jschat;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
* Client-side code for the JSChat program.
*
* @author Umar Abdul
* @version 1.0
* @since 2020
*/

public class JSChatClient extends JFrame implements ActionListener, Runnable{

  // Declaration of instances.
  private JFrame loginFrame;
  private JTextField hostField;
  private JTextField portField;
  private JTextField usernameField;
  private JTextField channelField;
  private String host;
  private int port;
  private String username;
  private String channel;
  private JLabel notification;
  private JLabel handlerNotification;
  private JTextArea messages;
  private JScrollPane msgScroller;
  private JTextField textField;
  private JButton connectBtn;
  private JButton sendBtn;
  private JButton clearBtn;
  private JButton showUsersBtn;
  private JButton pauseBtn;
  private JButton saveLogsBtn;
  private JButton quitBtn;
  private boolean active = false;
  private Client clientObj;
  private ArrayList<String> messageQueue;
  private boolean pause = false;


  /**
  * Default constructor.
  */
  public JSChatClient(){
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  /**
  * Setup and display the main window used for connecting to a chat server.
  */
  public void launch(){
    
    loginFrame = new JFrame("JSChat Client");
    loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

    hostField = new JTextField(15);
    portField = new JTextField(15);
    channelField = new JTextField(15);
    usernameField = new JTextField(15);
    connectBtn = new JButton("Connect");
    connectBtn.addActionListener(new ConnListener());
    JButton quitBtn = new JButton("Quit");
    quitBtn.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent event){
        System.exit(0);
      }
    });

    GridLayout grid = new GridLayout(4, 2);
    grid.setVgap(2);
    grid.setHgap(1);
    JPanel fieldsPanel = new JPanel(grid);
    fieldsPanel.add(new JLabel("Server IP:"));
    fieldsPanel.add(hostField);
    fieldsPanel.add(new JLabel("Server port:"));
    fieldsPanel.add(portField);
    portField.setText("4444");
    fieldsPanel.add(new JLabel("Username:"));
    fieldsPanel.add(usernameField);
    fieldsPanel.add(new JLabel("Channel name:"));
    fieldsPanel.add(channelField);
    
    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    buttonsPanel.add(connectBtn);
    buttonsPanel.add(quitBtn);
    JPanel errorPanel = new JPanel();
    notification = new JLabel(" ");
    errorPanel.add(notification);

    mainPanel.add(fieldsPanel);
    mainPanel.add(buttonsPanel);
    mainPanel.add(errorPanel);
    
    loginFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);
    loginFrame.getRootPane().setDefaultButton(connectBtn);
    loginFrame.setBounds(300, 100, 260, 280);
    loginFrame.pack();
    loginFrame.setResizable(false);
    loginFrame.setVisible(true);
  }
 
  /**
  * Handles the "connect" button.
  */
  public class ConnListener implements ActionListener{

    @Override
    public void actionPerformed(ActionEvent event){

      notification.setText(" ");
      host = hostField.getText();
      try{
        port = Integer.parseInt(portField.getText());
        if (!(port > 0 && port < 65536))
          throw new NumberFormatException("port out of range!");
      }catch(NumberFormatException e){
        notification.setText("Invalid port!");
        return;
      }
      channel = channelField.getText().trim();
      username = usernameField.getText().trim();
      if (host.length() == 0 || channel.length() == 0 || username.length() == 0){
        notification.setText("Required fields not defined!");
        return;
      }
      if (!(isAlphaNum(username))){
        notification.setText("Invalid username!");
        return;
      }
      if (!(isAlphaNum(channel))){
        notification.setText("Invalid channel name!");
        return;
      }
      connectBtn.setEnabled(false);
      Thread t = new Thread(new Connector());
      t.start();
    }
  }

  /**
  * Creates a connection with the chat server.
  */
  public class Connector implements Runnable{

    @Override
    public void run(){

      notification.setText("Connecting to server...");
      try{
        Socket conn = new Socket(host, port);
        clientObj = new Client(conn);
        clientObj.setTimeout(5000);
        notification.setText("Authenticating...");
        clientObj.send(String.format("%s<<>>%s", username, channel));
        String rsp = clientObj.receive();
        if (rsp == null){
          clientObj.close();
          notification.setText("Connection lost!");
          connectBtn.setEnabled(true);
          return;
        }
        else if (rsp.length() == 0){
          clientObj.close();
          notification.setText("Connection timed out!");
          connectBtn.setEnabled(true);
          return;
        }else{
          if (rsp.equals("[+]")){
            notification.setText("Connection established!");
            loginFrame.setVisible(false);
          }else{
            notification.setText(rsp);
            connectBtn.setEnabled(true);
            return;
          }
        }
      }catch(Exception e){
        notification.setText("Connection failed!");
        connectBtn.setEnabled(true);
        return;
      }
      active = true;
      mainHandler();
    }
  }

  /**
  * Provides the UI for interacting with the server after connecting.
  */
  private void mainHandler(){

    setTitle(String.format("%s@%s", username, channel));
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    messages = new JTextArea(20, 50);
    messages.setLineWrap(true);
    messages.setWrapStyleWord(true);
    messages.setEditable(false);
    messages.setFont(new Font("monospaced", Font.PLAIN, 14));
    msgScroller = new JScrollPane(messages);
    msgScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    msgScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    textField = new JTextField(30);
    sendBtn = new JButton("Send");
    clearBtn = new JButton("Clear logs");
    showUsersBtn = new JButton("Show users");
    pauseBtn = new JButton("Pause");
    saveLogsBtn = new JButton("Save logs");
    quitBtn = new JButton("Quit");
    sendBtn.addActionListener(this);
    clearBtn.addActionListener(this);
    quitBtn.addActionListener(this);
    showUsersBtn.addActionListener(this);
    pauseBtn.addActionListener(this);
    saveLogsBtn.addActionListener(this);
    handlerNotification = new JLabel(" ");
    messageQueue = new ArrayList<String>();

    JPanel inputPanel = new JPanel();
    inputPanel.add(textField);
    inputPanel.add(sendBtn);
    inputPanel.add(clearBtn);
    JPanel bottomPanel = new JPanel();
    bottomPanel.add(showUsersBtn);
    bottomPanel.add(pauseBtn);
    bottomPanel.add(saveLogsBtn);
    bottomPanel.add(quitBtn);
    JPanel notePanel = new JPanel();
    notePanel.add(handlerNotification);
    
    mainPanel.add(msgScroller);
    mainPanel.add(inputPanel);
    mainPanel.add(bottomPanel);
    mainPanel.add(notePanel);

    getContentPane().add(BorderLayout.CENTER, mainPanel);
    getRootPane().setDefaultButton(sendBtn);
    setBounds(100, 80, 800, 500);
    setVisible(true);
    textField.requestFocus();

    Thread t = new Thread(this);
    t.start();
  }

  /**
  * Handles all the buttons used in the chatting UI.
  * @param event ActionEvent generate by a {@code JButton}.
  */
  @Override
  public void actionPerformed(ActionEvent event){

    textField.requestFocus();
    handlerNotification.setText(" ");
    JButton src = (JButton)event.getSource();
    if (src == sendBtn){ // Send text entered.
      if (pause)
        return;
      String msg = textField.getText().trim(); // Obtain text, clean trailing whitespaces.
      if (msg.length() != 0)
        messageQueue.add(msg);
      textField.setText("");
    }else if (src == clearBtn){ // Clear message log.
      messages.setText("");
    }else if (src == quitBtn){ // Close connection and quit the app.
      active = false;
      clientObj.send("[quit]");
      clientObj.close();
      System.exit(0);
    }else if (src == showUsersBtn && active){ // Request for user listing.
      messageQueue.add("[users]");
    }else if (src == pauseBtn){
      // Pause or resume channel stream for the user. Incoming messages are queued while outgoing ones are discarded.
      if (pauseBtn.getText().equals("Pause")){
        pause = true;
        pauseBtn.setText("Resume");
        sendBtn.setEnabled(false);
        showUsersBtn.setEnabled(false);
        handlerNotification.setText("Channel stream paused!");
      }else{
        pause = false;
        pauseBtn.setText("Pause");
        sendBtn.setEnabled(true);
        showUsersBtn.setEnabled(true);
        handlerNotification.setText("Channel stream resumed!");
      }
    }else if (src == saveLogsBtn){
      String log = messages.getText();
      if (log.length() == 0){
        handlerNotification.setText("No message to log!");
        return;
      }
      JFileChooser chooser = new JFileChooser();
      chooser.showSaveDialog(this);
      File file = chooser.getSelectedFile();
      if (file != null){
        try{
          BufferedWriter writer = new BufferedWriter(new FileWriter(file));
          writer.write(log);
          writer.close();
          handlerNotification.setText("Logs saved successfully!");
        }catch(IOException e){
          handlerNotification.setText("Error saving logs!");
        }
      }
    }
  }

  /**
  * Handles all incoming and outgoing messages.
  */
  @Override
  public void run(){

    String msg;
    JScrollBar sbar = msgScroller.getVerticalScrollBar();
    clientObj.setTimeout(50);
    LocalDateTime dateObj;
    DateTimeFormatter pattern = DateTimeFormatter.ofPattern("HH:mm:ss");
    while (active){
      if (pause){
        try{
          Thread.sleep(200);
        }catch(InterruptedException e){}
        continue;
      }
      while (messageQueue.size() > 0 && active){ // Send messages composed by user.
        msg = messageQueue.remove(0).trim();
        if (clientObj.send(msg) == false){
          handlerNotification.setText("Connection lost!");
          sendBtn.setEnabled(false);
          active = false;
          return;
        }
      }
      // Check for incoming messages.
      msg = clientObj.receive();
      if (msg == null){
        handlerNotification.setText("Connection lost!");
        sendBtn.setEnabled(false);
        active = false;
        return;
      }
      if (msg.length() != 0){ // Valid message received.
        dateObj = LocalDateTime.now();
        messages.append(dateObj.format(pattern)+" "+msg+"\n");
        sbar.setValue(sbar.getMaximum()); // Scroll to bottom.
      }
      // Repeat ;)
    }
  }

  /**
  * Test if a given string contains only alphabets (upper and lowercase) and whitespace.
  * @param str String to test.
  * @return {@code true} on success.
  */
  public boolean isAlphaNum(String str){

    Pattern pattern = Pattern.compile("^[A-Za-z0-9 ]{1,9999}$"); // The regex rule in use.
    Matcher match = pattern.matcher(str);
    return match.find();
  }
}
