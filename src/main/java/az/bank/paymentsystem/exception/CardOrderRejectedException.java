package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.UnprocessableContentException;

public class CardOrderRejectedException extends UnprocessableContentException {
    public CardOrderRejectedException(String message) {
        super(message);
    }
}
