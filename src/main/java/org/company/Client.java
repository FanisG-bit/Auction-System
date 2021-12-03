package org.company;

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
                                }while (!choice.equalsIgnoreCase("YES")
                                        && !choice.equalsIgnoreCase("NO"));
                                if (choice.equalsIgnoreCase("YES")) {
                                    System.out.println("Type item name: ");
                                    String itemName = scanner.nextLine();
                                    System.out.println("Type item description: ");
                                    String itemDescription = scanner.nextLine();
                                    System.out.println("Type item price: ");
                                    int itemPrice = scanner.nextInt();

                                } else {
                                    System.out.println("Operation abandoned.");
                                    break;
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