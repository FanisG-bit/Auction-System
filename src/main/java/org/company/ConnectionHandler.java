package org.company;

import org.company.model.Auction;
import org.company.model.AuctionClosingType;
import org.company.model.Bid;
import org.company.model.User;
import org.company.service.DisconnectHandler;
import org.company.service.TCPPacketInteraction;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

public class ConnectionHandler implements Runnable {

    private Socket client;
    // IGNORE BELOW COMMENT FOR NOW BECAUSE IT'S PROBABLY INCORRECT
    // 'place a bid' and 'withdraw from an auction' are excluded from the list below, since they are operations
    // that take place after registering (participating) on an auction.
    private String listOfValidCommands = "List of valid commands (use '?' before entering one e.g. ?command):\n" +
            "placeItemForAuction,\nlistActiveAuctions,\nparticipateInAuction,\nplaceABid,\ncheckHighestBid,\nwithdrawFromAuction," +
            "\ndisconnect.\n";
    private String[] commands = new String[]{"placeItemForAuction", "listActiveAuctions", "participateInAuction",
            "placeABid", "checkHighestBid", "withdrawFromAuction", "disconnect"};
    // reference to the list of all the auctions that are currently on the server.
    private List<Auction> auctionsList;
    // reference to the list of users that are currently on the server.
    private List<User> usersList;
    // this is essentially the object of the user that is interacting with the server on this thread.
    private User currentUser;

    private String currentUsername;

    public ConnectionHandler(Socket connectionSocket, List<Auction> auctionsList, List<User> usersList, String username) {
        client = connectionSocket;
        this.auctionsList = auctionsList;
        this.usersList = usersList;
        currentUsername = username;
    }

