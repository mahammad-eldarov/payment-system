package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.NotFoundException;

public class TinNotFoundException extends NotFoundException {
    public TinNotFoundException(String message) { super(message); }
}



