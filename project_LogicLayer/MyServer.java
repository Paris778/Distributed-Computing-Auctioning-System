package project_LogicLayer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
//
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
//
import javax.swing.text.View;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.util.Util;

import project_DataLayer.*;


// set CLASSPATH=.;c:\Program Files\Java\jdk-15\lib;C:\Users\User\Desktop\SCC-311\Coursework\Coursework 3 - Clustering\jgroups-5.0.0.Final.jar

//  

public class MyServer extends UnicastRemoteObject implements RemoteInterface, Receiver {
    // CONSTANTS
    public static final int ERROR_CODE_MALICIOUS_KEY = -4;

    //
    private static final long serialVersionUID = 1L;

    // Data Manager
    private final DataManager dataManager;

    // Cipher
    private static Cipher cipher = null;
    //
    private Random rand = new Random();

    private Registry registry;
    private MyServer server;
    public static Timer timer = new Timer();
    private static boolean ACTIVE_COORDINATOR = false;

    //////////////////////////////////////////////////////////////////

    // http://www.jgroups.org/manual/html/user-channel.html

    private JChannel channel;
    String user_name = System.getProperty("user.name", "n/a");
    final LinkedList<String> state = new LinkedList<String>();

    private void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("AuctionServerCluster");
        channel.getState(null, 10000);
        this.runServerCoordinatorCheck();
        eventLoop();
        // After Server Shuts down
        System.exit(-1);
    }

    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void receive(Message msg) {
        System.out.println("\n\n----------------------------------------");
        System.out.println("-          DATA STATE UPDATED          -");
        System.out.println("----------------------------------------\n");
        synchronized (this.dataManager) {
            // state.add(line);
            this.dataManager.updateDataState(msg.getObject());
        }
        System.out.println(this.dataManager.getAllAuctionsList());
    }

    public void getState(OutputStream output) throws Exception {
        synchronized (dataManager) {
            Util.objectToStream(dataManager, new DataOutputStream(output));
        }
    }

    public void setState(InputStream input) throws Exception {
        DataManager dataState;
        dataState = (DataManager) Util.objectFromStream(new DataInputStream(input));
        synchronized (this.dataManager) {
            this.dataManager.updateDataState(dataState);
        }
        System.out.println(this.dataManager.getAllAuctionsList());
    }

    private void eventLoop() throws AccessException, RemoteException, NotBoundException {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                System.out.flush();
                String line = in.readLine().toLowerCase();
                if (line.startsWith("quit") || line.startsWith("exit")) {
                    System.out.print(" Server will shut down ...");
                    unbindAndDisconnect();
                    break;
                }
                if (line.contains("view") || line.contains("viewstate")) {
                    System.out.println(this.dataManager.getAllAuctionsList());
                }
                if (line.contains("update")) {
                    System.out.println("> Attempting to Update State");
                    channel.send(null, this.dataManager);
                    System.out.print("> ");
                }
                if (line.contains("bind")) {
                    bindToRegistry();
                }
                if (line.contains("users")) {
                    System.out.println(this.dataManager.printAuthenticatedUserIDs());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                System.out.println("Exception in While True loop in eventloop");
            }
        }
    }

    private void runServerCoordinatorCheck() {

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (registry == null) {
                    if (channel.getView().getMembers().get(0).equals(channel.getAddress())) {
                        bindToRegistry();
                        System.out.println("\n\n======================================");
                        System.out.println("  This Machine is now the Coordinator   ");
                        System.out.println("======================================\n");
                    }
                }
            }
        }, 0, 1000);
    }

    private boolean bindToRegistry() {
        
        try {
            this.registry = LocateRegistry.getRegistry();
            //Naming.rebind("//localhost/myServer", this);
            Naming.rebind("myServer", this);
        } catch (Exception e1) {
            return false;
        }

        //System.out.println("Registry:  " + registry);

        try {
            this.registry.bind("//localhost/myServer", this);
            Naming.rebind("//localhost/myServer", this);
            ACTIVE_COORDINATOR = true;
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private void unbindAndDisconnect() {
        if (this.registry != null) {
            try {
                //this.registry.unbind("//localhost/myServer");
                Naming.unbind("//localhost/myServer");
            } catch (Exception e) {
            }
        }
        if (this.channel != null) {
            this.channel.disconnect();
        }
        ACTIVE_COORDINATOR = false;
    }


    ////////////////////////////////////////////////////////////////////////////

    // Constructor
    public MyServer() throws RemoteException {
        super();

        // Initialise DataManager
        this.dataManager = DataManager.getInstance();
        initialiseUi();

        try {
            saveKey();
            saveAuthorisationKey();
        } catch (Exception e) {
            System.out.println("Hello 22 ");
            e.printStackTrace();
        }
    }

    // -Djava.rmi.server.codebase=file:

    ////////////////////////////////////////////////////
    // Main Method

    public static void main(String[] args) {
        try {
            ///
            // Initialising Registry
            Registry registry = LocateRegistry.getRegistry();
            System.out.println("Registry:  " + registry);
            // Bind an instance of the object
            MyServer server = new MyServer();
            System.out.println("Server is successfully deployed !!!!!!! \n\n");
            // Make Log
            Logger.makeLog("> Server deployed");
            server.start();
        } catch (Exception e) {
            System.out.println("Exception has occured: \n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Implemented Methods
    ////////////////////////////////////////////////////////////////////

    @Override
    public synchronized String viewAllAuctions(int clientId) throws RemoteException {

        System.out.println(("> Client: " + clientId + " requested to view all auctions"));
        Logger.makeLog("> Client: " + clientId + " requested to view all auctions");

        if (clientIsAuthenticated(clientId)) {
            return this.dataManager.getAllAuctionsList();
        }
        return ("> Non Authenticated User. Connection Declined.");
    }

    @Override
    public synchronized void makeAuction(int clientId, AuctionInstance auction) throws RemoteException {

        Logger.makeLog("> Client: " + clientId + " wants to make a new auction ");

        // Store Auction
        if (clientIsAuthenticated(clientId)) {
            System.out.println("> Make New Auction request received !!");
            this.dataManager.storeAuction(auction);
            System.out.println("> Auction successfully made !!! ");
            Logger.makeLog("> Client: " + clientId + " successfully made an auction");
            // Update Replica State
            try {
                channel.send(null, this.dataManager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized String viewAuctionDetails(int clientId, int auctionId) throws RemoteException {

        Logger.makeLog("> Client: " + clientId + " wants to view the details of auction :  " + auctionId);

        if (clientIsAuthenticated(clientId)) {
            String auctionDetails = this.dataManager.getAuction(auctionId).getAuctionDetails(clientId);
            return auctionDetails;
        }
        return ("> Non Authenticated User. Connection Declined.");
    }

    @Override
    public synchronized String bidToAuction(int auctionId, double bidPrice, int clientId, String bidderName,
            String bidderEmail) throws RemoteException {

        Logger.makeLog("> Client: " + clientId + " wants to bid to auction :  " + auctionId);

        if (clientIsAuthenticated(clientId)) {
            System.out.println("> Bid received for auction: " + auctionId);
            if (bidPrice < this.dataManager.getAuction(auctionId).getHighestBid()) {
                return "> Server: Bid needs to be higher than the current highest to be accepted.\n> Please update list to see current highest.";
            }
            if (userCanBid(auctionId, clientId)) {
                System.out.println("> Bid from user :" + clientId + " received");
                String s = this.dataManager.getAuction(auctionId).makeBid(clientId, bidPrice, bidderName, bidderEmail);
                // Update Replica State
                try {
                    channel.send(null, this.dataManager);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return (s);
            } else {
                System.out.println("> Bid from user :" + clientId + " is a phantom bid");
                return ("> No Phantom Bids are accepted, sorry");
            }
        }

        return ("> Non Authenticated User. Connection Declined.");
    }

    @Override
    public synchronized String closeAuction(int auctionId, int clientId) throws RemoteException {

        Logger.makeLog("> Client: " + clientId + " requested to close auction : " + auctionId);

        if (clientIsAuthenticated(clientId)) {
            System.out.println("> Closure of Auction: " + auctionId + " was requested by User: " + clientId);
            // If Client is the creator of the auction
            if (clientId == this.dataManager.getAuction(auctionId).getAuctionCreatorID()) {

                String auctionDetails = this.dataManager.getAuction(auctionId).getAuctionDetails(clientId);
                // Delete Auction from database
                this.dataManager.deleteAuction(auctionId);

                // Update Replica State
                try {
                    channel.send(null, this.dataManager);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return auctionDetails + "> Auction Succesfully closed !!!";
            }
            return ("> Sorry, you don't have permission to close this auction.");
        }
        return ("> Non Authenticated User. Connection Declined.");
    }

    //////////////////////////////////////////////////////////
    // Server Authentication Methods
    //////////////////////////////////////////////////////////

    @Override
    public synchronized int sendAndReceiveInitialNumbers(int clientId, int clientNumber) throws RemoteException {

        System.out.println("> Client " + clientId + " requests authentication");
        Logger.makeLog("> Client: " + clientId + " requested authentication");

        // Tries to detect Refection Attack
        if (clientNumberIsMalicious(clientNumber)) {
            System.out.println("> Malicious acitvity detected from Client: " + clientId);

            return MyServer.ERROR_CODE_MALICIOUS_KEY;
        }
        // Generate Server Authentication Number and save the one received from client
        // in encrypted form
        int serverNumber = generateSecretServerNumber();
        this.dataManager.storeOriginalAuthorisationNumbers(clientId, serverNumber,
                encryptAuthorisationNumber(clientNumber));
        // Give back its own ayth number
        return serverNumber;
    }

    @Override
    public synchronized SealedObject exchangeEncryptedNumbers(int clientId,
            SealedObject serverNumber_ClientVersion_Encrypted) throws RemoteException {

        Logger.makeLog("> Client: " + clientId + " requested exchange of Encrypted numbers ");

        // Recall the number sent to this specific client
        int originalServerNumber = this.dataManager.getOriginalAuthorisationNumber_Server(clientId);
        // Recall the number this client sent to us (Encrypted Version)
        SealedObject clientNumber_ServerVersion_Encrypted = this.dataManager
                .getOriginalClientResponse_Encrypted(clientId);

        // Decrypt what the Client sent us for their authentication (Our original
        // number)
        int serverNumber_ClientVersion_Decrypted = MyServer
                .decryptAuthorisationNumber(serverNumber_ClientVersion_Encrypted);

        // If the number they sent us (after decryption) , matches the original one we
        // sent, they are authenticated from our side
        if (serverNumber_ClientVersion_Decrypted == originalServerNumber) {

            // User is added to the authenticated Users database
            System.out.println(">The Client " + clientId + " is authenticated from the Server Side");
            Logger.makeLog("> Client: " + clientId + " has been authenticated by the server");
            this.dataManager.storeAuthenticatedUser(clientId);
            try {
                channel.send(null, this.dataManager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }

        // If user is authenticated then we send them back the encrypted server response
        // in order for us to get authenticated too by the user
        return clientNumber_ServerVersion_Encrypted;
    }

    @Override
    public synchronized boolean clientIsAuthenticated(int clientId) throws RemoteException {
        // See if the user exists into the authenticated users database
        return (this.dataManager.isAuthenticated(clientId));
    }

    ////////////////////////////////////////////////////
    // ENCRYPTION & AUTHENTICATION
    ////////////////////////////////////////////////////

    public static void saveAuthorisationKey() throws Exception {

        // Initilaise Files
        File file = new File("auth_key.txt");
        FileOutputStream fileOutput = new FileOutputStream(file);

        // Generate Key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();

        // Instanciate Cipher
        Cipher cipher = Cipher.getInstance("AES");
        MyServer.cipher = cipher;
        cipher.init(Cipher.ENCRYPT_MODE, key);

        /// Save Key into a file
        byte[] bytes = key.getEncoded();
        fileOutput.write(bytes);
        fileOutput.close();
    }

    ////////////////////////////////////////////////////

    public static void saveKey() throws Exception {

        // Initilaise Files
        File file = new File("key.txt");
        FileOutputStream fileOutput = new FileOutputStream(file);

        // Generate Key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();

        // Instanciate Cipher
        Cipher cipher = Cipher.getInstance("AES");
        MyServer.cipher = cipher;
        cipher.init(Cipher.ENCRYPT_MODE, key);

        /// Save Key into a file
        byte[] bytes = key.getEncoded();
        fileOutput.write(bytes);
        fileOutput.close();
    }

    ////////////////////////////////////////////////////

    public static SecretKey loadKey() throws Exception {

        FileInputStream fileStream = new FileInputStream("key.txt");

        byte[] encoded = new byte[16];
        fileStream.read(encoded);
        fileStream.close();

        return (new SecretKeySpec(encoded, 0, 16, "AES"));
    }

    ////////////////////////////////////////////////////

    public static SecretKey loadAuthorisationKey() throws Exception {

        FileInputStream fileStream = new FileInputStream("auth_key.txt");

        byte[] encoded = new byte[16];
        fileStream.read(encoded);
        fileStream.close();

        return (new SecretKeySpec(encoded, 0, 16, "AES"));
    }

    ////////////////////////////////////////////////////

    private static SealedObject encryptAuthorisationNumber(int secretNumber) {
        SealedObject sealedNumber = null;
        try {
            MyServer.cipher.init(Cipher.ENCRYPT_MODE, loadAuthorisationKey());
            sealedNumber = new SealedObject(secretNumber, MyServer.cipher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sealedNumber;
    }

    ////////////////////////////////////////////////////

    private static int decryptAuthorisationNumber(SealedObject encryptedNumber) {
        int decrypted = 0;
        try {
            MyServer.cipher.init(Cipher.DECRYPT_MODE, loadAuthorisationKey());
            decrypted = (int) encryptedNumber.getObject(MyServer.cipher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Other Methods

    private final boolean userCanBid(int auctionId, int userId) {
        // Check is the user is the creator
        if (this.dataManager.getAuction(auctionId).getAuctionCreatorID() == (userId)) {
            // If yes, they can't bid
            return false;
        }
        return true;
    }

    //////////////////////////

    private static int generateSecretServerNumber() {
        // Number needs to be even and greater than 10,000
        Random rand = new Random();
        int number = rand.nextInt(9998) + 10000;
        if (number % 2 != 0) {
            number += 1;
        }
        return number;
    }

    //////////////////////////

    private static boolean clientNumberIsMalicious(int number) {
        // Checking if number matches expected parameters of client secret numbers (odd
        // and less than 10,000)
        if ((number > 10000) && (number % 2 == 0)) {
            return true;
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialiseUi() {
        System.out.println("\n\n=====================================================\n"
                + "=====================================================\n"
                + "   _____ ______ _______      ________ _____  \n"
                + "  / ____|  ____|  __ \\ \\    / /  ____|  __ \\ \n"
                + " | (___ | |__  | |__) \\ \\  / /| |__  | |__) | \n"
                + "  \\___ \\|  __| |  _  / \\ \\/ / |  __| |  _  / \n"
                + "  ____) | |____| | \\ \\  \\  /  | |____| | \\ \\ \n "
                + "|_____/|______|_|  \\_\\  \\/   |______|_|  \\_\\ \n\n"
                + "=====================================================\n"
                + "=====================================================\n");
    }

}