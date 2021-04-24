package project_LogicLayer;

import java.io.Serializable;


public class AuctionInstance implements Serializable {

    private static final long serialVersionUID = 1L;
    // Variables
    private int auctionId;
    //
    private int auctionCreatorID;
    private AuctionItem auctionItem;
    private double startingPrice;
    private double reservePrice; //Minimum accepted Price 
    //
    private int highestBidderID = 0;
    private double highestBid = 0.00;
    private String highestBidderName = "none";
    private String highestBidderEmailAddress = "none";
    //
    private int numberOfBids = 0;
    

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //Constructor
    public AuctionInstance(int creatorID,AuctionItem auctionItem, double startingPrice, double reservePrice){

        this.auctionCreatorID = creatorID;
        this.auctionItem = auctionItem;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.highestBid = startingPrice;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Main Handling methods

    public String makeBid(int clientId, double amount, String bidderName, String bidderEmail){
        this.numberOfBids++;
        if(amount > highestBid){
            this.highestBid = amount;
            this.highestBidderID = clientId;
            this.highestBidderName = bidderName;
            this.highestBidderEmailAddress = bidderEmail;
            return ("> Bid succesfully registered !!");
        }
        return ("> Bid Rejected. Bid must be higher than the current one");
    }

    public String getAuctionDetails(int clientId){
        if(clientId == (this.auctionCreatorID)){
            return serveAuctionCreatorDetails();
        }
        return serveClientAuctionDetails();
    }

    public String  closeAuction(int clientId){

        if(clientId == (this.auctionCreatorID)){
            return serveAuctionCreatorDetails();
        }

        return "> Sorry, you don't have permission to do that.";
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    //Helper Handling methods

    private String serveAuctionCreatorDetails(){
        StringBuilder builder = new StringBuilder();

        builder.append(             
        "=============================== \n"
        +"| Auction Details for Creator | \n"
        +"=============================== \n"
        +"Auction ID : " + this.auctionId +"\n"
        + this.auctionItem.getFullItemSpecs()
        +"=============================== \n"
        +"Starting Price: $" + this.startingPrice +"\n"
        +"Reserve Price:  $" + this.reservePrice);

        if(this.highestBid >= this.reservePrice){
            builder.append(" -- Reserve Reached !!!");
        }

        builder.append(
         "\n=============================== \n"
        +"\nTotal Number of Bids: " + this.numberOfBids
        +"\n=============================== \n"
        +"Highest Bid: $" + this.highestBid +"\n"
        +"Highest Bidder Name : " + this.highestBidderName +"\n"
        +"Highest Bidder E-mail Address: " + this.highestBidderEmailAddress +"\n"
        +"===============================\n");
    

        return(builder.toString());

    }

    //

    private String serveClientAuctionDetails(){
        return(
             "=============================== \n"
            +"| Auction Details for Client | \n"
            +"=============================== \n"
            +"Auction ID : " + this.auctionId +"\n"
            + this.auctionItem.getFullItemSpecs()
            +"=============================== \n"
            +"Total Number of Bids: " + this.numberOfBids +"\n"
            +"=============================== \n"
            +"Highest Bid: " + this.highestBid +"\n"
            +"===============================\n"
        );
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    //Accessor Methods

    public int getNumberOfBids(){
        return this.numberOfBids;
    }

    public AuctionItem getAuctionItem(){
        return this.auctionItem;
    }

    public int getAuctionID(){
        return this.auctionId;
    }

    public int getAuctionCreatorID(){
        return this.auctionCreatorID;
    }

    public double getHighestBid(){
        return this.highestBid;
    }

    public int getHighestBidderID(){
        return this.highestBidderID;
    }

    public double getStartingPrice(){
        return this.startingPrice;
    }

    public double getReservePrice(){
        return this.reservePrice;
    }

    public void setID(int id){
        this.auctionId = id;
    }
    
}
