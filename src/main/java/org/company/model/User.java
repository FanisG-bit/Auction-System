package org.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Stack;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    private String username;
    private InetAddress IPAddress;
    private Stack<String> userInbox;

    public static int counter = 0;

    public static void incrementCounter() {
        counter++;
    }

}