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
            Socket clientSocket = new Socket(serverIP, outPort);
            System.out.println("Connected to server with IP: " + clientSocket.getInetAddress() +
                    " on port: " + outPort);
            while (true) {
                System.out.println("Type a valid command: ");
                String command = scanner.nextLine();
                TCPPacketInteraction.sendPacket(clientSocket, command);
                String responseMessage = (String) TCPPacketInteraction.receivePacket(clientSocket);
                System.out.println("Server response: " + responseMessage);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}