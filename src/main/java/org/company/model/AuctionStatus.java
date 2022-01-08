package org.company.model;

import java.io.Serializable;

/** @author Theofanis Gkoufas
 */

public enum AuctionStatus implements Serializable {
    /** Each Auction has two options when it comes to status; either be open (meaning that
     * it can accept participants, bids etc.) or close (does not accept participants, bids etc.
     * and it does not appear on user commands like "list active auctions").
     */
    OPEN, CLOSED
}