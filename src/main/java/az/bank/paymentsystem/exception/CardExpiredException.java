package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class CardExpiredException extends BadRequestException {
    public CardExpiredException(String message) { super(message); }
}

