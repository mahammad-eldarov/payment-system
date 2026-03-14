package az.bank.paymentsystem.exception.base;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
