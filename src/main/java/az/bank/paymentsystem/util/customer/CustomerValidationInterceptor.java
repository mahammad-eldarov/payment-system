package az.bank.paymentsystem.util.customer;

import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.repository.CustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
@RequiredArgsConstructor
public class CustomerValidationInterceptor implements HandlerInterceptor {

    private final CustomerRepository customerRepository;

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        Map<String, String> pathVars = (Map<String, String>)
                request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (pathVars != null && pathVars.containsKey("customerId")) {
            Integer customerId = Integer.valueOf(pathVars.get("customerId"));
            customerRepository.findByIdAndIsVisibleTrue(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        }

        return true;
    }
}
