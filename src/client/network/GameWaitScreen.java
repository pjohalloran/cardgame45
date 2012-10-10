/*
 * Created on 21-Feb-2005
 */
package client.network;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

import client.graphics.PlayScreen;

import common.Menu;

/**
 * @author Pj O' Halloran
 * 
 */
public class GameWaitScreen extends Menu 
								implements ActionListener{

    private static final String[] rulesStr= { " - The Joker Card is", " - Stripping the deck is", " - Reneging is", 
            							" - Card play going left and cutting offered right is", " - Automatic Win for winning all 5 tricks in a round is", " - Dealing out 3 cards and then 2 is" };
    private static final int REFRESH_GAP = 2000;
    
    //A textarea to display information about the game while clients are waiting
    private JTextArea gameInfo;
    //A textarea to display information about the players, playing while the clients are waiting to start
    private JTextArea playerInfo;
    //A reference to the client object
    Client myClient;
    //True if i am debugging
    boolean debug;
    //A timer which sends a message to the server every 3 seconds to refresh the information on this GUI
    private Timer refreshTimer;
    //The main frame of the game screen
    private PlayScreen mainGame;
    
    public GameWaitScreen(Client client, boolean toDebug, PlayScreen p){
        super("Please Wait for Players to Join");
        
        myClient = client;
        debug = toDebug;
        mainGame = p;
        
        refreshTimer = new Timer(REFRESH_GAP, this);
        
        setUp();
    }
    
    /**
     * Sets up the Preliminary Game Wait Screen which displays the game
     * being played and the players who have joined.
     *
     */
    public void setUp() {
        super.setUp(400, 400, 350, 300);
        
        JPanel gameInfoPanel = new JPanel();
        gameInfoPanel.setMinimumSize(new Dimension(400, 150));
        gameInfoPanel.setPreferredSize(new Dimension(400, 150));
        
        JPanel playerInfoPanel = new JPanel();
        playerInfoPanel.setMinimumSize(new Dimension(400, 150));
        playerInfoPanel.setPreferredSize(new Dimension(400, 150));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setMaximumSize(new Dimension(400, 100));
        
        gameInfo = makeTextArea();
        JScrollPane scrollGame = new JScrollPane(gameInfo);
        scrollGame.setPreferredSize(new Dimension(350, 175));
        scrollGame.setMaximumSize(new Dimension(350, 175));
        scrollGame.setBorder(BorderFactory.createTitledBorder("Game Information"));
        
        playerInfo = makeTextArea();
        JScrollPane scrollPlayer = new JScrollPane(playerInfo);
        scrollPlayer.setPreferredSize(new Dimension(350, 175));
        scrollPlayer.setMaximumSize(new Dimension(350, 175));
        scrollPlayer.setBorder(BorderFactory.createTitledBorder("Players Joined Information"));
        
        JButton start = new JButton("Ready to Play");
        start.setToolTipText("Click this button when you want to indicate to the Server you are ready to start the game");
        start.addMouseListener(new MouseAdapter(){
			
			public void mouseClicked(MouseEvent e){
			    //TODO: Send message to the Server telling it that this Client is ready to play
			    if(!debug)
			        myClient.sendMessage("cReady");
			    else
			        System.out.println("cReady");
			    
//		        if(!debug)
//	            myClient.sendMessage("cName"+"," + myClient.getNickName());
//	        else
//	            System.out.println("cName"+"," + myClient.getNickName());
			    
			    //gameInfo.append("Client here wants to start! Game will start once all players are ready or when timeout expires.\n");
			    playerInfo.append("\nClient here wants to start! Game will start once all players are ready or when timeout expires.\n");
			}
		});
        
        JButton refresh = new JButton("Refresh");
        refresh.setToolTipText("Click this button to refresh the information on the game, Note it refreshes every 3 seconds anyway!");
        refresh.addMouseListener(new MouseAdapter(){
			
			public void mouseClicked(MouseEvent e){
			    refresh();
			}
		});

        gameInfoPanel.add(scrollGame);
        playerInfoPanel.add(scrollPlayer);
        buttonPanel.add(start);
        buttonPanel.add(refresh);
        super.cPane.add(gameInfoPanel);
        super.cPane.add(playerInfoPanel);
        super.cPane.add(buttonPanel);
    }
    
    /**
     * Makes and returns a JTextArea
     * 
     * @return
     */
    private JTextArea makeTextArea(){
        JTextArea txt = new JTextArea();
        
        txt.setEditable(false);
        txt.setFont(new Font("Serif", Font.BOLD, 16));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        
        return txt;
    }
    
    /**
     * Displays the menu and starts up the timer on the GameWaitScreen
     */
    public void display(){
        super.display();
        
      if(!debug)
          myClient.sendMessage("cName," + myClient.getNickName());
      else
          System.out.println("cName," + myClient.getNickName());
        
        if(!refreshTimer.isRunning())
            refreshTimer.start();
    }
    
    /**
     * Hides the panel and stops the refresh timer
     */
    public void hide(){
    	//refresh the panel just before it is hidden
    	refresh();
    	//hide the panel
        super.hide();
        //stop the timer
        if(refreshTimer.isRunning())
            refreshTimer.stop();
        
        //take care of the main game screen preliminary updates
        //mainGame.setOrderInfo(orderInfo);
        mainGame.setThisPlayersName(myClient.getNickName());
        mainGame.display();
    }
    
    /**
     * can recieve 2 messages
     * 1 "sGame=45,sGRules=100011"
     * 	or
     * 2 "sOrder,NAME,NAME,NAME" - "no_one" if no player at a position yet...
     * 
     * @param message
     */
    public void update(String message) {
        String[] temp = message.split("\\,");
        
        if(temp[0].startsWith("sGame")) {
            String[] game = temp[0].split("\\=");
            String[] rules = temp[1].split("\\=");
            
        	//clear the text in the JTextArea
        	gameInfo.replaceRange("", 0, gameInfo.getText().length());
            
            gameInfo.append("The game being played is " + game[1] + "\n\n");
            for(int i=0; i<rules[1].length(); i++) {
                gameInfo.append(rulesStr[i]);
                if(rules[1].charAt(i) == '0')
                    gameInfo.append(" not included\n");
                else
                    gameInfo.append(" included\n");
            }
        }
        else{
        	//sOrder message
            //clear the text in the JTextArea
            playerInfo.replaceRange("", 0, playerInfo.getText().length());
        	
            playerInfo.append("Players in the Game:\n\n");
            
            playerInfo.append(" - Player 1: "+myClient.getNickName()+"\n");
	        for(int i=1; i<temp.length; i++){
	        	if(temp[i].trim().equals("no_one"))
	        		playerInfo.append(" - Player "+(i+1)+": \n");
	        	else
	        		playerInfo.append(" - Player "+(i+1)+": "+temp[i]+"\n");
	        }
            
	        //Set the game player order on screen information message
            mainGame.setOrderInfo(message);
        }
    }
    
    /**
     * Sends a message to the Server to tell it to send back information to the Client
     * about the game
     *
     */
    public void refresh() {
	    if(!debug)
	        myClient.sendMessage("cGInfo");
	    else
	        System.out.println("cGInfo");
    }
    
    /**
     * Closes up the window
     */
    public void closeUp() {
        hide();
    }
    
    /**
     * Called by the timer roughly every 3 seconds
     */
    public void actionPerformed(ActionEvent ae){
        refresh();
    }
}