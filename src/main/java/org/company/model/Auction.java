package org.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Auction implements Serializable {

    private int auctionID;
    private Item itemOnSale;
    private AuctionClosingType closingType;
    private User owner;
    private List<User> participants;
    // all the bids placed for this particular auction: user -> bid (number/how much money, time placed).
    private HashMap<User, Bid> bidsPlaced;
    // a timer is being set by the user in both of the closing types.
    private int closingTimer;

    // this counter belongs to the class and helps us for the generation of unique auction IDs'.
    public static int counter = 0;

    private AuctionStatus auctionStatus;

    // every time that a new auction is being created, the counter is incremented (just like
    // the 'auto increment' fields in databases).
    public static void incrementCounter() {
        counter++;
    }

}