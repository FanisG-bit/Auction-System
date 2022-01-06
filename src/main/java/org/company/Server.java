package org.company;

import org.company.model.Auction;
import org.company.model.User;
import org.company.service.DisconnectHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    public static void main(String[] args) {

        int inPort = 6666;
        Socket connectionSocket = null;

        /*read page in: https://www.codejava.net/java-core/collections/understanding-collections-and-thread-safety-in-java*/
        /*we retrieve a collection that is thread-safe. That way we avoid ConcurrentModificationException and
          general problems like race condition*/

        List<User> usersList = Collections.synchronizedList(new ArrayList<>());
        List<Auction> auctionsList = Collections.synchronizedList(new ArrayList<>());
        Map<Auction, Timer> secondScenarioTimers = Collections.synchronizedMap(new HashMap<>());

        System.out.println("""            
                                
                \t\t\tServerSideMenu
                --------------------------------------
                
                """);
        System.out.println("Server has started.");
        try {
            ServerSocket serverSocket = new ServerSocket(inPort);
            // Listens forever for connections.
            while (true) {
                System.out.println("Server is listening for new connections...");
                connectionSocket = serverSocket.accept();
                // Add user to the server users.
                // Queue is an interface so, we cannot create one without implementing all the methods. That's why
                // we choose one of the classes that implements it (LinkedList).
                User.incrementCounter();
                usersList.add(User.builder()
                        .username("User" + User.counter)
                        .IPAddress(connectionSocket.getInetAddress())
                        .userInbox(new LinkedList<>())
                        .build());
                System.out.println("User with username User" + User.counter + " has now joined.");
                new Thread(new ConnectionHandler(connectionSocket, auctionsList, usersList, "User" + User.counter, secondScenarioTimers)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}