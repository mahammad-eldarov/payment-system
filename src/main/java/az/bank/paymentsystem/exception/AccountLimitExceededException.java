package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class AccountLimitExceededException extends BadRequestException {
    public AccountLimitExceededException(String message) { super(message); }
}
