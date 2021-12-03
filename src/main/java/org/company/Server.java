package org.company;

import org.company.model.Auction;
import org.company.model.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {

    public static void main(String[] args) {

        int inPort = 6666;
        Socket connectionSocket = null;

        /*read page in: https://www.codejava.net/java-core/collections/understanding-collections-and-thread-safety-in-java*/
        /*we retrieve a collection that is thread-safe. That way we avoid ConcurrentModificationException and
          general problems like race condition*/

        List<User> usersList = Collections.synchronizedList(new ArrayList<>());
        List<Auction> auctionsList = Collections.synchronizedList(new ArrayList<>());

        System.out.println("Server has started.");
        try {
            ServerSocket serverSocket = new ServerSocket(inPort);
            // Listens forever for connections.
            while (true) {
                System.out.println("Server is listening for new connections...");
                connectionSocket = serverSocket.accept();
                System.out.println("User with IP " + connectionSocket.getInetAddress().getHostAddress() + " has now joined.");
                // Add user to the server users.
                usersList.add(User.builder()
                        .IPAddress(connectionSocket.getInetAddress().getHostAddress())
                        .build());
                new Thread(new ConnectionHandler(connectionSocket, auctionsList)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}