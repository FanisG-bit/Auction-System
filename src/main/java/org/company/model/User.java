package org.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Queue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    private String username;
    private InetAddress IPAddress;
    private Queue<String> userInbox;

    public static int counter = 0;

    public static void incrementCounter() {
        counter++;
    }

}