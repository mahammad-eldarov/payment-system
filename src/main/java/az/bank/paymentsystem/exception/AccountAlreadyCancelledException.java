package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class AccountAlreadyCancelledException extends BadRequestException {
    public AccountAlreadyCancelledException(String message) { super(message); }
}