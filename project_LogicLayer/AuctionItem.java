package project_LogicLayer;

import java.io.Serializable;


public class AuctionItem implements Serializable{

    
    private static final long serialVersionUID = 1L;
    // Variables
    private int itemId;
    private String itemTitle;
    private String itemDescription;

    //Constructor
    public AuctionItem(int itemId, String itemTitle, String itemDescription){

        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.itemDescription = itemDescription;
    }
   

    ////////////////////////////////////////////////////
    //Accessor Methods

    public int getItemId(){
        return this.itemId;
    }

    public String getItemTitle(){
        return this.itemTitle;
    }

    public String getItemDescription(){
        return this.itemDescription;
    }

    public String getFullItemSpecs(){

        String fullItemSpecs = (
            "\n===========================================================\n\n"
            + "=================\n" 
            + " Full Item Specs\n"
            + "=================\n"
            + " - Item ID: " + this.getItemId()  + "\n"
            + " - Item Title: " + this.getItemTitle() + "\n"
            + " - Item Description: " +this.getItemDescription() + "\n"
            + "\n===========================================================\n"
        );

        return fullItemSpecs;
    }

    ////////////////////////////////////////////////////
    //Mutator Methods


}