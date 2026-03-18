package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.TooManyRequestsException;

public class CardOrderCooldownException extends TooManyRequestsException {
    public CardOrderCooldownException(String message) {super(message);}
}
