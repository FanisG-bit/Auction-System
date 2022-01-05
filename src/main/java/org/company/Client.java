package org.company;

import org.company.exceptions.*;
import org.company.model.*;
import org.company.service.ClientAuctionManagementOperations;
import org.company.service.HandleInbox;
import org.company.service.PrintInbox;
import org.company.service.TCPPacketInteraction;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client {

    public static void main(String[] args) {

        int outPort = 6666;
        String serverIP = "127.0.0.1";
        Scanner scanner = new Scanner(System.in);

        String inputFromUser;
        System.out.println("""            
                                
                \t\t\tClientSideMenu
                --------------------------------------
                ?connect
                ?exit
                """);
        do {
            System.out.println("Type a Menu command (make sure to include '?' at the front): ");
            inputFromUser = scanner.nextLine();
            switch (inputFromUser) {
                case "?connect":
                break;
                case "?exit":
                    System.exit(0);
                break;
                default:
                    System.out.println("Not a valid menu command.");
            }
        } while (!inputFromUser.equals("?connect") && !inputFromUser.equals("?exit"));

        try {
            // Actually this is the socket that has information about the server that the client wants to connect to.
            Socket clientSocket = new Socket(serverIP, outPort);
            // Receive welcome message from the server upon connection.
            String welcomeMessage = (String) TCPPacketInteraction.receivePacket(clientSocket);
            System.out.println("\n" + welcomeMessage);
            // Receive valid commands that the user can use in order to perform operations.
            String listOfValidCommands = (String) TCPPacketInteraction.receivePacket(clientSocket);
            System.out.println(listOfValidCommands);
            while (true) {

                /*User thisUser = (User) TCPPacketInteraction.receivePacket(clientSocket);

                if (!thisUser.getUserInbox().isEmpty()) {
                    System.out.println(thisUser.getUserInbox().peek());
                }*/

                System.out.println("Type a valid command: ");
                String command = scanner.nextLine();
                // send the user's command to the server
                TCPPacketInteraction.sendPacket(clientSocket, command);
                String responseMessage = (String) TCPPacketInteraction.receivePacket(clientSocket);
                boolean isCommandValid = (boolean) TCPPacketInteraction.receivePacket(clientSocket);
                System.out.println("Server response: " + responseMessage);
                if (isCommandValid) {
                    // this switch handles the user command from the client perspective
                    switch (command) {
                        case "?placeItemForAuction":
                            while (true) {
                                String message = "You're about to place an item in auction. Do you wish to proceed? " +
                                        "(YES/NO)";
                                String choice;
                                do {
                                    System.out.println(message);
                                    choice = scanner.nextLine();
                                } while (!choice.equalsIgnoreCase("YES")
                                        && !choice.equalsIgnoreCase("NO"));
                                if (choice.equalsIgnoreCase("YES")) {
                                    System.out.println("Type item name: ");
                                    String itemName = scanner.nextLine();
                                    System.out.println("Type item description: ");
                                    String itemDescription = scanner.nextLine();
                                    boolean isItemPriceValid;
                                    double itemPrice = 0;
                                    do {
                                        System.out.println("Type item starting price: ");
                                        try {
                                            itemPrice = scanner.nextDouble();
                                            if (itemPrice <= 0) {
                                                throw new ItemPriceNegativeException("Item price must have a positive" +
                                                        " numerical value bigger than zero (0).");
                                            }
                                            isItemPriceValid = true;
                                        } catch (InputMismatchException e) {
                                            isItemPriceValid = false;
                                            System.out.println("Item price must consist of a numerical value.");
                                            scanner.nextLine();
                                        } catch (ItemPriceNegativeException e) {
                                            isItemPriceValid = false;
                                            System.out.println(e.getMessage());
                                            scanner.nextLine();
                                        }
                                    } while (!isItemPriceValid);
                                    scanner.nextLine();
                                    // We just the below with something, so that we won't be notified that the closingType
                                    // variable may not be initialised (later on in the builder of the auction object).
                                    String closingType = "";
                                    boolean isClosingTypeValid = false;
                                    System.out.println("Select one of the ways of closing the auction.\n" +
                                            "TYPE1 : Run for specific amount of time. When time ends, the participant " +
                                            "with the highest bid will get the item.\nTYPE2 : The auction will continue " +
                                            "as long as bids are placed.\nPlease type one of the options (e.g. TYPE1): ");
                                    do {
                                        try {
                                            closingType = scanner.nextLine();
                                            if (!closingType.equalsIgnoreCase("TYPE1")
                                                    && !closingType.equalsIgnoreCase("TYPE2")) {
                                                throw new SelectedClosingTypeMismatchException("One of the mentioned closing " +
                                                        "types should be selected.\nPlease type the way of closing the auction " +
                                                        "(either TYPE1 or TYPE2):");
                                            }
                                            isClosingTypeValid = true;
                                        } catch (SelectedClosingTypeMismatchException e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }while(!isClosingTypeValid);
                                    String timerMessage = closingType.equalsIgnoreCase("TYPE1")
                                            ? "Your auction will run for a specified amount of time.\nPlease type the " +
                                            "number of seconds in which the auction will be active (1 minute = 60 seconds," +
                                            "1 hour = 3600 seconds etc.): "
                                            : "Your auction will run for as long as bids are placed.\nPlease type the " +
                                            "number of seconds in which the auction will be active (1 minute = 60 seconds," +
                                            "1 hour = 3600 seconds etc.): ";
                                    int timer = 0;
                                    boolean isTimerValid;
                                    do {
                                        try {
                                            System.out.println(timerMessage);
                                            timer = scanner.nextInt();
                                            if (timer <= 0) {
                                                throw new TimerNegativeValueOrZeroException("Timer must consist of a " +
                                                        "positive numerical value bigger than zero (0): ");
                                            }
                                            isTimerValid = true;
                                        } catch(InputMismatchException e) {
                                            System.out.println("Timer must consist of a numeric value.");
                                            isTimerValid = false;
                                            // By having the line below (and in several other occasions), we protect our
                                            // program from getting into an infinite loop. That happens because after
                                            // reading either int or double from the keyboard, the scanner skips once
                                            // the next time that it attempts to read something from the user. This is
                                            // a very common issue with the scanners. More info here ->
                                            // https://stackoverflow.com/questions/13102045/scanner-is-skipping-nextline-after-using-next-or-nextfoo
                                            scanner.nextLine();
                                        } catch (TimerNegativeValueOrZeroException e) {
                                            System.out.println(e.getMessage());
                                            isTimerValid = false;
                                            scanner.nextLine();
                                        }
                                    }while(!isTimerValid);
                                    scanner.nextLine();
                                    // we firstly create the auction by calling the no args constructor in order to increment
                                    // the counter and set the auctionID.
                                    Auction auction = Auction.builder()
                                            .itemOnSale(Item.builder()
                                                    .itemName(itemName)
                                                    .itemDescription(itemDescription)
                                                    .itemStartingPrice(itemPrice)
                                                    .build())
                                            .closingType(closingType.equalsIgnoreCase("TYPE1")
                                                    ? AuctionClosingType.SPECIFIED_TIME_SET
                                                    : AuctionClosingType.BID_STARTS_TIMER)
                                            .closingTimer(timer)
                                            .build();
                                    // send that the server should proceed with the operation
                                    TCPPacketInteraction.sendPacket(clientSocket, true);
                                    // send the newly created auction to the server
                                    TCPPacketInteraction.sendPacket(clientSocket, auction);
                                    System.out.println("Auction successfully opened for item!");
                                    break;
                                } else {
                                    System.out.println("Operation abandoned.");
                                    TCPPacketInteraction.sendPacket(clientSocket, false);
                                    break;
                                }
                            }
                            break;
                        case "?listActiveAuctions":
                            List<Auction> auctionList = (List<Auction>) TCPPacketInteraction.receivePacket(clientSocket);
                            boolean atLeastOneOpen = false;
                            for (Auction auction:
                                 auctionList) {
                                if (auction.getAuctionStatus().equals(AuctionStatus.OPEN)) {
                                    atLeastOneOpen = true;
                                    break;
                                }
                            }
                            if (!auctionList.isEmpty() && atLeastOneOpen) {
                                System.out.println("\nAuction ID, Item Name, Item Description, Starting Price, " +
                                        "Highest Bid, Seller's Name");
                                for (Auction auction :
                                        auctionList) {
                                    if (auction.getAuctionStatus().equals(AuctionStatus.OPEN)) {
                                        System.out.println("----------------------------------------------------------" +
                                                "-------------------------");
                                        System.out.println(auction.getAuctionID() + ", " + auction.getItemOnSale().getItemName()
                                                + ", " + auction.getItemOnSale().getItemDescription() + ", " +
                                                auction.getItemOnSale().getItemStartingPrice() + ", " +
                                                ClientAuctionManagementOperations.getHighestBidPlacedInAuction(auctionList, auction.getAuctionID()).getBidValue()
                                                + ", " + auction.getOwner().getUsername());
                                    }
                                }
                                System.out.println("\n");
                            } else {
                                System.out.println("There are currently no auctions that are active.");
                            }
                            break;
                        case "?participateInAuction":
                            List<Auction> auctionList0 = (List<Auction>) TCPPacketInteraction.receivePacket(clientSocket);
                            // we also receive the user object in order to make sure that the auction that the user
                            // wants to participate is not one of his own (wouldn't make sense).
                            User thisUser0 = (User) TCPPacketInteraction.receivePacket(clientSocket);
                            int auctionID0 = ClientAuctionManagementOperations.handleAuctionIdInput(scanner,
                                                 clientSocket,
                                                 auctionList0,
                                                 thisUser0,
                                                "You cannot join an auction that " +
                                                        "was created by you.",
                                                "participateInAuction");
                            // if operation is abandoned by the user then inside the 'handleAuctionIdInput' method
                            // the server will be notified to abandon the operation. But for the client the loop will
                            // break, and it will return the default value of the 'auctionID' which is -1. So we check
                            // below that the operation will not be executed from the client-side.
                            if (auctionID0 != -1) {
                                TCPPacketInteraction.sendPacket(clientSocket, auctionID0);
                                System.out.println("You have now joined auction with ID: " + auctionID0 + ".");
                            }
                            break;
                        case "?placeABid":
                            // everytime we're changing variable names because they are already defined in the
                            // switch scope.
                            List<Auction> auctionList1 = (List<Auction>) TCPPacketInteraction.receivePacket(clientSocket);
                            User thisUser1 = (User) TCPPacketInteraction.receivePacket(clientSocket);
                            int auctionID1 = ClientAuctionManagementOperations.handleAuctionIdInput(scanner,
                                                 clientSocket,
                                                 auctionList1,
                                                 thisUser1,
                                                "You cannot bid on an auction that was created by you.",
                                                "placeABid");
                            if (auctionID1 != -1) {
                                TCPPacketInteraction.sendPacket(clientSocket, auctionID1);
                                Bid maxBid = ClientAuctionManagementOperations.getHighestBidPlacedInAuction(auctionList1, auctionID1);
                                // we don't need to try-catch for NullPointerException at the line below, since at
                                // this point of the program the auctionID will certainly match an already existing
                                // auction.
                                double itemStartingPrice = ClientAuctionManagementOperations.getAuctionItemStartingPrice(auctionList1, auctionID1);
                                boolean isOfferValid;
                                String offerStr;
                                double offer;
                                do {
                                    try {
                                        System.out.println("Make offer (or type -1 to cancel operation): ");
                                        offerStr = scanner.nextLine();
                                        offer = Double.parseDouble(offerStr);
                                        if (offer == -1) {
                                            // we cancel the offer.
                                            TCPPacketInteraction.sendPacket(clientSocket, offer);
                                            System.out.println("Cancelled offer.");
                                            break;
                                        }
                                        // if max bid is equal to -1 it means that there are no bids for that auction
                                        // yet. So, we want the next message to be displayed.
                                        if (maxBid.getBidValue() != -1) {
                                            if (offer <= maxBid.getBidValue()) {
                                                throw new BidInsufficientException("User with name: '" +
                                                        ClientAuctionManagementOperations.getUserWhoPlacedBid(auctionList1, auctionID1, maxBid).getUsername() +
                                                        "' has placed the largest bid of value: '" + maxBid.getBidValue() +
                                                        "'\nOffer was not submitted.");
                                            }
                                        }
                                        if (offer < itemStartingPrice) {
                                            throw new BidInsufficientException("The offer must consist of a value that " +
                                                    "is greater or equal to the item price specified by the owner of " +
                                                    "that item.");
                                        }
                                        if (offer <= 0) {
                                            throw new BidValueNegativeException("The offer cannot consist of a " +
                                                    "negative value or zero.");
                                        }
                                        TCPPacketInteraction.sendPacket(clientSocket, offer);
                                        System.out.println("Your offer is now placed!");
                                        isOfferValid = true;
                                    } catch (NumberFormatException e) {
                                        System.out.println("Offer must consist of a numeric value.");
                                        isOfferValid = false;
                                    } catch (BidInsufficientException | BidValueNegativeException e) {
                                        System.out.println(e.getMessage());
                                        isOfferValid = false;
                                    }
                                } while (!isOfferValid);
                            }
                        break;
                        case "?checkHighestBid":
                            List<Auction> auctionList2 = (List<Auction>) TCPPacketInteraction.receivePacket(clientSocket);
                            // For The exception message argument/parameter, we do not really care what value it will have since we won't
                            // be needing this in this case/command.
                            int auctionID2 = ClientAuctionManagementOperations.handleAuctionIdInput(scanner,
                                    clientSocket,
                                    auctionList2,
                                    null,
                                    "...",
                                    "checkHighestBid");
                            if (auctionID2 != -1) {
                                Bid highestBid = ClientAuctionManagementOperations.getHighestBidPlacedInAuction(auctionList2, auctionID2);
                                if (highestBid.getBidValue() != -1) {
                                    System.out.println("The highest bid for the auction with id=" + auctionID2 + " is " +
                                            highestBid.getBidValue() + " and was placed at " + highestBid.getTimeBidPlaced());
                                } else {
                                    System.out.println("There are not bids placed for the specified auction.");
                                }
                                // We have to send to the server any Integer value other than -1. That is because the
                                // server is waiting for a response from the user, since the user may abandon the
                                // operation. In the case of abandoning, the "handleAuctionIdInput" handles it and sends
                                // -1. In the success scenario, the server is waiting. That's why we need to send something.
                                TCPPacketInteraction.sendPacket(clientSocket, 1);
                            }
                        break;
                        case "?withdrawFromAuction":
                            List<Auction> auctionList3 = (List<Auction>) TCPPacketInteraction.receivePacket(clientSocket);
                            User thisUser3 = (User) TCPPacketInteraction.receivePacket(clientSocket);
                            int auctionID3 = ClientAuctionManagementOperations.handleAuctionIdInput(scanner,
                                    clientSocket,
                                    auctionList3,
                                    thisUser3,
                                    "You cannot withdraw from an auction that was created by you.",
                                    "withdrawFromAuction");
                            if (auctionID3 != -1) {
                                if (!thisUser3.equals(
                                        ClientAuctionManagementOperations.getUserWhoPlacedBid(auctionList3, auctionID3,
                                                ClientAuctionManagementOperations.getHighestBidPlacedInAuction(auctionList3, auctionID3)))) {
                                    System.out.println("You have successfully withdrawn from this auction!");
                                    TCPPacketInteraction.sendPacket(clientSocket, auctionID3);
                                }
                                else {
                                    System.out.println("You have the highest bid in this particular auction." +
                                            " You cannot withdraw until it finishes or someone else places a higher" +
                                            " bid.");
                                    TCPPacketInteraction.sendPacket(clientSocket, -1);
                                }
                            }
                        break;
                        case "?disconnect":
                            List<Auction> auctionList4 = (List<Auction>) TCPPacketInteraction.receivePacket(clientSocket);
                            User thisUser4 = (User) TCPPacketInteraction.receivePacket(clientSocket);
                            List<Auction> auctionsInWhichUserHighestBidder = ClientAuctionManagementOperations.checkIfHighestBidder(auctionList4, thisUser4);
                            if (!auctionsInWhichUserHighestBidder.isEmpty()) {
                                // It was advised by the IDE to replace += with append of the string builder.
                                StringBuilder auctionIDs = new StringBuilder("[");
                                for(int i=0; i<auctionsInWhichUserHighestBidder.size(); i++) {
                                    if (i != auctionsInWhichUserHighestBidder.size() - 1) {
                                        auctionIDs.append(auctionsInWhichUserHighestBidder.get(i).getAuctionID()).append(", ");
                                    }
                                    else {
                                        auctionIDs.append(auctionsInWhichUserHighestBidder.get(i).getAuctionID()).append("]");
                                    }
                                }
                                System.out.println("You cannot currently disconnect from the system.\nYou have the " +
                                        "highest bid in the auction(s) with IDs' = " + auctionIDs);
                                TCPPacketInteraction.sendPacket(clientSocket, false);
                            }
                            else {
                                // User can disconnect from the system.
                                TCPPacketInteraction.sendPacket(clientSocket, true);
                                String goodbyeMessage = (String) TCPPacketInteraction.receivePacket(clientSocket);
                                System.out.println(goodbyeMessage);
                                System.exit(0);
                            }
                        break;
                        case "?readInbox":
                            TCPPacketInteraction.sendPacket(clientSocket, true);
                            User thisUser = (User) TCPPacketInteraction.receivePacket(clientSocket);
                            ExecutorService executorService = Executors.newSingleThreadExecutor();
                            PrintInbox printInbox = new PrintInbox(thisUser);
                            Future<Boolean> future = executorService.submit(printInbox);
                            while (!future.isDone()) {
                                TCPPacketInteraction.sendPacket(clientSocket, true);
                                User updatedUser = (User) TCPPacketInteraction.receivePacket(clientSocket);
                                printInbox.setUser(updatedUser);
                            }
                            executorService.shutdown();
                            TCPPacketInteraction.sendPacket(clientSocket, false);
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection refused.\nServer is probably closed or under maintenance.");
        }

    }

}