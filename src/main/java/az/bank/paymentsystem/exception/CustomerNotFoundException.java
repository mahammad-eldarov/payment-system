package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.NotFoundException;

public class CustomerNotFoundException extends NotFoundException {
    public CustomerNotFoundException(String message) { super(message); }
}
