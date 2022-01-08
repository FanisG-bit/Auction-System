package org.company;

import org.company.model.Auction;
import org.company.model.User;
import org.company.service.ServerCommandHandler;
import org.company.service.TCPPacketInteraction;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

/** Each instance of this class can be run by a thread in order for the server to be able to communicate with a lot of
 *  users in parallel.
 *  @author Theofanis Gkoufas
 */

@SuppressWarnings("FieldMayBeFinal")
public class ConnectionHandler implements Runnable {

    private Socket client;
    @SuppressWarnings({"FieldCanBeLocal", "TextBlockMigration"})
    private String listOfValidCommands = "List of valid commands (use '?' before entering one e.g. ?command):\n" +
            "placeItemForAuction,\nlistActiveAuctions,\nparticipateInAuction,\nplaceABid,\ncheckHighestBid,\nwithdrawFromAuction," +
            "\nreadInbox,\ndisconnect.\n";
    private String[] commands = new String[]{"placeItemForAuction", "listActiveAuctions", "participateInAuction",
            "placeABid", "checkHighestBid", "withdrawFromAuction", "readInbox", "disconnect"};
    // Reference to the list of all the auctions that are currently on the server.
    private List<Auction> auctionsList;
    // Reference to the list of users that are currently on the server.
    private List<User> usersList;
    // This is essentially the object of the user that is interacting with the server on this thread.
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
        // The first thing is to send a welcome message to the client.
        try {
            // We then retrieve the object of the current user.
            currentUser = ServerCommandHandler.retrieveUserBasedOnName(usersList, currentUsername);
            TCPPacketInteraction.sendPacket(client, "Welcome to the auction system! You're identified as '" +
                    currentUser.getUsername() + "'\n");
            TCPPacketInteraction.sendPacket(client, listOfValidCommands);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                String clientCommand = (String) TCPPacketInteraction.receivePacket(client);
                boolean clientCommandValid;
                if (!clientCommand.isEmpty()) {
                   clientCommandValid = ServerCommandHandler.isClientCommandValid(clientCommand, commands);
                } else {
                    clientCommandValid = false;
                }
                if (!clientCommandValid) {
                    TCPPacketInteraction.sendPacket(client, "Command is not valid. Please try again.");
                    // Second argument could be replaced with 'clientCommandValid', but it is more clear writing
                    // it like this.
                    TCPPacketInteraction.sendPacket(client, false);
                } else {
                    TCPPacketInteraction.sendPacket(client, "Command is valid!");
                    TCPPacketInteraction.sendPacket(client, true);
                    ServerCommandHandler.handleCommand(clientCommand, client, currentUser, usersList, auctionsList, secondScenarioTimers);
                }
                /*  Note: below statements can be commented out in order to help in debugging!
                    System.out.println(usersList);
                    System.out.println(auctionsList);
                    System.out.println(secondScenarioTimers);
                */
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("User with username '" + currentUser.getUsername() + "' disconnected.");
                break;
            }
        }
    }

}