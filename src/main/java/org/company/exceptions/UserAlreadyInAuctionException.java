package org.company.exceptions;

public class UserAlreadyInAuctionException extends Exception {

    public UserAlreadyInAuctionException(String message) {
        super(message);
    }

}