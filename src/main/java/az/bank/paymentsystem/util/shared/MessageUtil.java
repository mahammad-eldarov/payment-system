package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.Language;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class MessageUtil {

    public Locale resolveLocale(CustomerEntity customer) {
        if (customer != null && customer.getLanguage() != null) {
            return customer.getLanguage().toLocale();
        }
        return Language.AZ.toLocale();
    }
}
