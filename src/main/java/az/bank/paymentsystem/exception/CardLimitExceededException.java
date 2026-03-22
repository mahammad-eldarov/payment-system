package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class CardLimitExceededException extends BadRequestException {
    public CardLimitExceededException(String message) { super(message); }
}

