package org.company.service;

import org.company.model.Auction;
import org.company.model.AuctionStatus;
import org.company.model.Bid;
import org.company.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Useful methods regarding finding information about an auction and similar.
 *  @author Theofanis Gkoufas
 */

public class AuctionRelatedFinder {

    /** Retrieves a list that includes the auctions in which, a user is the highest bidder. Used for
     *  the process of disconnecting a client from the system.
     *  @param auctionList The list that stores all the auctions that are currently on the system.
     *  @param thisUser The user that we want to check whether he is the highest bidder in any active (open) auctions.
     *  @return A list that has all the auctions in which a user has the highest bid. Could return an empty list if
     *  the user is not the highest bidder in any auction.
     */
    public static List<Auction> checkIfHighestBidder(List<Auction> auctionList, User thisUser) {
        List<Auction> auctionsInWhichUserHighestBidder = new ArrayList<>();
        for (Auction auction:
                auctionList) {
            if (auction.getParticipants().contains(thisUser)) {
                if (getUserWhoPlacedBid(auctionList,
                        auction.getAuctionID(),
                        getHighestBidPlacedInAuction(auctionList, auction.getAuctionID()))
                        .equals(thisUser) && auction.getAuctionStatus().equals(AuctionStatus.OPEN))
                {
                    auctionsInWhichUserHighestBidder.add(auction);
                }
            }
        }
        return auctionsInWhichUserHighestBidder;
    }

    /** Given an auction ID it searches within the given auction list and finds the starting price that was
     *  set by the owner of the auction for the respective item.
     * @param auctionList The list that stores all the auctions that are currently on the system.
     * @param auctionID The unique ID that matches to an auction within the system.
     * @return The starting price set by the owner of that particular auction.
     * @throws NullPointerException Admittedly, the method could be written (for example )in a way that would return -1.0
     * if the auction id does not match to any auction within the system. That would be a safer "approach". The
     * reason why it wasn't changed is that this method is being called after the passed auction id is validated as
     * a correct one (we interpret correct in this context as an id that matches to an auction). In conclusion, there
     * is no way that the program will produce this kind of exception.
     */
    @SuppressWarnings("ConstantConditions")
    public static double getAuctionItemStartingPrice(List<Auction> auctionList, int auctionID) throws NullPointerException {
        Auction thisAuction = null;
        for (Auction auction:
                auctionList) {
            if (auction.getAuctionID() == auctionID) {
                thisAuction = auction;
            }
        }
        return thisAuction.getItemOnSale().getItemStartingPrice();
    }

    /** Given an auction ID it searches for the highest bid that was placed in the respective auction (having that ID).
     * @param auctionList The list that stores all the auctions that are currently on the system.
     * @param auctionID The ID of the auction that we want to find the highest bid placed.
     * @return The highest bid that was placed on the auction having the given ID. If there are no bids placed it
     * returns a Bid object having -1 as its bid value.
     */
    public static Bid getHighestBidPlacedInAuction(List<Auction> auctionList, int auctionID) {
        HashMap<User, Bid> bidsPlaced = null;
        Bid maxBid = Bid.builder()
                .bidValue(-1.0)
                .build();
        for (Auction auction:
                auctionList) {
            if (auction.getAuctionID() == auctionID) {
                bidsPlaced = auction.getBidsPlaced();
            }
        }
        if (bidsPlaced != null) {
            if (bidsPlaced.isEmpty()) {
                return maxBid;
            } else {
                for (Bid bid:
                        bidsPlaced.values()) {
                    if (maxBid.getBidValue() < bid.getBidValue()) {
                        maxBid = bid;
                    }
                }
            }
        }
        return maxBid;
    }

    /** Given an auction ID and a Bid, it retrieves the user who placed that bid in the auction that has that ID.
     * @param auctionList The list that stores all the auctions that are currently on the system.
     * @param auctionID The id of the auction in which we want to find the user who placed a specific bid.
     * @param bid The bid that is placed by a user.
     * @return The user who placed the given bid.
     * @throws NullPointerException The "bidsPlaced" local variable could produce such an exception in line 118. In
     * this program though, we are already checking that the passed auction ID is valid thus it is not possible
     * to happen.
     * @see <a href="https://www.baeldung.com/java-map-key-from-value">map-key-from-value-baeldung</a>
     * -> Inspiration for the way of retrieving a key based on a value.
     */
    @SuppressWarnings("ConstantConditions")
    public static User getUserWhoPlacedBid(List<Auction> auctionList, int auctionID, Bid bid)
            throws NullPointerException {
        HashMap<User, Bid> bidsPlaced = null;
        for (Auction auction:
                auctionList) {
            if (auction.getAuctionID() == auctionID) {
                bidsPlaced = auction.getBidsPlaced();
            }
        }
        User userWhoPlacedThatBid = null;
        for (Map.Entry<User, Bid> entry:
                bidsPlaced.entrySet()) {
            if (entry.getValue().equals(bid)) {
                userWhoPlacedThatBid = entry.getKey();
            }
        }
        return userWhoPlacedThatBid;
    }

}