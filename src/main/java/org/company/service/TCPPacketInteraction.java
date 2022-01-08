package org.company.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Methods responsible for the tcp transfer of objects from point to point (client-server and vise versa).
 *  @see <a href="https://www.youtube.com/watch?v=a-5Rb1_A4ew&ab_channel=Neil%27sTutorials">
 *       Sending Serialized Objects/Classes Networking</a> -> Inspiration for the use of ObjectOutputStream/ObjectInputStream
 *  @see <a href="https://www.vogella.com/tutorials/JavaSerialization/article.html">
 *       Java Serialization</a> -> In order to send objects the classes should implement Serializable. That is needed
 *       because "Via Java Serialization you can stream your Java object to a sequence of
 *       byte and restore these objects from this stream of bytes".
 *  @author Theofanis Gkoufas
 */

public class TCPPacketInteraction {

    /** Given a socket (that essentially has information about the IP address and the port number of the other end in
     *  which we want to send something) as well as an object (which is basically the packet that we want to transfer
     *  could be a String message for example), an ObjectOutputStream is being created that reconstitutes the given
     *  object on the other host (server or client in this case).
     * @param socket Has information about the IP address and the port number of the other end.
     * @param object The packet that we want to transfer/send.
     * @throws IOException General class of exceptions produced by failed or interrupted I/O operations.
     */
    public static void sendPacket(Socket socket, Object object) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(object);
    }

    /** Given a socket (that essentially has information about the IP address and the port number of the other end from
     *  which we want to receive something), an ObjectInputStream is being created that reads an object from the stream.
     * @param socket Has information about the IP address and the port number of the other end.
     * @return The object that was read from the ObjectInputStream.
     * @throws IOException General class of exceptions produced by failed or interrupted I/O operations.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     */
    public static Object receivePacket(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        return inputStream.readObject();
    }

}