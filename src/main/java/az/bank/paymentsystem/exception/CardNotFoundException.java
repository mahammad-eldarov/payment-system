package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.NotFoundException;

public class CardNotFoundException extends NotFoundException {
    public CardNotFoundException(String message) { super(message); }
}


