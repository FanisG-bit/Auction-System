package org.company.model;

import lombok.*;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Queue;

/** Represents a User.
 *  @author Theofanis Gkoufas
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    /** Username is used as the main mechanism of differentiating the clients within the system.
     *  It is automatically assigned to the user upon successful connection to the auction system.
     */
    private String username;
    /** User's IP address be used originally for the differentiation of the users but was changed due
     *  to lack of hardware resources (meaning that this program was developed in one computer and there
     *  was no way of testing it). It is still stored as an attribute but not used throughout the execution.
     */
    private InetAddress IPAddress;
    /** The user inbox which is represented as a queue, is used in order to store every notification/message
     *  that is sent to the user. A message can for example be related to some other participant bidding
     *  in an auction in which this user has been registered. Another example would be when an auction finishes
     *  and the result of who acquired the item needs to be shared among all the participants (as well as the owner).
     *  The cause for using a queue is due to the FIFO ideology (First In First Out); it makes sense for the
     *  message that was received first, to be the first to be displayed.
     */
    private Queue<String> userInbox;
    /** Similar to the Auction class, we need a counter in order to generate user ids every time that a user
     *  is being added to the system.
     */
    public static int counter = 0;

    public static void incrementCounter() {
        counter++;
    }

    /*  When we are using a mutable class we should override the "equals" and "hashCode" methods in order to
        perform as desired. It is of significant importance since it was causing a bug which was related to
        what bids where placed etc.
        Sources that were read/watched:
        https://www.youtube.com/watch?v=HlpWrH3CcoM&ab_channel=SivaReddy
        https://www.thetechnojournals.com/2019/10/why-hashmap-key-should-be-immutable-in.html
    */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

}