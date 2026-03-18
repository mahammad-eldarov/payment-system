package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.ForbiddenException;

public class OperationNotAllowedException extends ForbiddenException {
    public OperationNotAllowedException(String message) {
        super(message);
    }
}
