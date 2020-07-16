
  JSChat is a basic chat program written in Java using the Swing library for GUI.
It is written as a practical demo for the use of Swing library, multi-threading, event-
driven programming, data structures (HashMap and ArrayList), and socket programming.

  The JSChat server uses multi-threading to handle multiple clients at a time. Every
client must define a username and channel to connect to during authentication. Only
one user in a channel can use a particular username at a time, and a username can be
blocked from the server's admin panel.

  A channel, when active, is handled by a single thread, which loops through a list of
all the users using that particular channel, and broadcast any message that isn't a 
special query to all the users of the channel. This reduces overhead when many users
are connected to different channels since only a thread is required to handle a channel.

To build from source;

<<<<<<< HEAD
-  $ git clone https://github.com/UmarAbdul01/JSChat.git
-  $ cd JSChat
-  $ chmod +x install.sh
-  $ ./install.sh
-  $ java -jar jschat.jar
 
                                                                  Author: Umar Abdul
                                                                  
=======
   $ git clone https://github.com/UmarAbdul01/JSChat.git
   $ cd JSChat
   $ chmod +x install.sh
   $ ./install.sh
   $ java -jar jschat.jar

  
                                                                 Author: Umar Abdul
>>>>>>> d4371724494925bdeaadf9ceb2023c377b6d2c9f
