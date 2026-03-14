package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.GoneException;

public class CardDeletedException extends GoneException {
    public CardDeletedException(String message) { super(message); }
}