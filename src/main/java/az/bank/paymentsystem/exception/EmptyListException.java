package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.NotFoundException;

public class EmptyListException extends NotFoundException {
    public EmptyListException(String message) { super(message); }
}
