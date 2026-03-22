package az.bank.paymentsystem.exception;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@RequiredArgsConstructor
public class MultiValidationException extends RuntimeException {
    private final List<ExceptionResponse> errors;

}
