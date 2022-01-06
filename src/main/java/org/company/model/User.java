package org.company.model;

import lombok.*;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Queue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    private String username;
    @ToString.Exclude
    private InetAddress IPAddress;
    @ToString.Exclude
    private Queue<String> userInbox;

    public static int counter = 0;

    public static void incrementCounter() {
        counter++;
    }

    //3 Extremely important! More info -> https://www.youtube.com/watch?v=HlpWrH3CcoM&ab_channel=SivaReddy

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