package client.graphics;
/*
 * Created on 24-Nov-2004
 */

/**
 * @author Pj O' Halloran
 * Loads the all the card images into a Map
 * for extremely easy and quick access and storage
 * with the disadvantage that it takes some time 
 * for the cards to load initially, but the advantage that they
 * can be called later instantly
 */

import java.awt.Image;
import java.util.HashMap;

import javax.swing.ImageIcon;

public class CardGameImages {
	
	private static final String[] imageFolders = 
						{"images/clubs/", "images/diamonds/", "images/hearts/", "images/spades/"};
	private static final String cIFolder = "images/common/";
	private static final int MAP_SIZE = 114;//make the map roughly double what is needed, to
								//save the problem of the map rehashing/resizing itself
								//if it runs out of space
    
    //The main data structure containing all the images of the cards
	private HashMap cardPics;
	//Marker which indicates if all the images have been loaded yet
	private boolean imagesLoaded;
	
	/**
	 * Creates a CardGameImages object
	 *
	 */
	public CardGameImages(){
		cardPics = new HashMap(MAP_SIZE);
		imagesLoaded = false;
	}
	
	/**
	 *  loads in the all the card game images into a map for easy access.
	 *
	 * e.g.
	 * The Ace Of Hearts can be got out of the map by 
	 * supplying the key "ah".
	 */
	public void loadInAllImages() {
		String key, suitID="";
		Image curPic;
		
		for(int folderI = 0; folderI < imageFolders.length; folderI++){
		    suitID = imageFolders[folderI].substring(7, 8);
		    
			//Number cards loaded and stored, in this inner loop
			for(int i = 2; i <= 10; i++){
				curPic = loadImage(imageFolders[folderI]+i+".gif");
				//key for the map will be [num, letter] e.g. '2c' = 2 of clubs card
				key = ( (i==10) ? "t"+suitID : i+suitID );
				
				cardPics.put( key, new CardInfo(curPic, key) );
			}
			//The suits Ace
			key = "a"+suitID;
			cardPics.put( key, new CardInfo(loadImage(imageFolders[folderI]+"a.gif"), key) );
			//The suits King
			key = "k"+suitID;
			cardPics.put( key, new CardInfo(loadImage(imageFolders[folderI]+"k.gif"), key) );
			//The suits Queen
			key = "q"+suitID;
			cardPics.put( key, new CardInfo(loadImage(imageFolders[folderI]+"q.gif"), key) );
			//The suits Jack
			key = "j"+suitID;
			cardPics.put( key, new CardInfo(loadImage(imageFolders[folderI]+"j.gif"), key) );
		}
		
		/** Load in all the misc images now **/
		//background
		cardPics.put("bg", new CardInfo(loadImage(cIFolder+"back1.JPG"), "bg") );
		
		//backface of card(vertical)
		cardPics.put("cardV", new CardInfo(loadImage(cIFolder+"card_back.gif"), "cardV") );
		
		//backface of card(horizontal)
		cardPics.put("cardH", new CardInfo(loadImage(cIFolder+"card_back_r.gif"), "cardH") );
		
		//Deck pictures (mouse clicked, released and entered pictures)
		cardPics.put("deckU", new CardInfo(loadImage(cIFolder+"deckUp.gif"), "deckU") );
		cardPics.put("deckD", new CardInfo(loadImage(cIFolder+"deckDown.gif"), "deckD") );
		cardPics.put("deckR", new CardInfo(loadImage(cIFolder+"deckRollover.gif"), "deckR") );
		
		//A temp card
		cardPics.put("temp", new CardInfo(loadImage(cIFolder+"blank.gif"), "temp") );
		
		//key must be "jok"
		cardPics.put("jok", new CardInfo(loadImage(cIFolder+"Joker.gif"), "jok") );
		/** End of load images **/
		
		imagesLoaded = true;
	}
	
	/**
	 * Uses a media tracker to load in the image in a seperate thread for speed
	 * 
	 * @param path 	The filepath of the image
	 * @return		The Image to be stored
	 */
	private Image loadImage(String path){
		return new ImageIcon(path).getImage();
	}
	
	/**
	 * if this function returns false, then the images have nt finished loading yet
	 * 
	 * @return true if the images have been loaded
	 */
	public boolean isImagesLoaded(){
		return imagesLoaded;
	}
	
	/**
	 * Returns the image id'd by the key passed in
	 * 
	 * Returns null if the key does not exist.
	 * 
	 * @param key The key of the image you want, e.g. '5h' for the 5 of hearts, 'ac' for the ace of clubs 
	 */
	public Image getCardImage(String key){
		if(cardPics.containsKey(key)){
			CardInfo x = (CardInfo)cardPics.get(key);
			return x.card;
		}

		return null;
	}
	
	/**
	 * Returns the card object
	 * 
	 * @param key The key of the card
	 * @return
	 */
	public CardInfo getCard(String key){
		if(cardPics.containsKey(key)){
			CardInfo x = (CardInfo)cardPics.get(key);
			return x;
		}

		return null;
	}
}

/**
 * 
 * @author Pj O' Halloran
 */
class CardInfo{

    //The key needed to identify the card on the Server side and to get it from the Hash Map on the Client side
    String keyID;
    //The image attached to the cards id
    Image card;
    
    /**
     * Creates a new CardInfo object
     * 
     * @param im The image of the card
     * @param key The unique string id of the card, e.g. "ah" - ace of hearts, "td" - ten of diamonds
     */
    public CardInfo(Image im, String key){
        card = im;
        keyID = key;
    }
    
    /**
     * Returns the string identifier of the card for id on the Server side 
     * and retrieving an image out of the hash map on the client side
     */
    public String toString(){
        return keyID;
    }
}