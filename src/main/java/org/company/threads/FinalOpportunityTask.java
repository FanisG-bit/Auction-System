package org.company.threads;

import org.company.model.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** Used in collaboration with the CloseAuctionHandler, in order to schedule events that happen after the
 *  CloseAuctionHandler task is over. More specifically, this task only executes in the case that an auction is of
 *  closing type BID_STARTS_TIMER. That is because, when an auction timer runs out, meaning that no more bids are placed
 *  (since with each bid, the timer is reset), then the system starts sending messages to the participants in the
 *  form of: "Last bid for the item X going twice" etc. In total there are 3 messages that needs to be sent. The first
 *  one ("going once") is sent in the CloseAuctionHandler class, right after the timer finishes. From that point the
 *  remaining 2 messages are scheduled to be sent after 5 and 10 seconds respectively (actually the 2nd message is scheduled
 *  for after 5 seconds and then the 3rd message is scheduled after 5 seconds as well). That hopefully explaines the
 *  importance of this class.
 *  @author Theofanis Gkoufas
 */

public class FinalOpportunityTask extends TimerTask {

    private boolean isTwice;
    private List<Auction> auctionsList;
    private Auction clientNewAuction;
    private User currentUser;
    private List<User> usersList;
    private User userWhoGainedItem;
    private Bid highestBid;

    /**
     * @param isTwice Is used in order to determine whether the instance that is being created is scheduled to send \
     *                messages regarding the 2nd message ("going twice") or the last one ("item sold").
     * @param auctionsList The list that stores all the auctions that are currently on the system.
     * @param clientNewAuction A reference to the object that represents a newly created auction. More info written in:
     *                         {@link CloseAuctionHandler#CloseAuctionHandler(AuctionClosingType, List, Auction, User, List)}
     * @param currentUser A reference to the user object that is communicating with the server.
     * @param usersList The list that contains all the connected users currently on the system.
     * @param userWhoGainedItem The client who had the highest bid and managed to acquire the item.
     * @param highestBid The bid value blaced by the client who acquired the item.
     */
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
                user.getUserInbox().add("Last bid for the item " + clientNewAuction.getItemOnSale().getItemName()
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
            System.out.println("Auction with ID = " + clientNewAuction.getAuctionID() + " is now closed." +
                    " No more bids can be placed.");
            String messageToSend;
            if (highestBid.getBidValue() != -1) {
                messageToSend = "Item " + clientNewAuction.getItemOnSale().getItemName() + " sold for price "
                        + highestBid.getBidValue() + " to participant " + userWhoGainedItem.getUsername() + ".";
            }
            else {
                /** If the highestBid value is -1, then it means that no bids where placed for this auction.
                 *  It is explained in: {@link org.company.service.AuctionRelatedFinder#getHighestBidPlacedInAuction(List, int)}
                 */
                messageToSend = "Item " + clientNewAuction.getItemOnSale().getItemName() + " had no bids placed. " +
                        "No one acquired the item.";
            }
            for (User user:
                 clientNewAuction.getParticipants()) {
                user.getUserInbox().add(messageToSend);
            }
            // This is the part where we also inform the owner of the auction.
            clientNewAuction.getOwner().getUserInbox().add("The auction created by you for " + messageToSend);
            // We also "close" the auction (meaning that we change the status).
            int auctionId = clientNewAuction.getAuctionID() - 1;
            auctionsList.get(auctionId).setAuctionStatus(AuctionStatus.CLOSED);
        }
    }

}