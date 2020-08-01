package com.umarabdul.jschat;

import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
* JSChat: A fully functional, multi-threaded chat server with support for multiple users and channels.
*
* @author Umar Abdul
* @version 1.0
* @since 2020
*/

public class JSChatServer extends JFrame implements ActionListener, Runnable{

  // Declaration of instance variables.
  private JTextField hostField;
  private JTextField portField;
  private JTextField channelAddField;
  private JTextField blockUserField;
  private JTextField unblockUserField;
  private JLabel channelCountLabel;
  private JLabel clientCountLabel;
  private JLabel blockCountLabel;
  private JButton serverBtn;
  private JButton addChannelBtn;
  private JButton showBlockUserBtn;
  private JButton unblockUserBtn;
  private JButton channelsBtn;
  private JButton manageChannelsBtn;
  private JButton quitBtn;
  private JButton blockUserBtn;
  private JButton saveConfigBtn;
  private JButton loadConfigBtn;
  private JLabel notification;
  private String host;
  private int port;
  private HashMap<String, ArrayList<Client>> channels; // Maps channel names to ArrayList of their clients.
  private ArrayList<String> blockedUsers;
  private boolean active = false;
  private int clientsCount = 0;
  private ArrayList<String> handledChannels;
  private HashMap<String, ArrayList<String>> channelBroadcast;
  private HashMap<String, String> channelPasswords;

  // Constants.
  public static final int MIN_USERNAME_LENGTH = 3; // minimum number of characters in a username.
  public static final int MAX_USERNAME_LENGTH = 20; // maximum number of characters in a username.
  public static final int MIN_CHANNEL_LENGTH = 3; // minimum channel name.
  public static final int MAX_CHANNEL_LENGTH = 20; // maximum channel name.
  public static final int MAX_CHANNEL_USERS = 500; // maximum number of users to allow per channel.


  /**
  * Default constructor.
  */
  public JSChatServer(){

    super("Admin Panel - JSChat");
    channels = new HashMap<String, ArrayList<Client>>();
    handledChannels = new ArrayList<String>();
    channelBroadcast = new HashMap<String, ArrayList<String>>();
    channelPasswords = new HashMap<String, String>();
    blockedUsers = new ArrayList<String>();
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  /**
  * Sets up and render the server's control panel window.
  * This is the starting point of the application when working in server mode.
  */
  public void launch(){
  
    hostField = new JTextField(15);
    portField = new JTextField(15);
    hostField.setText("localhost");
    portField.setText("4444");
    channelAddField = new JTextField(15);
    blockUserField = new JTextField(15);
    unblockUserField = new JTextField(15);
    channelCountLabel = new JLabel("0");
    clientCountLabel = new JLabel("0");
    blockCountLabel = new JLabel("0");
    serverBtn = new JButton("Start");
    addChannelBtn = new JButton("Add channel");
    blockUserBtn = new JButton("Block user");
    unblockUserBtn = new JButton("Unblock user");
    channelsBtn = new JButton("Show channels");
    manageChannelsBtn = new JButton("Manage channels");
    quitBtn = new JButton("Quit");
    showBlockUserBtn = new JButton("Blocked users");
    saveConfigBtn = new JButton("Save config");
    loadConfigBtn = new JButton("Load config");
    quitBtn.addActionListener(this);
    serverBtn.addActionListener(this);
    addChannelBtn.addActionListener(this);
    blockUserBtn.addActionListener(this);
    showBlockUserBtn.addActionListener(this);
    unblockUserBtn.addActionListener(this);
    channelsBtn.addActionListener(this);
    manageChannelsBtn.addActionListener(this);
    saveConfigBtn.addActionListener(this);
    loadConfigBtn.addActionListener(this);
    notification = new JLabel(" ");

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

    GridLayout grid = new GridLayout(8, 2);
    grid.setVgap(2);
    grid.setHgap(4);
    JPanel fieldsPanel = new JPanel(grid);
    fieldsPanel.add(new JLabel("Server host:"));
    fieldsPanel.add(hostField);
    fieldsPanel.add(new JLabel("Server port"));
    fieldsPanel.add(portField);
    fieldsPanel.add(addChannelBtn);
    fieldsPanel.add(channelAddField);
    fieldsPanel.add(blockUserBtn);
    fieldsPanel.add(blockUserField);
    fieldsPanel.add(unblockUserBtn);
    fieldsPanel.add(unblockUserField);
    fieldsPanel.add(new JLabel("Active channels:"));
    fieldsPanel.add(channelCountLabel);
    fieldsPanel.add(new JLabel("Active users:"));
    fieldsPanel.add(clientCountLabel);
    fieldsPanel.add(new JLabel("Blocked users:"));
    fieldsPanel.add(blockCountLabel);

    GridLayout grid2 = new GridLayout(7, 1);
    grid2.setVgap(8);
    JPanel buttonPanel = new JPanel(grid2);
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 80, 5, 80));
    buttonPanel.add(serverBtn);
    buttonPanel.add(channelsBtn);
    buttonPanel.add(manageChannelsBtn);
    buttonPanel.add(showBlockUserBtn);
    buttonPanel.add(loadConfigBtn);
    buttonPanel.add(saveConfigBtn);
    buttonPanel.add(quitBtn);

