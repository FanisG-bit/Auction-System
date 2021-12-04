package org.company;

import org.company.model.Auction;
import org.company.model.User;
import org.company.service.TCPPacketInteraction;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ConnectionHandler implements Runnable {

    private Socket client;
    // 'place a bid' and 'withdraw from an auction' are excluded from the list below, since they are operations
    // that take place after registering (participating) on an auction.
    private String listOfValidCommands = "List of valid commands (use '?' before entering one e.g. ?command):\n" +
            "placeItemForAuction,\nlistActiveAuctions,\nparticipateInAuction,\ncheckHighestBid,\ndisconnect.\n";
    private String[] commands = new String[]{"placeItemForAuction", "listActiveAuctions", "participateInAuction",
            "checkHighestBid", "disconnect"};
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
            TCPPacketInteraction.sendPacket(client, "Welcome to the auction system! You're identified as '" +
                    client.getInetAddress().getHostAddress() + "'\n");
            TCPPacketInteraction.sendPacket(client, listOfValidCommands);
            // we then retrieve the object of the current user.
            /*User user = retrieveUser();
            if (user != null) {
                currentUser = user;
            }*/
            currentUser = retrieveUserBasedOnName();
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
                System.err.println("User with IP " + client.getInetAddress() + " disconnected.");
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
                            auctionsList.add(clientNewAuction);
                            break;
                        } else {
                            // user abandons operation, so the loop breaks without something happening
                            break;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
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