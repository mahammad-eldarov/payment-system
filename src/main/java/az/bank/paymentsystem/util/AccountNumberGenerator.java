package az.bank.paymentsystem.util;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {

    @Value("${bank.account.number.prefix}")
    private String prefix;

    @Value("${bank.account.number.total-length}")
    private int totalLength;

    private int randomPartLength;
    private final SecureRandom secureRandom = new SecureRandom();
    private final CurrentAccountRepository currentAccountRepository;

    @PostConstruct
    public void init() {
        this.randomPartLength = totalLength - prefix.length();
    }

    public String generate() {
        String accountNumber;
        do {
            accountNumber = buildAccountNumber();
        } while (currentAccountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private String buildAccountNumber() {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < randomPartLength; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}