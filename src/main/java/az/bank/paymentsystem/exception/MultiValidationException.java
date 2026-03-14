package az.bank.paymentsystem.exception;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@RequiredArgsConstructor
//@NoArgsConstructor(force = true)
//@AllArgsConstructor
public class MultiValidationException extends RuntimeException {
    private final List<ExceptionResponse> errors;

//    public MultiValidationException(List<ExceptionResponse> errors) {
//        super();
//        this.errors = errors;
//    }
//
//    public List<ExceptionResponse> getErrors() {
//        return errors;
//    }
}
