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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import common.Menu;

/**
 * @author Pj O' Halloran
 */
public class ServerMenu extends Menu {

	//Components on this JFrame
	private JTextField ip1;
	private JTextField portTxt;
	private JTextField nameTxt;
	private JButton confirm;
    
	private Client myClient;
	
	//TextField max size
	private static final int PORT_SIZE = 12;
	private static final int NAME_SIZE = 15;
	
	/**
	 * Creates a menu for taking in Server information
	 *
	 */
    public ServerMenu(){
        super("Fill in the Server details and your Nickname");
        
        boolean debug = false;
        myClient = new Client(debug);
        setUp();
    }
    
    /**
     * Sets up the menu and displays it on screen
     *
     */
    private void setUp(){
        super.setUp(250, 250, 350, 250);
        super.menu.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				closeUp();
			}
		});
		
        //The labels of the fields
		JLabel srvrL = new JLabel("Server IP address");
		JLabel portL = new JLabel("Server Port Number");
		JLabel nameL = new JLabel("Your Nickname");
		
		//Setting up the IP address field
		ip1 = new JTextField();
		ip1.setText("default");
		ip1.setToolTipText("Type \"default\" for the default server or \"local\" if the Server is running on this machine");
		
		//Setting up the Port number field
		portTxt = new JTextField(PORT_SIZE);
		portTxt.setText("default");
		portTxt.setToolTipText("Type \"default\" for the default server");
		
		//Setting up the Nickname field
		nameTxt = new JTextField(NAME_SIZE);
		nameTxt.setToolTipText("Enter your nickname here");
		
		//Setting up the button
		confirm = new JButton("Connect");
		confirm.addMouseListener(new MouseAdapter(){
				boolean go = false;
				
				/**
				 * Confirm Deatails and connect to Server
				 * (Use Case 2) 
				 */
				public void mouseClicked(MouseEvent e){
					go = checkDetails();
					if(go == true){
					    myClient.connect();
					    //TODO: Must send over the name once the player has connected - doing this in GameWAitScreen
					    hide();
					}
				}
			});
		
		JPanel srvrIP = new JPanel(new GridLayout(1, 2) );
		srvrIP.setSize(new Dimension(250/4, 250/4));
		srvrIP.add(srvrL, 0);
		srvrIP.add(ip1, 1);
		super.cPane.add(srvrIP);
		
		JPanel srvrPort = new JPanel(new GridLayout(1, 2) );
		srvrPort.setSize(new Dimension(250/4, 250/4));
		srvrPort.add(portL, 0);
		srvrPort.add(portTxt, 1);
		super.cPane.add(srvrPort);
		
		JPanel name = new JPanel(new GridLayout(1, 2) );
		name.setSize(new Dimension(250/4, 250/4));
		name.add(nameL, 0);
		name.add(nameTxt, 1);
		super.cPane.add(name);
		
		JPanel game = new JPanel(new GridLayout(1, 2) );
		game.setSize(new Dimension(250/4, 250/4));
		game.add(confirm, 0);
		super.cPane.add(game);
		
		display();
    }
    
    /**
     * Checks if the fields entered by the player were done correctly
     * (Use Case 2A)
     * 
     * @return True if all fields were filled up correctly
     */
    private boolean checkDetails(){
		String ipAddress, port, name;
		int value = 0, portNum = 0;
		String[] splitIp;
		
		ipAddress = ip1.getText().trim();
		port = portTxt.getText().trim();
		name = nameTxt.getText().trim();
		
		if ( (ipAddress.equals("") ) || (port.equals("") ) || (name.equals("") ) ){
		    //if any of them are blank then show an error message
			displayError("Fill in all 3 fields please");
		    return false;
		}
		
		/*************Check Nick name******************/
		if( (name.length() <= 2) || (name.length() > 15) ){
		    displayError("Your nickname must be of minimum length of 3 characters and a maximum length of 15 characters.");
		    return false;
		}
		/*************End of check nickname************/ 
		
		/**************Check IP address****************/
		if (ipAddress.equals("local"))
			ipAddress = "localhost";
		else if(ipAddress.equals("default"))
		    ipAddress = "";
		else{
			splitIp = ipAddress.split("\\.");
			if (splitIp.length == 4){
				for(int i = 0; i < splitIp.length; i++) {
					try {
						value = Integer.parseInt(splitIp[i]);
					}
					catch(NumberFormatException nfe) {
						//if its an invalid IP address return failure
						displayError("The IP address you enter cannot contain a non-numeral part");
					    return false;
					}
					if ( (splitIp[i].length() > 3) || (value < 0) || (value > 255) ) {
						//if its an invalid IP address return failure
						displayError("That is an invalid IP address");
					    return false;
					}
				}
				//If the person types an extra '.' at the end of the address, remove it
				if(ipAddress.endsWith("."))
					ipAddress = ipAddress.substring(0, ipAddress.length()-1);
			}
			else {
				displayError("The IP address you entered is of invalid length");
			    return false;
			}
		}
		/**************End Check IP address****************/
		
		/**************Check Port number****************/
		if(port.equals("default")) {
		    portNum = -1;
		}
		else {
			try {
			    portNum = Integer.parseInt(port);
				if ( (portNum < 1200) || (portNum < 0) || (portNum > 12000) ) {
					//if its an invalid port number, return failure
				    displayError("That is an invalid port number. It must be between 1200 and 12000 and cannot be less than 0");
				    return false;
				}
			}
			catch(NumberFormatException nfe) {
				//if its an invalid port number, return failure
				displayError("This field must be a number between 1200 and 12000");
			    return false;
			}
		}
		/**************End Check Port number****************/
		
		//set the servers port number and ip address used to connect to it
		myClient.storeFields(name, ipAddress, portNum);
		//if we get as far as here, details are correct
		return true;
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
     * Hides the menu and exits the program
     *
     */
    public void closeUp(){
        hide();
        super.menu.dispose();
    }
    
    /**
     * Main function called when the Client exe is clicked
     * (Use Case 1)
     */
    public static void main(String[] args) {
        //this ensures that the menu is dispatched in the Event Thread
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ServerMenu srvrMenu = new ServerMenu();
            }
        });
    }
}