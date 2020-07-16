import com.umarabdul.jschat.JSChatServer;
import com.umarabdul.jschat.JSChatClient;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
* A launcher for the JSChat server and client.
* 
* @author Umar Abdul
* @version 1.0
* @since 2020
*/

public class Launcher extends JFrame implements ActionListener{

  private JButton serverBtn;
  private JButton clientBtn;
  private JButton quitBtn;

  public static void main(String args[]){

    Launcher launcher = new Launcher();
    launcher.start();
  }

  /**
  * Default constructor.
  */
  public Launcher(){
    super("Launcher - JSChat");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  /**
  * Create and render the launcher's window.
  */
  public void start(){

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
    GridLayout grid = new GridLayout(3, 1);
    grid.setVgap(8);
    JPanel buttonsPanel = new JPanel(grid);
    serverBtn = new JButton("JSChat Server");
    clientBtn = new JButton("JSChat Client");
    quitBtn = new JButton("Quit");
    serverBtn.addActionListener(this);
    clientBtn.addActionListener(this);
    quitBtn.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent event){
        System.exit(0);
      }
    });
    buttonsPanel.add(serverBtn);
    buttonsPanel.add(clientBtn);
    buttonsPanel.add(quitBtn);
    mainPanel.add(buttonsPanel);
    getContentPane().add(BorderLayout.CENTER, mainPanel);
    setBounds(300, 120, 280, 220);
    setResizable(false);
    setVisible(true);
  }

  /**
  * Handle client and server button used by the UI.
  * @param event ActionEvent containing the clicked button.
  */
  @Override
  public void actionPerformed(ActionEvent event){

    JButton src = (JButton)event.getSource();
    if (src == serverBtn){
      JSChatServer server = new JSChatServer();
      setVisible(false);
      server.launch();
    }else if (src == clientBtn){
      JSChatClient client = new JSChatClient();
      setVisible(false);
      client.launch();
    }
  }
}
