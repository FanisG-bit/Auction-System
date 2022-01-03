package org.company.service;

import org.company.exceptions.AuctionIdNotInListException;
import org.company.exceptions.UserAlreadyInAuctionException;
import org.company.exceptions.UserJoinsOwnAuctionException;
import org.company.exceptions.UserNotAuctionParticipantException;
import org.company.model.Auction;
import org.company.model.Bid;
import org.company.model.User;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ClientAuctionManagementOperations {

    public static List<Auction> checkIfHighestBidder(List<Auction> auctionList, User thisUser) {
        List<Auction> auctionsInWhichUserHighestBidder = new ArrayList<>();
        for (Auction auction:
                auctionList) {
            if (auction.getParticipants().contains(thisUser)) {
                if (getUserWhoPlacedBid(auctionList,
                        auction.getAuctionID(),
                        getHighestBidPlacedInAuction(auctionList, auction.getAuctionID()))
                        .equals(thisUser))
                {
                    auctionsInWhichUserHighestBidder.add(auction);
                }
            }
        }
        return auctionsInWhichUserHighestBidder;
    }

    public static double getAuctionItemStartingPrice(List<Auction> auctionList, int auctionID) throws NullPointerException {
        Auction thisAuction = null;
        for (Auction auction:
                auctionList) {
            if (auction.getAuctionID() == auctionID) {
                thisAuction = auction;
            }
        }
        return thisAuction.getItemOnSale().getItemStartingPrice();
    }

    public static Bid getHighestBidPlacedInAuction(List<Auction> auctionList, int auctionID) {
        HashMap<User, Bid> bidsPlaced = null;
        // The method will return the bid with a value of -1 in the case that there are
        // no bids placed.
        // The bidsPlaced which is initiated as null could produce a (Null) Exception but in
        // the way that it is being used in this program, there is no way that this
        // could happen, since we have already checked that the auctionID exists.
        Bid maxBid = Bid.builder()
                .bidValue(-1.0)
                .build();
        for (Auction auction:
                auctionList) {
            if (auction.getAuctionID() == auctionID) {
                bidsPlaced = auction.getBidsPlaced();
            }
        }
        if (bidsPlaced != null) {
            if (bidsPlaced.isEmpty()) {
                return maxBid;
            } else {
                for (Bid bid:
                        bidsPlaced.values()) {
                    if (maxBid.getBidValue() < bid.getBidValue()) {
                        maxBid = bid;
                    }
                }
            }
        }
        return maxBid;
    }

    public static User getUserWhoPlacedBid(List<Auction> auctionList, int auctionID, Bid bid) {
        // Retrieve key (user) based on value (bid).
        // Got inspiration from -> https://www.baeldung.com/java-map-key-from-value
        HashMap<User, Bid> bidsPlaced = null;
        for (Auction auction:
                auctionList) {
            if (auction.getAuctionID() == auctionID) {
                bidsPlaced = auction.getBidsPlaced();
            }
        }
        User userWhoPlacedThatBid = null;
        for (Map.Entry<User, Bid> entry:
                bidsPlaced.entrySet()) {
            if (entry.getValue().equals(bid)) {
                userWhoPlacedThatBid = entry.getKey();
            }
        }
        return userWhoPlacedThatBid;
    }

    public static int handleAuctionIdInput(Scanner scanner,
                                            Socket clientSocket,
                                            List<Auction> auctionList,
                                            User thisUser,
                                            String exceptionMessage,
                                            String operationName) throws IOException {
        String auctionIDStr;
        int auctionID = -1;
        boolean isAuctionIDValid;
        do {
            if (operationName.equals("participateInAuction")) {
                System.out.println("Type the ID of the auction that you wish to participate or -1" +
                        " to abandon operation:");
            } else if (operationName.equals("placeABid")) {
                System.out.println("Type the ID of the auction that you wish to place a bid " +
                        "or -1 to abandon operation:");
            } else if (operationName.equals("checkHighestBid")) {
                System.out.println("Type the ID of the auction that you wish to see the " +
                        "highest bid placed or -1 to abandon operation:");
            } else if (operationName.equals("withdrawFromAuction")) {
                System.out.println("Type the ID of the auction from which you wish to withdraw" +
                        " or -1 to abandon operation:");
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
                            // we write the below statement because in the case of command "checkHighestBid" etc.
                            // we do not pass a user because it is not needed. That way we ensure that we won't get
                            // an exception (thisUser being null).
                            if (thisUser != null) {
                                if (auction.getOwner().getUsername().equals(thisUser.getUsername())) {
                                    isOwnedByUser = true;
                                    break;
                                }
                            }
                            if (operationName.equals("participateInAuction") || operationName.equals("placeABid")
                                    || operationName.equals("withdrawFromAuction")) {
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
                        throw new UserNotAuctionParticipantException("You cannot place a bid in an auction in which you" +
                                " are not a participant. You need to join first.");
                    }
                }
                if (operationName.equals("withdrawFromAuction")) {
                    if (!isUserInParticipants) {
                        throw new UserNotAuctionParticipantException("You cannot withdraw from an auction in which" +
                                "you are not a participant. You need to join first.");
                    }
                }
                // if execution reaches this point, then it means that nothing went bad (no exceptions or operation
                // abandoned) and that 'isAuctionIDValid' has a value of true. So the condition of the loop is false
                // and the main operation will be fulfilled.
                isAuctionIDValid = true;
            } catch (NumberFormatException e) {
                System.out.println("Auction ID must consist of a numeric value.");
                isAuctionIDValid = false;
            } catch (AuctionIdNotInListException | UserJoinsOwnAuctionException | UserAlreadyInAuctionException | UserNotAuctionParticipantException e) {
                System.out.println(e.getMessage());
                isAuctionIDValid = false;
            }
        } while (!isAuctionIDValid);
        return auctionID;
    }

    public static boolean isUserInParticipants(List<Auction> auctionList, User thisUser, int auctionID) {
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