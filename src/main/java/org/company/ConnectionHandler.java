package org.company;

import org.company.model.Auction;
import org.company.model.AuctionClosingType;
import org.company.model.Bid;
import org.company.model.User;
import org.company.service.DisconnectHandler;
import org.company.service.ServerAuctionManagementOperations;
import org.company.service.TCPPacketInteraction;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

public class ConnectionHandler implements Runnable {

    private Socket client;
    private String listOfValidCommands = "List of valid commands (use '?' before entering one e.g. ?command):\n" +
            "placeItemForAuction,\nlistActiveAuctions,\nparticipateInAuction,\nplaceABid,\ncheckHighestBid,\nwithdrawFromAuction," +
            "\nreadInbox,\ndisconnect.\n";
    private String[] commands = new String[]{"placeItemForAuction", "listActiveAuctions", "participateInAuction",
            "placeABid", "checkHighestBid", "withdrawFromAuction", "readInbox", "disconnect"};
    // reference to the list of all the auctions that are currently on the server.
    private List<Auction> auctionsList;
    // reference to the list of users that are currently on the server.
    private List<User> usersList;
    // this is essentially the object of the user that is interacting with the server on this thread.
    private User currentUser;
    private String currentUsername;
    private Map<Auction, Timer> secondScenarioTimers;

    public ConnectionHandler(Socket connectionSocket, List<Auction> auctionsList, List<User> usersList, String username, Map<Auction, Timer> secondScenarioTimers) {
        client = connectionSocket;
        this.auctionsList = auctionsList;
        this.usersList = usersList;
        currentUsername = username;
        this.secondScenarioTimers = secondScenarioTimers;
    }

    @Override
    public void run() {
        // the first thing is to send a welcome message to the client.
        try {
            // we then retrieve the object of the current user.
            currentUser = ServerAuctionManagementOperations.retrieveUserBasedOnName(usersList, currentUsername);
            TCPPacketInteraction.sendPacket(client, "Welcome to the auction system! You're identified as '" +
                    currentUser.getUsername() + "'\n");
            TCPPacketInteraction.sendPacket(client, listOfValidCommands);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {

                /*TCPPacketInteraction.sendPacket(client, currentUser);

                if (!currentUser.getUserInbox().isEmpty()) {
                    currentUser.getUserInbox().poll();
                }*/

                String clientCommand = (String) TCPPacketInteraction.receivePacket(client);
                boolean clientCommandValid;
                if (!clientCommand.isEmpty()) {
                   clientCommandValid = ServerAuctionManagementOperations.isClientCommandValid(clientCommand, commands);
                } else {
                    clientCommandValid = false;
                }
                if (!clientCommandValid) {
                    TCPPacketInteraction.sendPacket(client, "Command is not valid. Please try again.");
                    // second argument could be replaced with 'clientCommandValid', but it is more clear writing
                    // it like this.
                    TCPPacketInteraction.sendPacket(client, false);
                } else {
                    TCPPacketInteraction.sendPacket(client, "Command is valid!");
                    TCPPacketInteraction.sendPacket(client, true);
                    ServerAuctionManagementOperations.handleCommand(clientCommand, client, currentUser, usersList, auctionsList, secondScenarioTimers);
                }
                // System.out.println(usersList);
                // System.out.println(auctionsList);
                // System.out.println(secondScenarioTimers);
                // System.out.println("Client's command is: " + clientCommand);
            } catch (IOException | ClassNotFoundException e) {
                //System.err.println("User with IP " + client.getInetAddress() + " disconnected.");
                System.err.println("User with username '" + currentUser.getUsername() + "' disconnected.");
                break;
            }
        }
    }

}