package project_SurfaceLayer;

//Imports
import java.io.FileInputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.util.Random;
import java.util.Scanner;
import project_LogicLayer.*;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


//Main Class
public class MyClient {

    //The Server Stub
    private static RemoteInterface theServer;
    //User Variables
    private static int clientId;
    private static String clientName;
    private static String clientEmail;
    //Cipher
    private static Cipher cipher;
    //Clustering variables
    private static Registry registry;
    private static  String host;

    // Constructor
    public MyClient() {

    }

    public static void main(String[] args) throws Exception {

        // Connect to Server and Promt Input
        try {
            System.out.println("Registry:  " + registry);
            MyClient.theServer = (RemoteInterface) Naming.lookup("rmi://localhost/myServer");

            // Instanciate Cipher
            Cipher cipher = Cipher.getInstance("AES");
            MyClient.cipher = cipher;

            // Prints UI
            MyClient.printMainUserInterface();
            MyClient.initialiseClientInfo();

            // Authentication of Server
            System.out.println("> Calling for authentication from server");
            if (authenticated(MyClient.theServer)) {
                System.out.println("> Both Parties have successfully authenticated each other !!!");
                //// Main promt loops
                while (true) {
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Please type a command: ");
                    System.out.printf("> ");
                    String input = scanner.nextLine().toLowerCase();
                    serveUserCommand(MyClient.theServer, input);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    ///////////////////////

    public static SecretKey loadKey() throws Exception {

        FileInputStream fileStream = new FileInputStream("key.txt");

        byte[] encoded = new byte[16];
        fileStream.read(encoded);
        fileStream.close();

        return (new SecretKeySpec(encoded, 0, 16, "AES"));
    }

    public static SecretKey loadAuthorisationKey() throws Exception {

        FileInputStream fileStream = new FileInputStream("auth_key.txt");

        byte[] encoded = new byte[16];
        fileStream.read(encoded);
        fileStream.close();

        return (new SecretKeySpec(encoded, 0, 16, "AES"));
    }

    ////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    // COMMANDS
    private static void serveUserCommand(RemoteInterface theServer, String input) throws Exception {

        // Local Commands
        if (input.contains("help")) {
            helpCommand();
        } else if (input.contains("rebind")) {
            rebind();
            serveUserCommand(MyClient.theServer, input);
        } else if (input.contains("listallauctions") || input.contains("allauctions")) {
            try {
                System.out.println(theServer.viewAllAuctions(MyClient.clientId));

            } catch (Exception e) {
                rebind();
                serveUserCommand(MyClient.theServer, input);
            }
        } else if (input.contains("bid")) {
            int auctionId = 0;
            double bidPrice = 0;

            // Extracts Infroamtion
            try {
                auctionId = Integer.parseInt(input.split(" ", 3)[1]);
                bidPrice = Double.parseDouble(input.split(" ", 3)[2]);
                //
            } catch (Exception e){
                System.out.println("> Sorry , not enough arguments\n> The correct format is >bid <auctionId> <price to Bid>");
            }

            try {
                String serverReply = theServer.bidToAuction(auctionId, bidPrice, MyClient.clientId, MyClient.clientName,
                        MyClient.clientEmail);
                System.out.println(serverReply);
            } catch (Exception e) {
                System.out.println("> Sorry, this auction was not found");
                rebind();
            }
        } else if (input.contains("makeauction")) {
            try {
                theServer.makeAuction(MyClient.clientId, makeAuction());
            } catch (Exception e) {
                System.out.println("\n\n======================================================");
                System.out.println("> We apologise ! Please re-enter the auction details");
                System.out.println("======================================================");
                rebind();
                serveUserCommand(MyClient.theServer, input);
            }
        } else if (input.contains("auctiondetails")) {

            int auctionId = 100;
            try {
                auctionId = Integer.parseInt(input.split(" ", 2)[1]);
            } catch (Exception e) {
                System.out.println("> Sorry , not enough arguments\n> The correct format is >closeAuction <Auction ID> ");
                 rebind();
            }
            try {
                System.out.println(theServer.viewAuctionDetails(MyClient.clientId, auctionId));
            } catch (Exception e) {
                System.out.println("> Sorry, this auction was not found.");
                rebind();
            }

        } else if (input.contains("closeauction")) {

            System.out.println("Trying to close auction !!!!!!!");
            int auctionId = 100;
            try {
                auctionId = Integer.parseInt(input.split(" ", 2)[1]);
            } catch (Exception e) {
                System.out
                        .println("> Sorry , not enough arguments\n> The correct format is >closeAuction <Auction ID> ");
                        rebind();
            }
            try {
                System.out.println(theServer.closeAuction(auctionId, MyClient.clientId));
            } catch (Exception e) {
                System.out.println("> Sorry, this auction was not found.");
                rebind();
            }

        } else if (input.contains("malicious")) {
            MyClient.clientId = generateFakeClientId();
        } else {
            System.out.println("> Sorry, command not recognised.");
        }
    }

    //////////////////////////////////////////////////////////////

    private static void rebind() {
        System.out.println("\n> Please wait ...");
        try {
            if(registry !=null){
                registry.unbind("//localhost/myServer");
            }
            registry = LocateRegistry.getRegistry(host);
            //System.out.println("Registry:  " + registry);
            MyClient.theServer = (RemoteInterface) Naming.lookup("//localhost/myServer");
        } catch (Exception e) {
            System.out.println("\n> Sorry. We are experiencing some technical diffculties at the moment. Please try again later.");
            System.exit(-1);
        }
    }


    /////////////////////////////////////////////////////////////

    private static boolean authenticated(RemoteInterface theServer) {

        try {
            if (theServer.clientIsAuthenticated(MyClient.clientId)) {
                return true;
            }
        } catch (RemoteException e3) {
            rebind();
        }
        // Mutual Authentication Sequence
        Random rand = new Random();

        // 1 Generate a client secret number
        int clientNumber = generateSecretClientNumber();

        // 2 Request a connection from the server and exchange secret numbers
        int serverSecretNumber = 1;
        try {
            serverSecretNumber = theServer.sendAndReceiveInitialNumbers(MyClient.clientId, clientNumber);
        } catch (RemoteException e2) {
            rebind();
            return authenticated(MyClient.theServer);
        }
        // Exit if server declines request due to malicious key // Avoid reflection
        // attack
        if (serverSecretNumber == MyServer.ERROR_CODE_MALICIOUS_KEY) {
            System.out.println("> Server declined the connection due to suspected malicious activity.");
            System.exit(0);
        }

        // 3 Encrypt the number received from the server
        SealedObject serverNumber_ClientVersion_Encrypted = null;
        try {
            serverNumber_ClientVersion_Encrypted = encryptAuthorisationNumber(serverSecretNumber);
        } catch (Exception e) {
            rebind();
            return authenticated(MyClient.theServer);
        }
        // Send the server its encrypted number and receive the encrypted number I sent
        // the server.
        SealedObject clientNumber_ServerVersion_Encrypted = null;
        try {
            clientNumber_ServerVersion_Encrypted = theServer.exchangeEncryptedNumbers(MyClient.clientId,serverNumber_ClientVersion_Encrypted);
        } catch (RemoteException e1) {
            rebind();
            return authenticated(MyClient.theServer);
        }

        // 4 Decrypt server number to see if it matches the one that was sent
        int clientNumber_ServerVersion_Decrypted = decryptAuthorisationNumber(clientNumber_ServerVersion_Encrypted);
        boolean serverAuthorised_FromClient;
        if (clientNumber_ServerVersion_Decrypted == clientNumber) {
            System.out.println("The server is authenticated from the Client Side");
            serverAuthorised_FromClient = true;
        } else {
            serverAuthorised_FromClient = false;
        }

        // 5 Confirm that both parties have authenticated each other
        try {
            if (theServer.clientIsAuthenticated(MyClient.clientId) && serverAuthorised_FromClient) {
                return true;
            }
        } catch (RemoteException e) {
            rebind();
            return authenticated(MyClient.theServer);
        }
        //
        return false;
    }

    ////////////////////////////////////////////////////

    private static SealedObject encryptAuthorisationNumber(int secretNumber) {
        SealedObject sealedNumber = null;
        try {
            MyClient.cipher.init(Cipher.ENCRYPT_MODE, loadAuthorisationKey());
            sealedNumber = new SealedObject(secretNumber, MyClient.cipher);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sealedNumber;
    }

    ////////////////////////////////////////////////////

    private static int decryptAuthorisationNumber(SealedObject encryptedNumber) {
        int decrypted = 0;
        try {
            MyClient.cipher.init(Cipher.DECRYPT_MODE, loadAuthorisationKey());
            decrypted = (int) encryptedNumber.getObject(MyClient.cipher);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    private static AuctionInstance makeAuction() {

        System.out.println("=====================================================================");
        System.out.println("                        Making a new Auction ");
        System.out.println("=====================================================================\n");

        AuctionItem auctionedItem = makeItem();

        // System.out.println("> Hey let's make an auction");
        Scanner scanner = new Scanner(System.in);
        //
        boolean validPrice = false;
        double startingPrice = -1;
        do {
            System.out.println("> Please type the starting price of your item ($) : ");
            System.out.printf("> ");

            try {
                startingPrice = Double.parseDouble(scanner.nextLine());
                if (startingPrice < 0) {
                    System.out.println("> Invalid Price");
                    validPrice = false;
                } else {
                    validPrice = true;
                }
            } catch (Exception e) {
                System.out.println("> Invalid Price");
                validPrice = false;
            }
        } while (!validPrice);
        //
        double reservedPrice = 0;
        do {
            System.out.println("> Please type the reserved price of your item (minimum accepted price) ($) : ");
            System.out.printf("> ");
            try {
                reservedPrice = Double.parseDouble(scanner.nextLine());
                if (reservedPrice < startingPrice) {
                    System.out.printf("> Sorry, reserve price must be higher than starting price: $%.2f \n",
                            startingPrice);
                }
            } catch (Exception e) {
                reservedPrice = 0;
                System.out.println("> Invalid Price");
            }
        } while (reservedPrice < startingPrice);
        //
        System.out.println("> AUCTION SAVED !");
        System.out.println("\n=====================================================================\n");

        return new AuctionInstance(MyClient.clientId, auctionedItem, startingPrice, reservedPrice);
    }

    ///////////////

    private static AuctionItem makeItem() {
        Scanner sc = new Scanner(System.in);

        System.out.println("==============================================");
        System.out.println("      Making an Item to put to Auction");
        System.out.println("==============================================");

        //
        System.out.println("Please type the NAME of your item: ");
        System.out.printf("> ");
        String itemName = sc.nextLine();
        //
        System.out.println("Please type a description of your item: ");
        System.out.printf("> ");
        String itemDescription = sc.nextLine();

        System.out.println("\n> Item Successfully created !!!");
        System.out.println("==============================================\n");
        return new AuctionItem(1, itemName, itemDescription);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    private static void initialiseClientInfo() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Please type your name: ");
        System.out.printf("> ");
        MyClient.clientName = sc.nextLine();
        //
        System.out.println("Please type your e-mail: ");
        System.out.printf("> ");
        MyClient.clientEmail = sc.nextLine();
        //
        System.out.println("> Your allocated Client ID is: " + MyClient.generateClientId());
    }

    ///////////////////////////

    private static int generateClientId() {
        Random rand = new Random();
        String stringToBeHashed = (MyClient.clientName + ":" + MyClient.clientEmail + rand.nextInt(999));
        // Hash the string
        int hash = stringToBeHashed.hashCode();
        // Avoid negative numbers
        if (hash < 0) {
            hash = -hash;
        }
        // Assign as ID
        MyClient.clientId = hash;

        return MyClient.clientId;
    }

    ///////////////////////////

    private static int generateSecretClientNumber() {
        // Number needs to be odd and less than 10,000
        Random rand = new Random();
        int number = rand.nextInt(9996) + 1;
        if (number % 2 == 0) {
            number += 1;
        }
        return number;
    }

    /////

    private static int generateFakeClientId() {
        Random rand = new Random();
        String s = "fake id abc";

        int hash = s.hashCode();
        hash = hash + rand.nextInt(999);
        // Avoid negative numbers
        if (hash < 0) {
            hash = -hash;
        }
        MyClient.clientId = hash;
        System.out.println("> Your new Fake ID is: " + hash);
        System.out.println("> WARNING !!! Servers might decline a connection with you if you're not authenticated");

        return MyClient.clientId;
    }

    private static void helpCommand() {

        System.out.println("=========================================================================================\n"
                + "|                            This is a list of availabe commands                        |\n"
                + "=========================================================================================\n"
                + "> ListAllAuctions - List all ongoing auctions\n"
                + "> ViewAuctionDetails - Can see a detailed view of the auction specs\n"
                + "> MakeAction - Start making a new auction\n"
                + "> Bid <auctionId> <price to Bid> - Bids the specified price to the auction matching the ID\n"
                + "> CloseAuction <AuctionID> - Close specified auction (WARNING: Only for Auction Admins)\n"
                + "=========================================================================================\n");
    }

    /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////
    // USER INTERFACE
    ////////////////////////////////////////////////////////////////

    private static void printMainUserInterface() {

        System.out.println("===================================================\n"
                + "===================================================\n"
                + "    _____ _      _____ ______ _   _ _______ \n" + "   / ____| |    |_   _|  ____| \\ | |__   __|\n"
                + "  | |    | |      | | | |__  |  \\| |  | |   \n" + "  | |    | |      | | |  __| | . ` |  | |   \n"
                + "  | |____| |____ _| |_| |____| |\\  |  | |   \n" + "   \\_____|______|_____|______|_| \\_|  |_|\n"
                + "\n===================================================\n"
                + "===================================================\n\n");

        System.out.println(
                "> Hello !!!! \n> Welcome CLIENT\n\n" + "******************************************************\n"
                        + "*  Please type Help for a list of available commands *\n"
                        + "******************************************************\n");
    }
}
