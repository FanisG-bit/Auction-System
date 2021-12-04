package org.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Auction implements Serializable {

    private int auctionID;
    private Item itemOnSale;
    private AuctionClosingType closingType;
    private User owner;

    // this counter belongs to the class and helps us for the generation of unique auction IDs'.
    public static int counter = 0;

    // every time that a new auction is being created, the counter is incremented (just like
    // the 'auto increment' fields in databases).
    public static void incrementCounter() {
        counter++;
    }

}