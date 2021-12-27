package org.company.service;

import org.company.model.Auction;
import org.company.model.AuctionClosingType;
import org.company.model.User;

import java.util.List;
import java.util.TimerTask;

public class DisconnectHandler extends TimerTask {

    private final AuctionClosingType closingType;
    private List<Auction> auctionsList;
    private Auction clientNewAuction;
    private User currentUser;
    private List<User> usersList;

    public DisconnectHandler(AuctionClosingType closingType, List<Auction> auctionsList, Auction clientNewAuction,
                             User currentUser, List<User> usersList) {
        this.closingType = closingType;
        this.auctionsList = auctionsList;
        this.clientNewAuction = clientNewAuction;
        this.currentUser = currentUser;
        this.usersList = usersList;
    }

    @Override
    public void run() {
        if (closingType.equals(AuctionClosingType.SPECIFIED_TIME_SET)) {
            System.out.println("Auction with ID = " + clientNewAuction.getAuctionID() + " is now closed.");
            // We "close" the auction.
            auctionsList.remove(clientNewAuction);
            // we notify the owner for starters
            currentUser.getUserInbox().push("Your item from auction with ID=" + clientNewAuction.getAuctionID()
                    + " was bought at the price of ");
        }
    }

}