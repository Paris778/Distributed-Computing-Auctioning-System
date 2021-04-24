package project_LogicLayer;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.crypto.SealedObject;


public interface RemoteInterface extends Remote, Serializable{

    // Auction Logic Methods

    abstract public String viewAllAuctions(int clientId) throws RemoteException;

    abstract public String viewAuctionDetails(int clientId, int auctionId) throws RemoteException;

    //Seller Methods

    abstract public void makeAuction(int clientId, AuctionInstance auction) throws RemoteException, Exception;

    abstract public String closeAuction(int auctionId, int clientId) throws RemoteException, Exception;

    //Buyer Methods
    
    abstract public String bidToAuction(int auctionId, double bidPrice, int clientId, String bidderName, String bidderEmail)  throws RemoteException, Exception;

    //Authentication 

    abstract public int sendAndReceiveInitialNumbers(int clientId, int clientNumber) throws RemoteException;

    abstract public SealedObject exchangeEncryptedNumbers(int clientId, SealedObject encryptedServer_ClientVersion) throws RemoteException;
    
    abstract public boolean clientIsAuthenticated(int clientId) throws RemoteException;

	

    

}
