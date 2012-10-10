/*
 * Created on 21-Feb-2005
 */
package client.network;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import common.Menu;

/**
 * @author Pj O' Halloran
 * 
 * (Use Case 4)
 */
public class GameMenu extends Menu{
    
    //Group of radio buttons for picking the game
    private JRadioButton button41;
    private JRadioButton button45;
    private JRadioButton button110;
    private ButtonGroup gameGroup;
    
    //Checkbox buttons for customizing the game
    private JCheckBox joker;
    private JCheckBox strip;
    private JCheckBox reneging;
    private JCheckBox autowin;
    private JCheckBox cutRightPlayLeft;
    private JCheckBox deal_3_2;
    
    //The message to be sent to the Server, which contains information about the game to be played and its rules
    private String gameMessage;
    //A reference to the client object
    private Client myClient;
    //Debug mode marker
    boolean debug;
    
    /**
     * Creates a Game Setup Menu
     *
     */
    public GameMenu(Client c, boolean d){
        super("Please pick the Game and its rules");
        gameMessage = "";
        debug = d;
        myClient = c;
        setUp();
    }
    
    /**
     * Sets up the Game Menu GUI
     * (Use Case 4,1)
     *
     */
    public void setUp(){
        Dimension gamePanelSize = new Dimension(350, 200);
        
        super.setUp(300, 300, gamePanelSize.width, gamePanelSize.height);
        
		super.menu.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				closeUp();
			}
		});
		
		JPanel tabPanel = createPanel();
		JPanel buttonPanel = createPanel();
		JButton confirm = new JButton("Send");
		confirm.setToolTipText("Click to send game details to the Server");
		confirm.setAlignmentX(0.5f);
		confirm.addMouseListener(new MouseAdapter(){
			
			public void mouseClicked(MouseEvent e){
			    makeGameMessage();
//			    System.out.println("Click!");
			    hide();
			}
		});
		
		JPanel gamePanel = createPanel();
		JPanel rulePanel = createPanel();
		
		button41 = new JRadioButton("41");
		button45 = new JRadioButton("45");
		button45.setSelected(true);
		button110 = new JRadioButton("110");
		
		gameGroup = new ButtonGroup();
		gameGroup.add(button41);
		gameGroup.add(button45);
		gameGroup.add(button110);
		
		button41.addMouseListener(new MouseAdapter(){
			
			public void mouseClicked(MouseEvent e){
			    displayError("Game hasnt been implemented yet");
			    button45.setSelected(true);
			}
		});
		button45.addMouseListener(new MouseAdapter(){
			
			public void mouseClicked(MouseEvent e){
			    
			}
		});
		button110.addMouseListener(new MouseAdapter(){
			
			public void mouseClicked(MouseEvent e){
			    displayError("Game hasnt been implemented yet");
			    button45.setSelected(true);
			}
		});
		
		button41.setAlignmentX(0.5f);
		button45.setAlignmentX(0.5f);
		button110.setAlignmentX(0.5f);
		
		gamePanel.setBorder(BorderFactory.createTitledBorder("Please choose a Game"));
		gamePanel.add(button41);
		gamePanel.add(button45);
		gamePanel.add(button110);
		
		joker = new JCheckBox("Include Joker Card?");
		joker.setToolTipText("Tick this box to include the Joker card in play");
		
		strip = new JCheckBox("Strip the deck?");
		strip.setToolTipText("Tick this box to strip the deck of all low value cards");
		
		reneging = new JCheckBox("Include Reneging?");
		reneging.setToolTipText("Tick this box to include the concept of reneging in play");
		
		cutRightPlayLeft = new JCheckBox("Cutting goes right and Play goes left?");
		cutRightPlayLeft.setToolTipText("Tick this box to have the player to the right of the dealer offered to cut the deck"
		        + " and to have play going round to the left of the dealer");
		
		autowin = new JCheckBox("Win if anybody wins all 5 tricks in a row?");
		autowin.setToolTipText("Tick this box to allow a player to win if they win all 5 tricks in a round");
		
		deal_3_2 = new JCheckBox("Deal out 3 cards then 2?");
		deal_3_2.setToolTipText("Tick this box to have the dealer deal out 3 cards and then 2 card per dealing round or else to deal out 4 and 1 per dealing round");
		
		setDefaultRules();
		
		JButton resetRules = new JButton("Restore default rules");
		resetRules.addMouseListener(new MouseAdapter(){
			
			public void mouseClicked(MouseEvent e){
			    setDefaultRules();
			}
		});
		
		rulePanel.add(joker);
		rulePanel.add(strip);
		rulePanel.add(cutRightPlayLeft);
		rulePanel.add(reneging);
		rulePanel.add(autowin);
		rulePanel.add(deal_3_2);
		rulePanel.add(new JPanel(new GridLayout(0, 1) ).add(resetRules, 0) );
		
		JTabbedPane gameTab = new JTabbedPane();
		gameTab.addTab("Game", null, gamePanel, "Choose the game");
		gameTab.addTab("Rules", null, rulePanel, "Pick the rules. Default rules checked already");
		tabPanel.setToolTipText("Click \"Send\" straight away to start a game of 45 with its default rules.");
		tabPanel.add(gameTab);
		tabPanel.add(confirm);
		
		super.cPane.add(tabPanel);
		display();
    }
    
    /**
     * Resets the checklist to the default rules
     * 
     */
    private void setDefaultRules(){
        joker.setSelected(true);
        strip.setSelected(false);
        reneging.setSelected(true);
        autowin.setSelected(true);
        cutRightPlayLeft.setSelected(true);
        deal_3_2.setSelected(true);
    }
    
    /**
     * Creates a JPanel with a BoxLayout
     * 
     * @return A JPanel
     */
    private JPanel createPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        return panel;
    }
    
    /**
     * Displays a pop up error message
     * 
     * @param errorName The feedback you wish to display to the user
     */
    public void displayError(String errorName){
	    super.displayError(errorName);
    }
    
    /**
     * Shows the menu
     *
     */
    public void display(){
		super.display();
    }
    
    /**
     * hides the menu
     *
     */
    public void hide(){
        super.hide();
    }
    
    /**
     * Hides the menu and disposes of it
     *
     */
    public void closeUp(){
        hide();
        super.menu.dispose();
    }
    
    /**
     * Makes the game Message to send to the Server to make the game
     * 
     * (Use Case 4,3)
     */
    public void makeGameMessage() {
        String game = "cGAME=", rules = "cGRULES=";
        
        if(button45.isSelected() == true)
            game += "45";
        else if(button41.isSelected() == true)
            game += "41";
        else
            game += "110";
        
        rules += concatenateRules(joker.isSelected());
        rules += concatenateRules(strip.isSelected());
        rules += concatenateRules(cutRightPlayLeft.isSelected());
        rules += concatenateRules(reneging.isSelected());
        rules += concatenateRules(autowin.isSelected());
        rules += concatenateRules(deal_3_2.isSelected());
        
        gameMessage = game + "," + rules;
        if(debug)
            System.out.println(gameMessage);
        else
            myClient.sendMessage(gameMessage);
    }
    
    /**
     * 
     * @return The current game message, an empty String if it hasnt been created yet
     */
    public String getGameMessage(){
        return gameMessage;
    }
    
    /**
     * 
     * @param selected If the option was selected or not
     * @return A string of value "0" or "1" depending if the rule was ticked or not
     */
    private String concatenateRules(boolean selected){
        return ( (selected == true) ? "1" : "0" );
    }
}