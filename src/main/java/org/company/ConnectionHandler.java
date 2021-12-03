package org.company;

import org.company.model.Auction;
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
    private List<Auction> auctionsList;

    public ConnectionHandler(Socket connectionSocket, List<Auction> auctionsList) {
        client = connectionSocket;
        this.auctionsList = auctionsList;
    }

    @Override
    public void run() {
        // the first thing is to send a welcome message to the client.
        try {
            TCPPacketInteraction.sendPacket(client, "Welcome to the auction system! You're identified as '" +
                    client.getInetAddress().getHostAddress() + "'\n");
            TCPPacketInteraction.sendPacket(client, listOfValidCommands);
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
                // System.out.println("Client's command is: " + clientCommand);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("User with IP " + client.getInetAddress() + " disconnected.");
                break;
            }
        }
    }

    // this method handles the user command from the server perspective
    private void handleCommand(String clientCommand) {
        switch (clientCommand) {
            case "?placeItemForAuction":
                while (true) {

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