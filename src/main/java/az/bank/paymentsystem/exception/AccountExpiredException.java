package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class AccountExpiredException extends BadRequestException {
    public AccountExpiredException(String message) { super(message); }
}
