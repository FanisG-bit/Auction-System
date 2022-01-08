package org.company.threads;

import org.company.model.*;
import org.company.service.AuctionRelatedFinder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** Class responsible for handling the closing of an auction. An instance of this class is used as a task
 *  that is scheduled by a Timer, in order to be executed at a specific (and given) date/time. There are two ways of
 *  closing an auction, both of which, require different steps to be taken. See: {@link AuctionClosingType}
 *  @author Theofanis Gkoufas
 */

@SuppressWarnings("FieldMayBeFinal")
public class CloseAuctionHandler extends TimerTask {

    private AuctionClosingType closingType;
    private List<Auction> auctionsList;
    private Auction clientNewAuction;
    private User currentUser;
    private List<User> usersList;

    /** All-arguments constructor required for the creation of an instance of this class.
     * @param closingType Indicates whether the task that is scheduled is of closing type SPECIFIED_TIME_SET or
     *                    BID_STARTS_TIMER.
     * @param auctionsList The list that stores all the auctions that are currently on the system.
     * @param clientNewAuction A reference to the object that represents a newly created auction. It is needed for various
     *                         reasons, one being the need to alter the object when the auction finishes (change status
     *                         from OPEN to CLOSE). Also, because appropriate messages are required to be sent to the
     *                         participants atc.
     * @param currentUser A reference to the user object that is communicating with the server. More information:
     *                    {@link org.company.service.ServerCommandHandler#retrieveUserBasedOnName(List, String)}
     * @param usersList The list that contains all the connected users currently on the system.
     */
    public CloseAuctionHandler(AuctionClosingType closingType, List<Auction> auctionsList, Auction clientNewAuction,
                               User currentUser, List<User> usersList) {
        this.closingType = closingType;
        this.auctionsList = auctionsList;
        this.clientNewAuction = clientNewAuction;
        this.currentUser = currentUser;
        this.usersList = usersList;
    }

    @Override
    public void run() {
        User userWhoGainedItem = AuctionRelatedFinder.getUserWhoPlacedBid(auctionsList, clientNewAuction.getAuctionID(),
                AuctionRelatedFinder.getHighestBidPlacedInAuction(auctionsList,
                        clientNewAuction.getAuctionID()));
        Bid highestBid = AuctionRelatedFinder.getHighestBidPlacedInAuction(auctionsList,
                clientNewAuction.getAuctionID());
        if (closingType.equals(AuctionClosingType.SPECIFIED_TIME_SET)) {
            System.out.println("Auction with ID = " + clientNewAuction.getAuctionID() + " is now closed." +
                    " No more bids can be placed.");
            if (highestBid.getBidValue() != -1) {
                // We notify the owner.
                clientNewAuction.getOwner().getUserInbox().add("The item '" + clientNewAuction.getItemOnSale().getItemName()
                        + "' from the auction that was created by you with ID = " + clientNewAuction.getAuctionID()
                        + " was bought at the price of " + highestBid.getBidValue()
                        + " by the User = '" + userWhoGainedItem.getUsername() + "'."
                );
                // We notify all the participants.
                for (User user :
                        clientNewAuction.getParticipants()) {
                    if (user.equals(userWhoGainedItem)) {
                        user.getUserInbox().add("The item '" + clientNewAuction.getItemOnSale().getItemName()
                                + "' from the auction with ID = " + clientNewAuction.getAuctionID()
                                + " was bought at the price of " + highestBid.getBidValue()
                                + " by you. Good job!");
                    } else {
                        user.getUserInbox().add("The item '" + clientNewAuction.getItemOnSale().getItemName()
                                + "' from the auction with ID = " + clientNewAuction.getAuctionID()
                                + " was bought at the price of " + highestBid.getBidValue()
                                + " by the User = '" + userWhoGainedItem.getUsername() + "'.");
                    }
                }
            } else {
                // In this case, it means that no bids where placed for this auction. So different messages are sent.
                clientNewAuction.getOwner().getUserInbox().add("The item '" + clientNewAuction.getItemOnSale().getItemName()
                        + "' from the auction that was created by you with ID = " + clientNewAuction.getAuctionID()
                        + " had no bids placed. No one acquired the item."
                );
                for (User user :
                        clientNewAuction.getParticipants()) {
                    user.getUserInbox().add("The item '" + clientNewAuction.getItemOnSale().getItemName()
                            + "' from the auction with ID = " + clientNewAuction.getAuctionID()
                            + " was not acquired by anyone. No bids were placed.");
                }
            }
            // We "close" the auction. The auctionID is incremented by one compared to the list (in which auctions are
            // stored and the starting index is 0).
            int auctionId = clientNewAuction.getAuctionID() - 1;
            auctionsList.get(auctionId).setAuctionStatus(AuctionStatus.CLOSED);
        }
        if (closingType.equals(AuctionClosingType.BID_STARTS_TIMER)) {
            /*
                At this point of the execution, the timer has finally finished (even after all the potential resets
                by other bidders).
                We start "spreading" the "warning" messages (e.g. "Last bid for item X was price Y: going once").
            */
            for (User user :
                    clientNewAuction.getParticipants()) {
                // No reason to notify the owner about the "going once, two etc." Except for the last bit (when sold).
                user.getUserInbox().add("Last bid for the item " + clientNewAuction.getItemOnSale().getItemName()
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