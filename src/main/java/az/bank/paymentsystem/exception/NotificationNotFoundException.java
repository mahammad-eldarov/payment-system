package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.NotFoundException;

public class NotificationNotFoundException extends NotFoundException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
