package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.GoneException;

public class CustomerDeletedException extends GoneException {
    public CustomerDeletedException(String message) { super(message); }
}