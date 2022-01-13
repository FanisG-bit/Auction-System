package org.company.model;

import lombok.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/** Represents an Auction.
 *  @author Theofanis Gkoufas
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Auction implements Serializable {

    private int auctionID;
    private Item itemOnSale;
    /** @see AuctionClosingType
     */
    private AuctionClosingType closingType;
    private User owner;
    /** This collection stores a reference of each User that has been registered on this particular auction.
     */
    private List<User> participants;
    /** This collection contains all the bids placed for this particular auction: User -> Bid (which has two fields:
     * how much money was bid, time in which it was placed).
     */
    private HashMap<User, Bid> bidsPlaced;
    /** A timer is being set by the user in both of the closing types.
     */
    private int closingTimer;
    /** This counter belongs to the class (static) and helps us for the generation of unique auction IDs'.
     */
    public static int counter = 0;
    /** @see AuctionStatus
     */
    private AuctionStatus auctionStatus;

    /** Every time that a new auction is being created, the counter is incremented (just like the 'auto increment'
     *  fields in databases).
     */
    public static void incrementCounter() {
        counter++;
    }

    // TODO we have the secondScenarioTimers map that uses Auction as the key. We should probably override
    //  equals and hashCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Auction auction = (Auction) o;
        return auctionID == auction.auctionID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionID);
    }

}