    JPanel notePanel = new JPanel();
    notePanel.add(notification);
    mainPanel.add(fieldsPanel);
    mainPanel.add(buttonPanel);
    mainPanel.add(notePanel);

    getContentPane().add(BorderLayout.CENTER, mainPanel);
    setBounds(300, 20, 350, 250);
    pack();
    setResizable(false);
    setVisible(true);
  }

  /**
  * Handles all the buttons in the server's control panel window.
  * @param event An ActionEvent generated by a {@code JButton} object.
  */
  @Override
  public void actionPerformed(ActionEvent event){
    
    JButton src = (JButton)event.getSource();
    notification.setText(" ");
    if (src == serverBtn){
      if (serverBtn.getText().equals("Stop") && active == true){ // Stop server and kill connection threads.
        active = false;
        serverBtn.setEnabled(false);
      }else if (serverBtn.getText().equals("Start") && active == false){ // Start the server.
        host = hostField.getText().trim();
        if (host.length() == 0){
          notification.setText("Hostname/IP required!");
          return;
        }
        try{
          port = Integer.parseInt(portField.getText());
          if (!(port > 0 && port < 65536))
            throw new NumberFormatException("port out of range!");
        }catch(NumberFormatException e){
          notification.setText("Invalid port!");
          return;
        }
        // Dispatch a thread to handle incoming connections.
        Thread t = new Thread(this);
        t.start();
      }
      serverBtn.setText((active == true) ? "Stop" : "Start");
    }else if (src == quitBtn){ // Kill server and exit.
      if (active){
        active = false;
        try{
          Thread.sleep(1000);
        }catch(InterruptedException e){}
      }
      System.exit(0);
    }else if (src == addChannelBtn){ // Create a new channel.
      channelAddField.requestFocus();
      String name = channelAddField.getText().trim();
      if (name.length() < MIN_CHANNEL_LENGTH){
        notification.setText("Channel name too short!");
        return;
      }
      if (!(isAlphaNum(name))){
        notification.setText("Invalid channel name!");
        return;
      }
      if (name.length() > MAX_CHANNEL_LENGTH){
        notification.setText("Channel name too long!");
        return;
      }
      if (channels.containsKey(name)){
        notification.setText("Channel already exists!");
        return;
      }
      channels.put(name, new ArrayList<Client>());
      notification.setText("Channel created!");
      channelPasswords.put(name, "null");
      channelAddField.setText("");
    }else if (src == blockUserBtn){ // Add a username to block list.
      blockUserField.requestFocus();
      String username = blockUserField.getText().trim();
      if (username.length() == 0){
        notification.setText("Target username required!");
        return;
      }
      if (blockedUsers.contains(username)){
        notification.setText("User already in block list!");
        return;
      }
      blockedUsers.add(username);
      notification.setText("User blocked!");
      blockUserField.setText("");
    }else if (src == unblockUserBtn){ // Remove a username from block list.
      unblockUserField.requestFocus();
      String username = unblockUserField.getText().trim();
      if (username.length() == 0){
        notification.setText("Target username required!");
        return;
      }
      if (!(blockedUsers.contains(username))){
        notification.setText("User not in block list!");
        return;
      }
      blockedUsers.remove(blockedUsers.indexOf(username));
      notification.setText("User unblocked!");
      unblockUserField.setText("");
    }else if (src == channelsBtn){ // Show available channels.
      if (channels.size() == 0){
        notification.setText("No channel available!");
        return;
      }
      showChannels();
    }else if (src == manageChannelsBtn){ // Manage available channels.
      if (channels.size() != 0)
        new ChannelManager();
      else
        notification.setText("No channel available!");
    }else if (src == showBlockUserBtn){ // Create a window listing blocked users.
      if (blockedUsers.size() == 0){
        notification.setText("Block list is empty!");
        return;
      }
      // Build table, setup GUI, and render.
      String rows[][] = new String[blockedUsers.size()][2];
      String cols[] = {"S/N", "Username"};
      for (int i = 0; i < blockedUsers.size(); i++){
        rows[i][0] = String.valueOf(i+1);
        rows[i][1] = blockedUsers.get(i);
      }
      createTableWindow("Blocked Users", rows, cols, null);      
    }else if (src == saveConfigBtn){ // Save server configurations.
      JFileChooser chooser = new JFileChooser();
      chooser.showSaveDialog(this);
      File file = chooser.getSelectedFile();
      if (file == null)
        return;
      try{
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(String.format("Host:%s\nPort:%s\n", hostField.getText().trim(), portField.getText().trim()));
        for (String ch : channels.keySet())
          writer.write(String.format("Channel:%s<<>>%s\n", ch, channelPasswords.get(ch)));
        for (String bl : blockedUsers)
          writer.write(String.format("Blocked:%s\n", bl));
        writer.close();
        notification.setText("Configurations saved!");
      }catch(IOException e){
        notification.setText("Error saving configurations!");
      }
    }else if (src == loadConfigBtn){ // Load server configurations.
      if (active){
        notification.setText("Stop server first!");
        return;
      }
      JFileChooser chooser = new JFileChooser();
      chooser.showOpenDialog(this);
      File file = chooser.getSelectedFile();
      if (file == null)
        return;
      hostField.setText("");
      portField.setText("");
      channelAddField.setText("");
      blockUserField.setText("");
      unblockUserField.setText("");
      channels.clear();
      try{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        String data[];
        while ((line = reader.readLine()) != null){
          data = line.split(":");
          if (data[0].equals("Host")){
            hostField.setText(data[1].trim());
          }else if (data[0].equals("Port")){
            portField.setText(data[1].trim());
          }else if (data[0].equals("Channel")){
            String[] channelData = data[1].trim().split("<<>>");
            String cname = channelData[0];
            String cpass = channelData[1];
            if (cname.length() >= MIN_CHANNEL_LENGTH && cname.length() <= MAX_CHANNEL_LENGTH && isAlphaNum(cname)){
              channels.put(cname, new ArrayList<Client>());
              channelPasswords.put(cname, cpass);
            }
          }else if (data[0].equals("Blocked")){
            blockedUsers.add(data[1].trim());
          }
        }
        reader.close();
        notification.setText("Configuration loaded!");
      }catch(Exception e){
        e.printStackTrace();
        notification.setText("Error loading configurations!");
      }
    }
    updateCountLabels();
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

  /**
  * Update display for the count of active clients, blocked users, and active channels.
  */
  public void updateCountLabels(){
    
    blockCountLabel.setText(String.valueOf(blockedUsers.size()));
    clientsCount = 0;
    for (String name : channels.keySet())
      clientsCount += channels.get(name).size();
    clientCountLabel.setText(String.valueOf(clientsCount));
    channelCountLabel.setText(String.valueOf(channels.size()));
  }

  /**
  * Delete a channel and disconnect all of it's clients.
  * @param name Name of channel to delete.
  */
  public void deleteChannel(String name){

    ArrayList<Client> clients = channels.remove(name);
    channelPasswords.remove(name);
    for (Client client : clients){
      client.close();
    }
    handledChannels.remove(name);
  }

  /**
  * Disconnect all users in a channel, without deleting the channel itself.
  * @param name Name of channel.
  */
  public void deleteChannelUsers(String name){

    ArrayList<Client> clients = channels.get(name);
    if (clients == null)
      return;
    Client client;
    for (int i = 0; i < clients.size(); i++){
      client = clients.remove(i);
      client.close();
    }
    clients.clear();
  }

  /**
  * Check if the given channel contains the given username as an active user.
  * @param channel Name of channel.
  * @param username Username to check for.
  * @return {@code true} if a match is found.
  */
  public boolean channelUserExists(String channel, String username){

    ArrayList<Client> clients = channels.get(channel);
    if (clients == null)
      return false;
    Client client;
    for (Client c : clients){
      if (c.getUsername().equals(username))
        return true;
    }
    return false;
  }

  /**
  * Display a window showing available channels and their users.
  */
  public void showChannels(){

    String rows[][] = new String[channels.size()][3];
    String names[] = new String[channels.size()];
    int j = 0;
    for (String n : channels.keySet()){
      names[j] = n;
      j++;
    }
    for (int i = 0; i < channels.size(); i++){
      rows[i][0] = String.valueOf(i+1);
      rows[i][1] = names[i];
      rows[i][2] = String.valueOf(channels.get(names[i]).size());
    }

    String columns[] = {"S/N", "Channel Name", "Channel Users"};
    // Create a click listener.
    MouseAdapter listener = new MouseAdapter(){

      @Override
      public void mouseClicked(MouseEvent event){

        JTable src = (JTable)event.getSource();
        int row = src.getSelectedRow();
        try{
          String cname = (String)src.getValueAt(row, 1); // Get channel name of selected row.
          ArrayList<Client> clients = channels.get(cname);
          if (clients.size() == 0)
            return;
          // Build the users table.
          String rows[][] = new String[clients.size()][3];
          String cols[] = {"S/N", "Username", "Active for"};
          Client c;
          for (int i = 0; i < clients.size(); i++){
            c = clients.get(i);
            rows[i][0] = String.valueOf(i+1);
            rows[i][1] = c.getUsername();
            rows[i][2] = c.computeLoginDuration();
          }
          // Render.
          createTableWindow(cname, rows, cols, null);
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    };
    // Render.
    createTableWindow("Available Channels", rows, columns, listener);
  }

  /**
  * Handles server connections.
  */
  @Override
  public void run(){

    serverBtn.setEnabled(false);
    ServerSocket sock = null;
    try{
      sock = new ServerSocket(port, 100, InetAddress.getByName(host));
    }catch(Exception e){
      notification.setText("Error starting server!");
      serverBtn.setEnabled(true);
      return;
    }
    try{
      sock.setSoTimeout(500);
    }catch(SocketException e1){}
    notification.setText("Server started!");
    serverBtn.setText("Stop");
    serverBtn.setEnabled(true);
    active = true;
    Socket client;
    Thread t;
    String quitMsg = null;
    while (active){
      try{
        client = sock.accept();
        t = new Thread(new ConnHandler(client));
        t.start();
      }catch(SocketTimeoutException e2){
        continue;
      }catch(IOException e3){
        // Kill server.
        e3.printStackTrace();
        active = false;
        quitMsg = "Server error!";
        break;
      }
    }
    // Handle server shutdown.
    try{
      sock.close();
    }catch(IOException e4){}
    for (String name : channels.keySet())
      deleteChannelUsers(name);
    handledChannels.clear();
    updateCountLabels();
    serverBtn.setText("Start");
    serverBtn.setEnabled(true);
    if (quitMsg == null)
      quitMsg = "Server stopped!";
    notification.setText(quitMsg);
  }

  /**
  * Handles a freshly accepted connection, authenticate it, and pass it to a channel handler.
  */
  public class ConnHandler implements Runnable{

    private Socket sock;

    public ConnHandler(Socket sock){
      this.sock = sock;
    }

    @Override
    public void run(){

      Client client = null;
      try{
        client = new Client(sock);
      }catch(Exception e){
        e.printStackTrace();
        client.close();
        return;
      }
      client.setTimeout(5000);
      String data = client.receive();
      if (data == null || data.length() == 0){
        client.close();
        return;
      }
      try{
        String params[] = data.split("<<>>");
        if (params.length != 3){
          client.close();
          return;
        }
        String username = params[0].trim();
        String channel = params[1].trim();
        String password = params[2].trim();
        if (!(channels.containsKey(channel))){
          client.send("Invalid channel!");
          client.close();
          return;
        }
        if (channels.get(channel).size() >= MAX_CHANNEL_USERS){
          client.send("Maximum channel users attained!");
          client.close();
          return;
        }
        if (isAlphaNum(username) != true || username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH || channelUserExists(channel, username) || blockedUsers.contains(username)){
          client.send("Username taken/invalid");
          client.close();
          return;
        }
        String orgPass = channelPasswords.get(channel);
        if (orgPass != null && orgPass.length() != 0){
          if (!(orgPass.equals(password))){
            client.send("Authentication failed!");
            client.close();
            return;
          }
        }
        client.setLoginTime(System.currentTimeMillis());
        client.setUsername(username);
        client.send("[+]");
        client.setTimeout(50); // Restore low timeout for faster loop circles.
        channels.get(channel).add(client);
        updateCountLabels();
        // Dispatch a thread for the channel if none is available!
        if (!(channelBroadcast.containsKey(channel)))
            channelBroadcast.put(channel, new ArrayList<String>());
        if (!(handledChannels.contains(channel))){
          handledChannels.add(channel);
          Thread t = new Thread(new ChannelHandler(channel));
          t.start();
        }
        channelBroadcast.get(channel).add(String.format("[NOTIFICATION] : %s has joined!", username));
      }catch(Exception e){
        e.printStackTrace();
        client.close();
        return;
      }
    }
  }

  /**
  * A thread is started with a new instance of this to manage every channel created.
  */
  public class ChannelHandler implements Runnable{

    private String channelName;

    public ChannelHandler(String name){
      channelName = name;
    }

    @Override
    public void run(){
      
      ArrayList<Client> clients = channels.get(channelName);
      ArrayList<String> broadcasts = channelBroadcast.get(channelName);
      Client client;
      String msg;
      String username;
      int i;
      while (active && channels.containsKey(channelName)){
        if (clients.size() == 0){ // quit the thread when the channel has no active user.
          handledChannels.remove(handledChannels.indexOf(channelName));
          return;
        }
        for (i = 0; (i < clients.size() && active); i++){
          client = clients.get(i);
          msg = client.receive();
          username = client.getUsername();
          if (blockedUsers.contains(username)){
            clients.remove(i);
            client.close();
            broadcasts.add(String.format("[NOTIFICATION] : %s has been blocked!", username));
            updateCountLabels();
            continue;
          }
          if (msg == null || msg.equals("[quit]")){
            broadcasts.add(String.format("[NOTIFICATION] : %s has left!", username));
            client.close();
            clients.remove(client);
            updateCountLabels();
          }else if (msg.equals("[users]")){
            Client cl;
            msg = "\n            Channel Users\n            -------------\n\n";
            for (i = 0; i < clients.size(); i++){
              cl = clients.get(i);
              msg += String.format("  %03d   -   %-16s  (active for: %s)\n", i+1, cl.getUsername(), cl.computeLoginDuration());
            }
            client.send(msg);
          }else if (msg.length() > 0){
            msg = String.format("(%s) > %s", username, msg.trim());
            for (Client r : clients)
              r.send(msg);
          }
          // Check for broadcasts.
          while (broadcasts.size() != 0){
            msg = broadcasts.remove(0);
            for (Client r : clients)
              r.send(msg);
          }
        }
      }
    }
  }

  /**
  * A window for managing channels.
  */
  public class ChannelManager implements ActionListener{

    private JButton deleteBtn;
    private JButton passwdBtn;
    private JButton closeBtn;
    private JTextField passwdField;
    private JComboBox<String> channelsCombo;
    private boolean pauseComboListener = false;
    private JFrame frame;

    public ChannelManager(){

      frame = new JFrame("Channels - JSChat");
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
      passwdBtn = new JButton("Set password");
      deleteBtn = new JButton("Delete");
      closeBtn = new JButton("Close");
      passwdField = new JTextField(10);
      channelsCombo = new JComboBox<String>();
      JPanel comboPanel = new JPanel();
      comboPanel.add(new JLabel("Channel:"));
      if (channels.size() == 0){
        passwdBtn.setEnabled(false);
      }else{
        for (String c : channels.keySet())
          channelsCombo.addItem(c);
      }
      passwdBtn.setEnabled(true);

      channelsCombo.addActionListener(new ActionListener(){
        
        @Override
        public void actionPerformed(ActionEvent event){

          if (pauseComboListener)
            return;
          String cname = (String)channelsCombo.getSelectedItem();
          if (cname == null)
            return;
          if (!(channels.containsKey(cname))){
            passwdBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
            return;
          }
          passwdBtn.setEnabled(true);
          deleteBtn.setEnabled(true);
          String currPass = channelPasswords.get(cname);
          passwdField.setText(currPass);
          passwdField.requestFocus();
        }
      });
      comboPanel.add(channelsCombo);
      passwdBtn.addActionListener(this);
      deleteBtn.addActionListener(this);
      closeBtn.addActionListener(this);

      JPanel passwdPanel = new JPanel();
      passwdPanel.add(passwdField);
      passwdPanel.add(passwdBtn);
      JPanel btnPanel = new JPanel();
      btnPanel.add(deleteBtn);
      btnPanel.add(closeBtn);

      mainPanel.add(comboPanel);
      mainPanel.add(passwdPanel);
      mainPanel.add(btnPanel);
      passwdField.requestFocus();

      frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
      frame.getRootPane().setDefaultButton(passwdBtn);
      frame.setBounds(300, 100, 300, 300);
      frame.pack();
      frame.setResizable(false);
      frame.setVisible(true);

      String cname = (String)channelsCombo.getSelectedItem();
      if (cname != null){
        String passwd = channelPasswords.get(cname);
        passwdField.setText(passwd);
      }
    }

    @Override
    public void actionPerformed(ActionEvent event){
    
      JButton src = (JButton)event.getSource();
      String channelName = (String)channelsCombo.getSelectedItem();
      if (channelName == null && src != closeBtn)
        return;
      if (src == closeBtn){
        updateCountLabels();
        frame.setVisible(false);
      }else if (src == passwdBtn){
        String passwd = (String)passwdField.getText();
        channelPasswords.put(channelName, passwd);
      }else if (src == deleteBtn){
        pauseComboListener = true;
        deleteChannel(channelName);
        channelsCombo.removeAllItems();
        for (String s : channels.keySet())
          channelsCombo.addItem(s);
        if (channels.size() == 0)
          frame.setVisible(false);
        updateCountLabels();
        channelName = (String)channelsCombo.getSelectedItem();
        if (channelName != null)
          passwdField.setText(channelPasswords.get(channelName));
        pauseComboListener = false;
      }
    }
  }

  /**
  * Create and render a window of a JTable containing the given data.
  * @param title Window title.
  * @param rows Table rows.
  * @param cols Table columns.
  * @param listener An instance of {@code MouseAdapter} to use as click listener. {@code null} to disable.
  */
  public void createTableWindow(String title, String rows[][], String cols[], MouseAdapter listener){

    JFrame tFrame = new JFrame(title);
    JPanel mainPanel = new JPanel();
    JButton closeBtn = new JButton("Close");
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    JTable table = new JTable(rows, cols);
    if (listener != null)
      table.addMouseListener(listener);
    closeBtn.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent event){
        tFrame.setVisible(false);
      }
    });
    JScrollPane scroller = new JScrollPane(table);
    scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    mainPanel.add(scroller);
    JPanel btnPanel = new JPanel();
    btnPanel.add(closeBtn);
    mainPanel.add(btnPanel);

    tFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);
    tFrame.getRootPane().setDefaultButton(closeBtn);
    tFrame.setBounds(250, 40, 300, 200);
    tFrame.pack();
    tFrame.setResizable(false);
    tFrame.setVisible(true);
  }
}
