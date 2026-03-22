package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class CardAlreadyCancelledException extends BadRequestException {
    public CardAlreadyCancelledException(String message) { super(message); }
}
