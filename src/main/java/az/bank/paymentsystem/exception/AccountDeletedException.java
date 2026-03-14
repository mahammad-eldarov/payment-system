package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.GoneException;

public class AccountDeletedException extends GoneException {
    public AccountDeletedException(String message) { super(message); }
}