package az.bank.paymentsystem.util.customer;

import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.repository.CustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CustomerValidationInterceptor implements HandlerInterceptor {

    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        Locale locale = LocaleContextHolder.getLocale();

        Map<String, String> pathVars = (Map<String, String>)
                request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (pathVars != null && pathVars.containsKey("customerId")) {
            Integer customerId = Integer.valueOf(pathVars.get("customerId"));
            if (customerRepository.findByIdAndIsVisibleTrue(customerId).isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("application/json");
                ExceptionResponse error = new ExceptionResponse(404, messageSource.getMessage( "customerValidationInterceptor.preHandle.customerNotFound",null,locale), LocalDateTime.now());
                response.getWriter().write(objectMapper.writeValueAsString(error));
                return false;
            }
        }

        return true;
    }
}
