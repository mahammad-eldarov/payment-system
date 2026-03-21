package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.ForbiddenException;

public class FraudDetectedException extends ForbiddenException {
    public FraudDetectedException (String message) {
        super(message);
    }
}
