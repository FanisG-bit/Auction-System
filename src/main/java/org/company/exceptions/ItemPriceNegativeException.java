package org.company.exceptions;

public class ItemPriceNegativeException extends Exception {

    public ItemPriceNegativeException(String message) {
        super(message);
    }

}