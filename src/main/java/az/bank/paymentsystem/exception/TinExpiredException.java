package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class TinExpiredException extends BadRequestException {
    public TinExpiredException(String message) { super(message); }
}
