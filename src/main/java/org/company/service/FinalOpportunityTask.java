package org.company.service;

import org.company.model.Auction;
import org.company.model.Bid;
import org.company.model.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FinalOpportunityTask extends TimerTask {

    private boolean isTwice;
    private List<Auction> auctionsList;
    private Auction clientNewAuction;
    private User currentUser;
    private List<User> usersList;
    private User userWhoGainedItem;
    private Bid highestBid;

    public FinalOpportunityTask(boolean isTwice, List<Auction> auctionsList, Auction clientNewAuction,
                                User currentUser, List<User> usersList, User userWhoGainedItem, Bid highestBid) {
        this.isTwice = isTwice;
        this.auctionsList = auctionsList;
        this.clientNewAuction = clientNewAuction;
        this.currentUser = currentUser;
        this.usersList = usersList;
        this.userWhoGainedItem = userWhoGainedItem;
        this.highestBid = highestBid;
    }

    @Override
    public void run() {
        if (isTwice) {
            for (User user:
                 clientNewAuction.getParticipants()) {
                user.getUserInbox().push("Last bid for the item " + clientNewAuction.getItemOnSale().getItemName()
                        + " was price " + highestBid.getBidValue() + ": going twice.");
            }
            // Then we count 5 more seconds until the last notification.
            LocalDateTime after5Seconds =
                    LocalDateTime.now().plusSeconds(5);
            new Timer().schedule(new FinalOpportunityTask(
                            false,
                            auctionsList,
                            clientNewAuction,
                            currentUser,
                            usersList,
                            userWhoGainedItem,
                            highestBid),
                    Date.from(after5Seconds.atZone(ZoneId.systemDefault()).toInstant()));
        }
        // If isTwice is false, then it means that we're at the stage where the auction finally finished.
        else {
            String messageToSend = "Item " + clientNewAuction.getItemOnSale().getItemName() + " sold for price "
                    + highestBid.getBidValue() + " to participant " + userWhoGainedItem.getUsername() + ".";
            for (User user:
                 clientNewAuction.getParticipants()) {
                user.getUserInbox().push(messageToSend);
            }
            // This is the part where we also inform the owner of the auction.
            currentUser.getUserInbox().push(messageToSend);
            // We also remove the auction (meaning that we "close it").
            auctionsList.remove(clientNewAuction);
        }
    }

}