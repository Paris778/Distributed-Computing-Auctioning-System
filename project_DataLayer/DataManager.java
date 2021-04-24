package project_DataLayer;

import java.io.Serializable;
import java.util.LinkedHashMap;
import javax.crypto.SealedObject;

import project_LogicLayer.*;

public class DataManager implements Serializable {
    /*
    * The dataManager class is a Singleton Class,
    * Meaning that only one instance of it can exist at a time.
    */

    private static final long serialVersionUID = 1L;

    // VARIABLES
    private static DataManager dataManagerInstance = null;

    //Databases
    private LinkedHashMap<Integer, AuctionInstance> auctionDataBase = new LinkedHashMap<Integer, AuctionInstance>();
    //
    private LinkedHashMap<Integer,Boolean> authenticatedUsersDataBase = new LinkedHashMap<Integer, Boolean>();
    //
    private LinkedHashMap<Integer,LinkedHashMap<Integer,SealedObject>> clientResponseDatabase = new LinkedHashMap<Integer,LinkedHashMap<Integer,SealedObject>>();
    //


    //Misc
    private String auctionBanners = String.format("  %3s %25s %18s","ID", "Item Title" , "Current Bid");

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Constructor
    private DataManager(){
        System.out.println("> DataManager succesfully created !!!");
    }

    //Static method to create/return instance of singleton dataManager class
    public static DataManager getInstance(){

        if (dataManagerInstance == null){
            dataManagerInstance = new DataManager();
        }
        
        return dataManagerInstance; 
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Accessor Methods

    public AuctionInstance getAuction(int auctionId){
        return this.auctionDataBase.get(auctionId);
    }

    public String getAllAuctionsList(){

        if(this.auctionDataBase.size() >= 1){
            StringBuilder builder = new StringBuilder();
            builder.append("\n======================================================\n");
            builder.append(this.auctionBanners + "\n");
            builder.append("======================================================\n");
            //Loop through all Auctions
            for(AuctionInstance auction : auctionDataBase.values()){
                //
                String auctionDetails = String.format("> %3d %25s            $%.2f",
                auction.getAuctionID(),
                auction.getAuctionItem().getItemTitle(),
                auction.getHighestBid()
                );
                //
                builder.append(auctionDetails + "\n");
            }
            return builder.toString();
        }
        //
        return "> Sorry, no auctions to display";
    }

    ///////////////////////////////////////////////
    //Data Methods

    public void storeAuction(AuctionInstance auction){
        auction.setID(generateAuctionId());
        System.out.println("> Trying to store auction in data manager !");
        this.auctionDataBase.put(auction.getAuctionID(), auction);
    }

    public void deleteAuction(int auctionId) {
        //Remove auction from database
        try {
            this.auctionDataBase.remove(auctionId);
        } catch (Exception e) {
            System.out.println("> Item does not exist in database");
        } 
    }

    //////////////////////////////////////////
    //User authentication methods

    public void storeAuthenticatedUser(int clientId){
        this.authenticatedUsersDataBase.put(clientId,true);
        //Memory Management, Remove User from previous steps
        this.clientResponseDatabase.remove(clientId);
    }

    public boolean isAuthenticated(int clientId){

        if(this.authenticatedUsersDataBase.containsKey(clientId)){
            if(this.authenticatedUsersDataBase.get(clientId)){
                return true;
            }
        }
        return false;
    }

    //////////////

	public void storeOriginalAuthorisationNumbers(int clientId, int serverNumber, SealedObject encryptAuthorisationNumber) {
        LinkedHashMap<Integer,SealedObject> map =  new LinkedHashMap<Integer,SealedObject>();
        map.put((Integer)serverNumber, encryptAuthorisationNumber);
        //
        this.clientResponseDatabase.put(clientId, map);
	}

	public int getOriginalAuthorisationNumber_Server(int clientId) {
        return (int) (this.clientResponseDatabase.get(clientId).keySet().toArray()[0]);
    }
    
    public SealedObject getOriginalClientResponse_Encrypted(int clientId){
        return (SealedObject) (this.clientResponseDatabase.get(clientId).values().toArray()[0]);
    }    

    /////////////////////

    private final int generateAuctionId() {
        return this.auctionDataBase.size();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Data Synchronisation Methods

    public final LinkedHashMap<Integer,AuctionInstance> getAuctionDataBaseState(){
        return this.auctionDataBase;
    }

    public final LinkedHashMap<Integer,Boolean> getAuthenticatedUserDataBaseState(){
        return this.authenticatedUsersDataBase;
    }

    public final void updateDataState(DataManager newState){
        //Update Databases
        this.auctionDataBase = newState.getAuctionDataBaseState();
        //
        this.authenticatedUsersDataBase = newState.getAuthenticatedUserDataBaseState();
    }

    /////

    public String printAuthenticatedUserIDs(){
        StringBuilder builder = new StringBuilder();
        if(this.authenticatedUsersDataBase.size() >= 1){
            for(Integer id : authenticatedUsersDataBase.keySet()){
                builder.append("> " + id + "\n");
            }
        } else {
            return ("> Sorry, no IDs are available");
        }
        return builder.toString();
    }
}
