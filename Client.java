package com.umarabdul.jschat;

import java.net.*;
import java.io.*;


/**
* A class used for wrapping socket object of clients for easier management.
*
* @author Umar Abdul
* @version 1.0
* @since 2020
*/

public class Client{

  private Socket sock;
  private DataInputStream reader;
  private DataOutputStream writer;
  private int timeout = 50; // Default read timeout, in milliseconds. The lower, the more loop cycles executed in some places, the faster messages are exchanged.
  private String username = null;
  private long loginTime = 0;

  /**
  * Class constructor. Wraps the socket object and sets the default timeout.
  * @param sock A {@code Socket} object to wrap.
  * @throws IOException on {@code Socket.setSoTimeout(int)} failure.
  * @throws SocketException on data stream connection failure.
  */
  public Client(Socket sock) throws IOException, SocketException{
    
    sock.setSoTimeout(timeout);
    reader = new DataInputStream(sock.getInputStream());
    writer = new DataOutputStream(sock.getOutputStream());
    this.sock = sock;
  }

  /**
  * Change the wrapped socket's timeout.
  * @param timeout Socket timeout, in milliseconds.
  */
  public void setTimeout(int timeout){
    try{
      this.timeout = timeout;
      sock.setSoTimeout(timeout);
    }catch(Exception e){}
  }

  /**
  * Send the given data to the wrapped socket.
  * @param text String data to send.
  * @return {@code true} on success.
  */
  public boolean send(String text){
    
    try{
      writer.writeUTF(text);
      return true;
    }catch(Exception e){
      return false;
    }
  }

  /**
  * Receive data from the wrapped socket.
  * @return String data received, {@code ""} on timeout, {@code null} on failure.
  */
  public String receive(){

    try{
      return reader.readUTF();
    }catch(SocketTimeoutException e1){
      return "";
    }catch(Exception e2){
      return null;
    }
  }

  /**
  * Receive data from the wrapped socket object. Loop continously until data is found or connection is lost.
  * @return String data received, {@code null} on failure.
  */
  public String receiveWait(){

    while (true){
      try{
        return reader.readUTF();
      }catch(SocketTimeoutException e1){
        continue;
      }catch(Exception e2){
        return null;
      }
    }
  }

  /**
  * Return the wrapped socket object.
  * @return Socket object.
  */
  public Socket getSocket(){
    return sock;
  }

  /**
  * Return an instance of {@code DataInputStream} connected to the wrapped socket object.
  * @return An instnce of {@code DataInputStream}.
  */
  public DataInputStream getReader(){
    return reader;
  }

  /**
  * Return an instance of {@code DataOutputStream} connected to the wrapped socket object.
  * @return An instance of {@code DataOutputStream}.
  */
  public DataOutputStream getWriter(){
    return writer;
  }

  /**
  * Close the connection and associated data streams.
  */
  public void close(){

    try{
      reader.close();
      writer.close();
      sock.close();
    }catch(IOException e){}
  }

  /**
  * Assign a username to the wrapped socket. Called after authentication.
  * @param username Username to assign.
  */
  public void setUsername(String username){
    this.username = username;
  }

  /**
  * Return username assigned to the wrapped socket object.
  * @return Username assigned to the socket.
  * @see setUsername
  */
  public String getUsername(){
    return username;
  }

  /**
  * Record login time for the user of the wrapped socket.
  * @param loginTime Result of {@code System.currentTimeMillis()} at the time of user's successful authentication.
  */
  public void setLoginTime(long loginTime){
    this.loginTime = loginTime;
  }

  /**
  * Return the output of {@code System.currentTimeMillis()} at the time of user's successful authentication.
  * @return Timestamp at the time of user's successful authentication.
  * @see setLoginTime
  */
  public long getLoginTime(){
    return loginTime;
  }

  /**
  * Generate a formatted string indicating for how long the user has been authenticated.
  * @return String representing time elapsed since user's authentication.
  */
  public String computeLoginDuration(){
    
    long secs = (System.currentTimeMillis() - loginTime) / 1000;
    if (secs > (60 * 60))
      return String.format("%.1f hrs", (float)secs / (60*60));
    if (secs > 60)
      return String.format("%.1f mins", (float)secs / 60);
    return String.format("%.1f secs", (float)secs);
  }
}
