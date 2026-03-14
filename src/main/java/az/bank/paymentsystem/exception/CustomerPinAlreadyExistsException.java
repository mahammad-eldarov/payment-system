package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.ConflictException;

public class CustomerPinAlreadyExistsException extends ConflictException {
    public CustomerPinAlreadyExistsException(String message) { super(message); }
}
