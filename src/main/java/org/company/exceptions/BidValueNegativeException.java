package org.company.exceptions;

public class BidValueNegativeException extends Exception{

    public BidValueNegativeException(String message) {
        super(message);
    }

}