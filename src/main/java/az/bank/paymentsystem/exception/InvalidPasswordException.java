package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class InvalidPasswordException extends BadRequestException {
    public InvalidPasswordException(String message) { super(message); }
}
