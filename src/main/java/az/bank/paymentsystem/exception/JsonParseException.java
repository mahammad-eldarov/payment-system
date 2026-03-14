package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class JsonParseException extends BadRequestException {
    public JsonParseException(String message) { super(message); }
}
