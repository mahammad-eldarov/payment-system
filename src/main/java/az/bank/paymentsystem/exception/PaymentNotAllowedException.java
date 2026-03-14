package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class PaymentNotAllowedException extends BadRequestException {
    public PaymentNotAllowedException(String message) { super(message); }
}

