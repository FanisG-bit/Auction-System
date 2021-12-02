package org.company.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*Inspiration for the use of ObjectOutputStream/ObjectInputStream taken from
        https://www.youtube.com/watch?v=a-5Rb1_A4ew&ab_channel=Neil%27sTutorials*/
/*In order to send objects the classes should implement Serializable. That is needed
because "Via Java Serialization you can stream your Java object to a sequence of
byte and restore these objects from this stream of bytes"
-> https://www.vogella.com/tutorials/JavaSerialization/article.html*/

public class TCPPacketInteraction {

    public static void sendPacket(Socket socket, Object object) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(object);
    }

    public static Object receivePacket(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        return inputStream.readObject();
    }

}