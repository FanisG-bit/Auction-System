package org.company.exceptions;

public class AuctionIsClosedException extends Exception {

    public AuctionIsClosedException(String message) {
        super(message);
    }

}