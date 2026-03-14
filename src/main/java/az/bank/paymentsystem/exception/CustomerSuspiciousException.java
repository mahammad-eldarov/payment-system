package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.ForbiddenException;

public class CustomerSuspiciousException extends ForbiddenException {
    public CustomerSuspiciousException(String message) { super(message); }
}
