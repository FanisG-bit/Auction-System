package org.company;

import org.company.exceptions.*;
import org.company.model.Auction;
import org.company.model.Bid;
import org.company.model.Item;
import org.company.model.User;
import org.company.service.TCPPacketInteraction;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

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
                    ?exit""");
        do {
            System.out.println("\nType a Menu command: ");
            inputFromUser = scanner.nextLine();
            switch (inputFromUser) {
                case "?connect":
                    break;
                case "?exit":
                    System.exit(0);
            }
        } while (!inputFromUser.equals("?connect") && !inputFromUser.equals("?exit"));

        try {
            // Actually this is the socket that has information about the server that the client wants to connect to.
            Socket clientSocket = new Socket(serverIP, outPort);
            // Receive welcome message from the server upon connection.
            String welcomeMessage = (String) TCPPacketInteraction.receivePacket(clientSocket);
            System.out.println(welcomeMessage);
            // Receive valid commands that the user can use in order to perform operations.
            String listOfValidCommands = (String) TCPPacketInteraction.receivePacket(clientSocket);
            System.out.println(listOfValidCommands);
            while (true) {
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
                                    // we firstly create the auction by calling the no args constructor in order to increment
                                    // the counter and set the auctionID.
                                    Auction auction = Auction.builder()
                                            .itemOnSale(Item.builder()
                                                    .itemName(itemName)
                                                    .itemDescription(itemDescription)
                                                    .itemStartingPrice(itemPrice)
                                                    .build())
                                            .build();
                                    // TODO also the client needs to set the way that the auction will close
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
                            if (!auctionList.isEmpty()) {
                                System.out.println("\nAuction ID, Item Name, Item Description, Starting Price, " +
                                        "Highest Bid, Seller's Name");
                                for (Auction auction :
                                        auctionList) {
                                    System.out.println("----------------------------------------------------------" +
                                            "-------------------------");
                                    System.out.println(auction.getAuctionID() + ", " + auction.getItemOnSale().getItemName()
                                            + ", " + auction.getItemOnSale().getItemDescription() + ", " +
                                            auction.getItemOnSale().getItemStartingPrice() + ", " +
                                            getHighestBidPlacedInAuction(auctionList, auction.getAuctionID())
                                            + ", " + auction.getOwner().getUsername());
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
                            int auctionID0 = handleAuctionIdInput(scanner,
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
                            int auctionID1 = handleAuctionIdInput(scanner,
                                                 clientSocket,
                                                 auctionList1,
                                                 thisUser1,
                                                "You cannot bid on an auction that was created by you.",
                                                "placeABid");
                            if (auctionID1 != -1) {
                                TCPPacketInteraction.sendPacket(clientSocket, auctionID1);
                                double maxBid = getHighestBidPlacedInAuction(auctionList1, auctionID1);
                                // we don't need to try-catch for NullPointerException at the line below, since at
                                // this point of the program the auctionID will certainly match an already existing
                                // auction.
                                double itemStartingPrice = getAuctionItemStartingPrice(auctionList1, auctionID1);
                                boolean isOfferValid = true;
                                String offerStr;
                                double offer;
                                do {
                                    try {
                                        System.out.println("Make offer (or type -1 to cancel operation): ");
                                        offerStr = scanner.nextLine();
                                        offer = Double.parseDouble(offerStr);
                                        if (offer == -1) {
                                            // we cancel the offer.
                                            TCPPacketInteraction.sendPacket(clientSocket, -1);
                                            System.out.println("Cancelled offer.");
                                            break;
                                        }
                                        // if max bid is equal to -1 it means that there are no bids for that auction
                                        // yet. So, we want the next message to be displayed.
                                        if (maxBid != -1) {
                                            if (offer <= maxBid) {
                                                throw new BidInsufficientException("There is a higher bid for this item " +
                                                        "placed by another user. Your bid was not submitted.");
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
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection refused.\nServer is probably closed or under maintenance.");
        }

    }

    private static double getAuctionItemStartingPrice(List<Auction> auctionList, int auctionID) throws NullPointerException {
        Auction thisAuction = null;
        for (Auction auction:
                auctionList) {
            if (auction.getAuctionID() == auctionID) {
                thisAuction = auction;
            }
        }
        return thisAuction.getItemOnSale().getItemStartingPrice();
    }

    private static double getHighestBidPlacedInAuction(List<Auction> auctionList, int auctionID) {
        HashMap<User, Bid> bidsPlaced = null;
        // if value stays at -1 it means that there are currently no bids on the specified auction.
        double maxBidValue = -1;
        for (Auction auction:
                auctionList) {
            if (auction.getAuctionID() == auctionID) {
                bidsPlaced = auction.getBidsPlaced();
            }
        }
        if (bidsPlaced != null) {
            if (bidsPlaced.isEmpty()) {
                return -1;
            } else {
                for (Bid bid:
                        bidsPlaced.values()) {
                    if (maxBidValue < bid.getBidValue()) {
                        maxBidValue = bid.getBidValue();
                    }
                }
            }
        }
        return maxBidValue;
    }

    private static int handleAuctionIdInput(Scanner scanner,
                                             Socket clientSocket,
                                             List<Auction> auctionList,
                                             User thisUser,
                                             String exceptionMessage,
                                             String operationName)
                                             throws IOException {
        String auctionIDStr;
        int auctionID = -1;
        boolean isAuctionIDValid = true;
        do {
            if (operationName.equals("participateInAuction")) {
                System.out.println("Type the ID of the auction that you wish to participate or -1" +
                        " to abandon operation:");
            } else if (operationName.equals("placeABid")) {
                System.out.println("Type the ID of the auction that you wish to place a bid " +
                        "or -1 to abandon operation:");
            }
            auctionIDStr = scanner.nextLine();
            try {
                auctionID = Integer.parseInt(auctionIDStr);
                boolean isInList = false;
                boolean isOwnedByUser = false;
                boolean isOperationAbandoned = false;
                boolean isUserInParticipants = false;
                if (auctionID == -1) {
                    isOperationAbandoned = true;
                } else {
                    for (Auction auction :
                            auctionList) {
                        if (auction.getAuctionID() == auctionID) {
                            isInList = true;
                            if(auction.getOwner().getUsername().equals(thisUser.getUsername())) {
                                isOwnedByUser = true;
                                break;
                            }
                            if (operationName.equals("participateInAuction") || operationName.equals("placeABid")) {
                                isUserInParticipants = isUserInParticipants(auctionList, thisUser, auctionID);
                                break;
                            }
                            break;
                        }
                    }
                }
                if (isOperationAbandoned) {
                    System.out.println("Operation abandoned!");
                    // we let the server know that the operation has been abandoned.
                    TCPPacketInteraction.sendPacket(clientSocket, -1);
                    break;
                }
                if (!isInList) {
                    throw new AuctionIdNotInListException("Given Auction ID does not match to any " +
                            "auctions currently on the system.");
                }
                if(isOwnedByUser) {
                    throw new UserJoinsOwnAuctionException(exceptionMessage);
                }
                if (operationName.equals("participateInAuction")) {
                    if (isUserInParticipants) {
                        throw new UserAlreadyInAuctionException("You have already joined this auction. Please choose " +
                                "another one.");
                    }
                }
                if (operationName.equals("placeABid")) {
                    if (!isUserInParticipants) {
                        throw new UserBidsOwnAuctionException("You cannot place a bid in an auction in which you" +
                                " are not a participant. You need to join first.");
                    }
                }
                // if execution reaches this point, then it means that nothing went bad (no exceptions or operation
                // abandoned) and that 'isAuctionIDValid' has a value of true. So the condition of the loop is false
                // and the main operation will be fulfilled.
                isAuctionIDValid = true;
            } catch (NumberFormatException e) {
                System.out.println("Auction ID must consist of a numeric value.");
                isAuctionIDValid = false;
            } catch (AuctionIdNotInListException | UserJoinsOwnAuctionException | UserAlreadyInAuctionException | UserBidsOwnAuctionException e) {
                System.out.println(e.getMessage());
                isAuctionIDValid = false;
            }
        } while (!isAuctionIDValid);
        return auctionID;
    }

    private static boolean isUserInParticipants(List<Auction> auctionList, User thisUser, int auctionID) {
        boolean isUserInParticipants = false;
        for (Auction auction:
                auctionList) {
            if (auction.getAuctionID() == auctionID) {
                if (auction.getParticipants().contains(thisUser)) {
                    isUserInParticipants = true;
                }
            }
        }
        return isUserInParticipants;
    }

}