package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.FraudBlacklistEntity;
import az.bank.paymentsystem.exception.FraudDetectedException;
import az.bank.paymentsystem.repository.FraudBlacklistRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FraudBlacklistChecker {
    private final FraudBlacklistRepository blacklistRepository;

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
        List<String> reasons = new ArrayList<>();

        if (blacklistRepository.existsByPin(pin)) {
            reasons.add("PIN is blacklisted due to previous fraudulent activity");
        }
        if (blacklistRepository.existsByPhoneNumber(phone)) {
            reasons.add("Phone number is associated with a previously flagged account");
        }
        if (blacklistRepository.existsByEmail(email)) {
            reasons.add("Email is associated with a previously flagged account");
        }

        if (!reasons.isEmpty()) {
            throw new FraudDetectedException(String.join(". ", reasons));
        }
    }
}
