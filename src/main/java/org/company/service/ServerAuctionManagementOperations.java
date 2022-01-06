package org.company.service;

import org.company.model.*;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

public class ServerAuctionManagementOperations {

    public static User retrieveUserBasedOnName(List<User> usersList, String currentUsername) {
        User currentUser = null;
        for (User user : usersList) {
            if (user.getUsername().equals(currentUsername)) {
                currentUser = user;
                break;
            }
        }
        return currentUser;
    }

    public static User retrieveUser(List<User> usersList, Socket client) {
        User currentUser = null;
        for (User user : usersList) {
            if (user.getIPAddress().getHostAddress().equals(client.getInetAddress().getHostAddress())) {
                currentUser = user;
                break;
            }
        }
        return currentUser;
    }

    // this method handles the user command from the server perspective
    public static void handleCommand(String clientCommand, Socket client, User currentUser, List<User> usersList, List<Auction> auctionsList, Map<Auction, Timer> secondScenarioTimers) {
        switch (clientCommand) {
            case "?placeItemForAuction":
                while (true) {
                    try {
                        boolean proceedWithOperation = (boolean) TCPPacketInteraction.receivePacket(client);
                        if (proceedWithOperation) {
                            Auction clientNewAuction = (Auction) TCPPacketInteraction.receivePacket(client);
                            Auction.incrementCounter();
                            clientNewAuction.setAuctionID(Auction.counter);
                            clientNewAuction.setOwner(currentUser);
                            clientNewAuction.setParticipants(new ArrayList<>());
                            clientNewAuction.setBidsPlaced(new HashMap<>());
                            clientNewAuction.setAuctionStatus(AuctionStatus.OPEN);
                            auctionsList.add(clientNewAuction);
                            if (clientNewAuction.getClosingType().equals(AuctionClosingType.SPECIFIED_TIME_SET)) {
                                // The lines below refer to the first way of closing an auction where we set a timer and after
                                // that timer finishes, the highest bidder gets the item.
                                LocalDateTime afterHowManySecondsItCloses =
                                        LocalDateTime.now().plusSeconds(clientNewAuction.getClosingTimer());
                                // We convert the LocalDateTime to a Date object since that is the only acceptable form of
                                // the second parameter. The steps are:
                                // - convert LocalDateTime to ZonedDateTime,
                                // - pass the system's default time-zone as the argument of the atZone method,
                                // - convert ZonedDateTime to an Instant object,
                                // - obtain an instance of Date from the Instant object by using the Date.from
                                // Help was acquired from -> https://www.baeldung.com/java-date-to-localdate-and-localdatetime
                                new Timer().schedule(new DisconnectHandler(AuctionClosingType.SPECIFIED_TIME_SET, auctionsList,
                                                clientNewAuction, currentUser, usersList),
                                        Date.from(afterHowManySecondsItCloses.atZone(ZoneId.systemDefault()).toInstant()));
                                break;
                            } else {
                                // The lines below refer to the second way where the auction will keep going as long
                                // as bids are being placed. If the timer ends and no valid bids are placed, then a
                                // message is being sent to the participants like “Last bid for item X was price Y:
                                // going once” etc.
                                LocalDateTime afterHowManySecondsItCloses =
                                        LocalDateTime.now().plusSeconds(clientNewAuction.getClosingTimer());
                                Timer timer = new Timer();
                                timer.schedule(new DisconnectHandler(AuctionClosingType.BID_STARTS_TIMER,
                                                auctionsList, clientNewAuction, currentUser, usersList),
                                        Date.from(afterHowManySecondsItCloses.atZone(ZoneId.systemDefault()).toInstant()));
                                secondScenarioTimers.put(clientNewAuction, timer);
                                break;
                            }
                        } else {
                            // user abandons operation, so the loop breaks without something happening
                            break;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            break;
            case "?listActiveAuctions":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "?participateInAuction":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                    TCPPacketInteraction.sendPacket(client, currentUser);
                    int auctionID = (int) TCPPacketInteraction.receivePacket(client);
                    if (auctionID == -1) {
                        // operation has been abandoned by the user.
                        // we break from the switch.
                        break;
                    } else {
                        auctionID--; // that is because list index starts from 0 but auction IDs start from 1 and increment.
                        auctionsList.get(auctionID).getParticipants().add(currentUser);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            break;
            case "?placeABid":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                    TCPPacketInteraction.sendPacket(client, currentUser);
                    int auctionID = (int) TCPPacketInteraction.receivePacket(client);
                    if (auctionID == -1) {
                        // operation has been abandoned by the user on the state of CHOOSING AN AUCTION.
                        break;
                    } else {
                        double clientOffer = (double) TCPPacketInteraction.receivePacket(client);
                        if (clientOffer == -1) {
                            // operation has been abandoned by the user on the state of PLACING AN OFFER.
                            break;
                        } else {
                            auctionID--;

                            auctionsList.get(auctionID)
                                    .getBidsPlaced()
                                    .put(currentUser, new Bid(clientOffer, LocalTime.now()));

                            // Notify all the participants
                            int auctionIdPriorToDecrease = auctionID + 1;
                            for (User participant:
                                 auctionsList.get(auctionID).getParticipants()) {
                                if (!participant.equals(currentUser)) {
                                    participant.getUserInbox().add("Participant: " + currentUser.getUsername() + " made a bid of "
                                             + clientOffer + " for the item " +
                                            auctionsList.get(auctionID).getItemOnSale().getItemName() + " of the auction " +
                                            "with ID=" + auctionIdPriorToDecrease + ".");
                                }
                            }

                            if (auctionsList.get(auctionID).getClosingType()
                                    .equals(AuctionClosingType.BID_STARTS_TIMER)) {
                                // In past development of this part, we were only retrieving the Map as a Set using
                                // the entrySet() inside an enhanced for loop. Then I came to the realization that
                                // when trying to delete an element from the "secondScenarioTimers" Map, there was no way
                                // as explained in the documentation of the entrySet() method. In order to achieve that
                                // an Iterator is needed. So the lines below are explained as: if the iteration has
                                // more elements, then retrieve the Map as a Set and return an iterator over the
                                // elements of that Set.
                                for (Iterator<Map.Entry<Auction, Timer>> it = secondScenarioTimers.entrySet().iterator();
                                     it.hasNext();) {
                                    // We get each element.
                                    Map.Entry<Auction, Timer> timer = it.next();
                                    if (timer.getKey().equals(auctionsList.get(auctionID))) {
                                        // We cancel the scheduled event (was firstly created when owner opens
                                        // the auction).
                                        timer.getValue().cancel();
                                        // We delete (remove) the old association from the Map in order to put the
                                        // new one, which will include the new Timer. This is done because now that the timer
                                        // is canceled, we cannot re-schedule it again. More specifically this error was
                                        // displayed when trying to use the same timer -> "Timer already cancelled".
                                        // https://stackoverflow.com/questions/20242540/how-to-reschedule-a-task-using-timer
                                        // We then reschedule the timer (meaning that we schedule the same event, timer
                                        // resets to zero/starts again).
                                        it.remove();
                                        // Get the time that was set by the owner.
                                        LocalDateTime afterHowManySecondsItCloses =
                                                LocalDateTime.now().plusSeconds(auctionsList.get(auctionID).getClosingTimer());
                                        Timer newTimer = new Timer();
                                        newTimer.schedule(
                                                new DisconnectHandler(AuctionClosingType.BID_STARTS_TIMER,
                                                        auctionsList, auctionsList.get(auctionID), currentUser, usersList),
                                                Date.from(afterHowManySecondsItCloses.atZone(ZoneId.systemDefault()).toInstant()));
                                        secondScenarioTimers.put(auctionsList.get(auctionID), newTimer);
                                        break;
                                    }
                                }
                            }

                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            break;
            case "?checkHighestBid":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                    int auctionID = (int) TCPPacketInteraction.receivePacket(client);
                    // Operation has been abandoned by the user. Not sure if the break is needed
                    // at this point. Most probably not. There is also nothing that the server
                    // can do with the response from the client.
                    // We have to retrieve a response just because in the client, we use the
                    // "handleAuctionIdInput" method which sends -1 to the server, if the operation is
                    // abandoned by the user while choosing for an auction.
                    if (auctionID == -1) {
                        break;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            break;
            case "?withdrawFromAuction":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                    TCPPacketInteraction.sendPacket(client, currentUser);
                    int auctionID = (int) TCPPacketInteraction.receivePacket(client);
                    if (auctionID != -1) {
                        // We remove (withdraw) the user from the specified auction.
                        auctionID--;
                        auctionsList.get(auctionID).getParticipants().remove(currentUser);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            break;
            case "?disconnect":
                try {
                    TCPPacketInteraction.sendPacket(client, auctionsList);
                    TCPPacketInteraction.sendPacket(client, currentUser);
                    boolean shouldDisconnect = (boolean) TCPPacketInteraction.receivePacket(client);
                    if (shouldDisconnect) {
                        // We remove the user from any auctions in which he/she is a participant.
                        for (Auction auction:
                                auctionsList) {
                            if (auction.getParticipants().contains(currentUser)) {
                                auction.getParticipants().remove(currentUser);
                            }
                        }
                        // We also remove that person from the list that contains all the users currently
                        // on the system.
                        usersList.remove(currentUser);
                        TCPPacketInteraction.sendPacket(client, "Goodbye!");
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            break;
            case "?readInbox":
                boolean shouldContinue = true;
                do {
                    try {
                        shouldContinue = (boolean) TCPPacketInteraction.receivePacket(client);
                        if (shouldContinue) {
                            TCPPacketInteraction.sendPacket(client, currentUser);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }while (shouldContinue);
            break;
        }
    }

    public static boolean isClientCommandValid(String command, String[] commands) {
        boolean isValid = true;
        if (!command.startsWith("?")) {
            isValid = false;
        }
        if (!Arrays.asList(commands).contains(command.substring(1))) {
            isValid = false;
        }
        return isValid;
    }

}