package org.company;

import org.company.model.Participant;

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
        List<Participant> participantsList = Collections.synchronizedList(new ArrayList<>());

        System.out.println("Server is listening.");
        try {
            ServerSocket serverSocket = new ServerSocket(inPort);
//          listens forever for connections
            while (true) {
                connectionSocket = serverSocket.accept();
                System.out.println("User with ip " + connectionSocket.getInetAddress() + " has now joined.");
                new Thread(new ConnectionHandler(connectionSocket)).start();
                System.out.println("what happens");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}