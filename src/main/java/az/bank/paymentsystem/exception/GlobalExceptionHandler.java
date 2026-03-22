package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.ForbiddenException;
import az.bank.paymentsystem.exception.base.TooManyRequestsException;
import az.bank.paymentsystem.exception.base.UnprocessableContentException;
import jakarta.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import az.bank.paymentsystem.exception.base.BadRequestException;
import az.bank.paymentsystem.exception.base.ConflictException;
import az.bank.paymentsystem.exception.base.GoneException;
import az.bank.paymentsystem.exception.base.NotFoundException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(400, ex.getMessage(), LocalDateTime.now()));
    }
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequest(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ExceptionResponse(403, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(404, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        String supported = ex.getSupportedMethods() != null
                ? String.join(", ", ex.getSupportedMethods())
                : "unknown";
        String message = ex.getMessage() + ". Supported methods: " + supported;
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ExceptionResponse(405, message, LocalDateTime.now()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ExceptionResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ExceptionResponse(409, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(GoneException.class)
    public ResponseEntity<ExceptionResponse> handleGone(GoneException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ExceptionResponse(410, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(UnprocessableContentException.class)
    public ResponseEntity<ExceptionResponse> handleUnprocessableEntity(UnprocessableContentException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(new ExceptionResponse(422, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ExceptionResponse> handleTooManyRequest(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ExceptionResponse(429, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MultiValidationException.class)
    public ResponseEntity<List<ExceptionResponse>> handleMultiValidation(MultiValidationException ex) {
        return ResponseEntity.unprocessableContent().body(ex.getErrors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("globalExceptionHandler.handleHttpMessageNotReadable.invalidJson", null, locale);

        Throwable cause = ex.getCause();
        while (cause != null && !(cause instanceof InvalidFormatException)) {
            cause = cause.getCause();
        }

        if (cause instanceof InvalidFormatException invalidFormatException) {
            String field = invalidFormatException.getPath().isEmpty()
                    ? messageSource.getMessage("globalExceptionHandler.handleHttpMessageNotReadable.unknownField", null, locale)
                    : invalidFormatException.getPath().get(invalidFormatException.getPath().size() - 1).getPropertyName();
            String value = String.valueOf(invalidFormatException.getValue());

            if (invalidFormatException.getTargetType().isEnum()) {
                String allowed = Arrays.stream(invalidFormatException.getTargetType().getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                String template = messageSource.getMessage("globalExceptionHandler.handleHttpMessageNotReadable.enumError", null, locale);
                message = template.formatted(value, field, allowed);
            } else {
                String template = messageSource.getMessage("globalExceptionHandler.handleHttpMessageNotReadable.generalError", null, locale);
                message = template.formatted(value, field);
            }
        }

        return ResponseEntity.badRequest().body(new ExceptionResponse(400, message, LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        Locale locale = LocaleContextHolder.getLocale();

        String message = messageSource.getMessage("globalExceptionHandler.handleMethodArgumentTypeMismatch.error", null, locale)
                .formatted(ex.getValue(), ex.getName());

        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            String allowedValues = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            message += messageSource.getMessage("globalExceptionHandler.handleMethodArgumentTypeMismatch.allowedValues", new Object[]{allowedValues}, locale);
        }

        return ResponseEntity.badRequest().body(
                new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), message, LocalDateTime.now())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationLanguageException(MethodArgumentNotValidException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        List<String> exceptionMessages = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String exceptionMessage = error.getDefaultMessage();
            assert exceptionMessage != null;
            String messageKey = exceptionMessage.substring(1, exceptionMessage.length() - 1);
            exceptionMessage = messageSource.getMessage(messageKey, null, exceptionMessage, locale);
            exceptionMessages.add(exceptionMessage);
        });
        String combinedMessages = String.join(", ", exceptionMessages);
        ExceptionResponse exceptionResponse = new ExceptionResponse(400, combinedMessages, LocalDateTime.now());

        return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        Locale locale  = LocaleContextHolder.getLocale();
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                messageSource.getMessage("globalExceptionHandler.handleGenericException", null, locale),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(ValidationException validationException) {
        return ResponseEntity
                .badRequest()
                .body(new ExceptionResponse(400, validationException.getMessage(), LocalDateTime.now()));
    }

}
