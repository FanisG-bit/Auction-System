package org.company;

import org.company.service.TCPPacketInteraction;

import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler implements Runnable {

    private Socket client;

    public ConnectionHandler(Socket connectionSocket) {
        client = connectionSocket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String clientCommand = (String) TCPPacketInteraction.receivePacket(client);
                System.out.println("Client's command is: " + clientCommand);
                TCPPacketInteraction.sendPacket(client, "Fuck you.");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}