    @Override
    public void run() {
        // the first thing is to send a welcome message to the client.
        try {
            // we then retrieve the object of the current user.
            currentUser = retrieveUserBasedOnName();
            TCPPacketInteraction.sendPacket(client, "Welcome to the auction system! You're identified as '" +
                    currentUser.getUsername() + "'\n");
            TCPPacketInteraction.sendPacket(client, listOfValidCommands);
            /*User user = retrieveUser();
            if (user != null) {
                currentUser = user;
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                String clientCommand = (String) TCPPacketInteraction.receivePacket(client);
                boolean clientCommandValid = isClientCommandValid(clientCommand);
                if (!clientCommandValid) {
                    TCPPacketInteraction.sendPacket(client, "Command is not valid. Please try again.");
                    // second argument could be replaced with 'clientCommandValid', but it is more clear writing
                    // it like this.
                    TCPPacketInteraction.sendPacket(client, false);
                } else {
                    TCPPacketInteraction.sendPacket(client, "Command is valid!");
                    TCPPacketInteraction.sendPacket(client, true);
                    handleCommand(clientCommand);
                }
                System.out.println(usersList);
                System.out.println(auctionsList);
                // System.out.println("Client's command is: " + clientCommand);
            } catch (IOException | ClassNotFoundException e) {
                //System.err.println("User with IP " + client.getInetAddress() + " disconnected.");
                System.err.println("User with username '" + currentUser.getUsername() + "' disconnected.");
                break;
            }
        }
    }

    private User retrieveUserBasedOnName() {
        User currentUser = null;
        for (User user : usersList) {
            if (user.getUsername().equals(currentUsername)) {
                currentUser = user;
                break;
            }
        }
        return currentUser;
    }

    private User retrieveUser() {
        User currentUser = null;
        for (User user : usersList) {
            if (user.getIPAddress().getHostAddress().equals(client.getInetAddress().getHostAddress())) {
                currentUser = user;
                break;
            }
        }
        return currentUser;
    }

    // this method handles the user command from the server perspective
    private void handleCommand(String clientCommand) {
        switch (clientCommand) {
            case "?placeItemForAuction":
                while (true) {
                    try {
                        boolean proceedWithOperation = (boolean) TCPPacketInteraction.receivePacket(client);
                        if (proceedWithOperation) {
                            Auction clientNewAuction = (Auction) TCPPacketInteraction.receivePacket(client);
                            Auction.incrementCounter();
                            clientNewAuction.setAuctionID(Auction.counter);
                            clientNewAuction.setOwner(currentUser);
                            clientNewAuction.setParticipants(new ArrayList<>());
                            clientNewAuction.setBidsPlaced(new HashMap<>());
                            auctionsList.add(clientNewAuction);
                            // The lines below refer to the first way of closing an auction where we set a timer and after
                            // that timer finishes, the highest bidder gets the item.
                            LocalDateTime afterHowManySecondsItCloses =
                                    LocalDateTime.now().plusSeconds(clientNewAuction.getClosingTimer());
                            // We convert the LocalDateTime to a Date object since that is the only acceptable form of
                            // the second parameter. The steps are:
                            // - convert LocalDateTime to ZonedDateTime,
                            // - pass the system's default time-zone as the argument of the atZone method,
                            // - convert ZonedDateTime to an Instant object,
                            // - obtain an instance of Date from the Instant object by using the Date.from
                            // Help was acquired from -> https://www.baeldung.com/java-date-to-localdate-and-localdatetime
                            new Timer().schedule(new DisconnectHandler(AuctionClosingType.SPECIFIED_TIME_SET, auctionsList,
                                            clientNewAuction, currentUser, usersList),
                                    Date.from(afterHowManySecondsItCloses.atZone(ZoneId.systemDefault()).toInstant()));
                            break;
                        } else {
                            // user abandons operation, so the loop breaks without something happening
                            break;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            break;
            case "?listActiveAuctions":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            break;
            case "?participateInAuction":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                    TCPPacketInteraction.sendPacket(client, currentUser);
                    int auctionID = (int) TCPPacketInteraction.receivePacket(client);
                    if (auctionID == -1) {
                        // operation has been abandoned by the user.
                        // we break from the switch.
                        break;
                    } else {
                        auctionID--; // that is because list index starts from 0 but auction IDs start from 1 and increment.
                        auctionsList.get(auctionID).getParticipants().add(currentUser);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            break;
            case "?placeABid":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                    TCPPacketInteraction.sendPacket(client, currentUser);
                    int auctionID = (int) TCPPacketInteraction.receivePacket(client);
                    if (auctionID == -1) {
                        // operation has been abandoned by the user on the state of CHOOSING AN AUCTION.
                        break;
                    } else {
                        double clientOffer = (double) TCPPacketInteraction.receivePacket(client);
                        if (clientOffer == -1) {
                            // operation has been abandoned by the user on the state of PLACING AN OFFER.
                            break;
                        } else {
                            auctionID--;
                            auctionsList.get(auctionID)
                                    .getBidsPlaced()
                                    .put(currentUser, new Bid(clientOffer, LocalTime.now()));
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            break;
            case "?checkHighestBid":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                    int auctionID = (int) TCPPacketInteraction.receivePacket(client);
                    // Operation has been abandoned by the user. Not sure if the break is needed
                    // at this point. Most probably not. There is also nothing that the server
                    // can do with the response from the client.
                    // We have to retrieve a response just because in the client, we use the
                    // "handleAuctionIdInput" method which sends -1 to the server, if the operation is
                    // abandoned by the user while choosing for an auction.
                    if (auctionID == -1) {
                        break;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            break;
            case "?withdrawFromAuction":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                    TCPPacketInteraction.sendPacket(client, currentUser);
                    int auctionID = (int) TCPPacketInteraction.receivePacket(client);
                    if (auctionID != -1) {
                        // We remove (withdraw) the user from the specified auction.
                        auctionID--;
                        auctionsList.get(auctionID).getParticipants().remove(currentUser);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            break;
            case "?disconnect":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                    TCPPacketInteraction.sendPacket(client, currentUser);
                    boolean shouldDisconnect = (boolean) TCPPacketInteraction.receivePacket(client);
                    if (shouldDisconnect) {
                        // We remove the user from any auctions in which he/she is a participant.
                        for (Auction auction:
                             auctionsList) {
                            if (auction.getParticipants().contains(currentUser)) {
                                auction.getParticipants().remove(currentUser);
                            }
                        }
                        // We also remove that person from the list that contains all the users currently
                        // on the system.
                        usersList.remove(currentUser);
                        TCPPacketInteraction.sendPacket(client, "Goodbye!");
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            break;
        }
    }

    private boolean isClientCommandValid(String command) {
        boolean isValid = true;
        if (!command.startsWith("?")) {
            isValid = false;
        }
        if (!Arrays.asList(commands).contains(command.substring(1))) {
            isValid = false;
        }
        return isValid;
    }

}