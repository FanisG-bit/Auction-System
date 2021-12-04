package org.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    private String username;
    private InetAddress IPAddress;
    // all the auctions that the user has started and belong to him
    // private List<Auction> userAuctions = new ArrayList<>();

    public static int counter = 0;

    public static void incrementCounter() {
        counter++;
    }

}