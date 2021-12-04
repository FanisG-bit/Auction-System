package org.company;

import org.company.model.Auction;
import org.company.model.Item;
import org.company.service.TCPPacketInteraction;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        int outPort = 6666;
        String serverIP = "127.0.0.1";
        Scanner scanner = new Scanner(System.in);

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
                // TODO for some reason after the operation placeItemForAuction completes, it gets as an input
                //  empty which is why it prints 'Type a valid command: ' twice. This has to do with the scanner I think.
                System.out.println("Type a valid command: ");
                String command = scanner.nextLine();
                if(command.length() >= 1) {
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
                                    }while (!choice.equalsIgnoreCase("YES")
                                            && !choice.equalsIgnoreCase("NO"));
                                    if (choice.equalsIgnoreCase("YES")) {
                                        System.out.println("Type item name: ");
                                        String itemName = scanner.nextLine();
                                        System.out.println("Type item description: ");
                                        String itemDescription = scanner.nextLine();
                                        System.out.println("Type item starting price: ");
                                        // TODO catch cases where user inputs bullshit
                                        int itemPrice = scanner.nextInt();
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
                                        break;
                                    } else {
                                        System.out.println("Operation abandoned.");
                                        TCPPacketInteraction.sendPacket(clientSocket, false);
                                        break;
                                    }
                                }
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection refused.\nServer is probably closed or under maintenance.");
        }

    }

}