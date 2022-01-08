package org.company.model;

import java.io.Serializable;

/** @author Theofanis Gkoufas
 */

public enum AuctionClosingType implements Serializable {
    /** There are two Closing Types:
     *  - SPECIFIED_TIME_SET: the auction will run for a specified amount of time set by the client. The participant,
     *  who has the highest bid when this time expires, gets the item.
     *  - BID_STARTS_TIMER: the auction runs as long as bids are placed. When a bid is placed, the server starts
     *  a timer. If the timer expires and nobody made a new bid, then the server sends a message to the participants
     *  “Last bid for item X was price Y: going once”. Then, after 5 seconds, it does the same again with a
     *  “… going twice” message. Finally, after 5 seconds it sends a message “Item X Sold for price Y to
     *  participant Z” to all registered clients of the specific auction (including the client who
     *  initiated the auction).
     */
    SPECIFIED_TIME_SET, BID_STARTS_TIMER
}