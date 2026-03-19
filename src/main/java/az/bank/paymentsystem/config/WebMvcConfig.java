package az.bank.paymentsystem.config;

import az.bank.paymentsystem.util.customer.CustomerValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CustomerValidationInterceptor customerValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customerValidationInterceptor)
                .addPathPatterns("/api/v1/current-account-order/customer/*",
                                 "/api/v1/card-order/customer/*");
    }
}
