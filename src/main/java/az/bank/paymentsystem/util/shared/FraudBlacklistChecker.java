package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.FraudBlacklistEntity;
import az.bank.paymentsystem.exception.FraudDetectedException;
import az.bank.paymentsystem.repository.FraudBlacklistRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FraudBlacklistChecker {
    private final FraudBlacklistRepository blacklistRepository;
    private final MessageSource messageSource;

    public void addToBlacklist(CustomerEntity customer, String reason) {
        FraudBlacklistEntity entry = new FraudBlacklistEntity();
        entry.setPin(customer.getPin());
        entry.setPhoneNumber(customer.getPhoneNumber());
        entry.setEmail(customer.getEmail());
        entry.setReason(reason);
        entry.setBlacklistedAt(Instant.now());
        blacklistRepository.save(entry);
    }

    public void checkBlacklist(String pin, String phone, String email) {
        Locale locale = LocaleContextHolder.getLocale();
        List<String> reasons = new ArrayList<>();

        if (blacklistRepository.existsByPin(pin)) {
            reasons.add(messageSource.getMessage("fraudBlacklistChecker.checkBlacklist.pin", null, locale));
        }
        if (blacklistRepository.existsByPhoneNumber(phone)) {
            reasons.add(messageSource.getMessage("fraudBlacklistChecker.checkBlacklist.phoneNumber", null, locale));
        }
        if (blacklistRepository.existsByEmail(email)) {
            reasons.add(messageSource.getMessage("fraudBlacklistChecker.checkBlacklist.email", null, locale));
        }

        if (!reasons.isEmpty()) {
            throw new FraudDetectedException(String.join(". ", reasons));
        }
    }
}
