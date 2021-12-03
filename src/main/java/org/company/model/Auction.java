package org.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction implements Serializable {

    private int auctionID = counter;
    private Item itemOnSale;
    private AuctionClosingType closingType;

    // this counter belongs to the class and helps us for the generation of unique auction IDs'.
    private static int counter;

    // every time that a new auction is being created, the counter is incremented (just like
    // the 'auto increment' fields in databases).
    private static void incrementCounter() {
        counter++;
    }

}