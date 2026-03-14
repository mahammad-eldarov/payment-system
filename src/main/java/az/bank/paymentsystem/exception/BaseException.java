package az.bank.paymentsystem.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@RequiredArgsConstructor
public class BaseException extends RuntimeException{

    private final String errorMessage;
    private final String details;
    private final HttpStatus status;

    public BaseException(String message, String errorMessage, String details, HttpStatus status) {
        super(message);
        this.errorMessage = errorMessage;
        this.details = details;
        this.status = status;
    }

}




