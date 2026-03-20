package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.NotFoundException;

public class ExternalPartyNotFoundException extends NotFoundException {
    public ExternalPartyNotFoundException(String message) {
        super(message);
    }
}
