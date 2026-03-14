package az.bank.paymentsystem.exception;

public class HttpRequestMethodNotSupportedException extends RuntimeException {
    public HttpRequestMethodNotSupportedException(String message) {
        super(message);
    }
}
