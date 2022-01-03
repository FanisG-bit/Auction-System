package org.company.service;

import org.company.model.Auction;
import org.company.model.AuctionClosingType;
import org.company.model.Bid;
import org.company.model.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DisconnectHandler extends TimerTask {

    private AuctionClosingType closingType;
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
        User userWhoGainedItem = ClientAuctionManagementOperations.getUserWhoPlacedBid(auctionsList, clientNewAuction.getAuctionID(),
                ClientAuctionManagementOperations.getHighestBidPlacedInAuction(auctionsList,
                        clientNewAuction.getAuctionID()));
        Bid highestBid = ClientAuctionManagementOperations.getHighestBidPlacedInAuction(auctionsList,
                clientNewAuction.getAuctionID());
        if (closingType.equals(AuctionClosingType.SPECIFIED_TIME_SET)) {
            System.out.println("Auction with ID = " + clientNewAuction.getAuctionID() + " is now closed." +
                    " No more bids can be placed.");
            // we notify the owner
            currentUser.getUserInbox().push("The item '" + clientNewAuction.getItemOnSale().getItemName()
                    + "' from the auction that was created by you with ID = " + clientNewAuction.getAuctionID()
                    + " was bought at the price of " + highestBid.getBidValue()
                    + " from the User = '" + userWhoGainedItem.getUsername() + "'"
            );
            // we notify all the participants
            for (User user :
                    clientNewAuction.getParticipants()) {
                if (user.equals(userWhoGainedItem)) {
                    user.getUserInbox().push("The item '" + clientNewAuction.getItemOnSale().getItemName()
                            + "' from the auction with ID = " + clientNewAuction.getAuctionID()
                            + " was bought at the price of " + highestBid.getBidValue()
                            + " by you. Good job!");
                } else {
                    user.getUserInbox().push("The item '" + clientNewAuction.getItemOnSale().getItemName()
                            + "' from the auction with ID = " + clientNewAuction.getAuctionID()
                            + " was bought at the price of " + highestBid.getBidValue()
                            + " from the User = '" + userWhoGainedItem.getUsername() + "'");
                }
            }
            // We "close" the auction.
            auctionsList.remove(clientNewAuction);
        }
        if (closingType.equals(AuctionClosingType.BID_STARTS_TIMER)) {
            // At this point of the execution, the timer has finally finished (even after all the potential resets
            // by other bidders).
            System.out.println("timer finished!");
            // We start "spreading" the "warning" messages (e.g. "Last bid for item X was price Y: going once").
            for (User user :
                    clientNewAuction.getParticipants()) {
                // No reason to notify the owner about the "going once, two etc." Except for the last bit (when sold).
                user.getUserInbox().push("Last bid for the item " + clientNewAuction.getItemOnSale().getItemName()
                        + " was price " + highestBid.getBidValue() + ": going once.");
            }
            // Then we count 5 seconds until the next notification.
            LocalDateTime after5Seconds =
                    LocalDateTime.now().plusSeconds(5);
            new Timer().schedule(new FinalOpportunityTask(
                    true,
                    auctionsList,
                    clientNewAuction,
                    currentUser,
                    usersList,
                    userWhoGainedItem,
                    highestBid),
                    Date.from(after5Seconds.atZone(ZoneId.systemDefault()).toInstant()));
        }
    }
}