package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class TinAlreadyCancelledException extends BadRequestException {
    public TinAlreadyCancelledException(String message) { super(message); }
}