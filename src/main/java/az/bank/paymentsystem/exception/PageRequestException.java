package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class PageRequestException extends BadRequestException {
    public PageRequestException(String message) { super(message); }
}
