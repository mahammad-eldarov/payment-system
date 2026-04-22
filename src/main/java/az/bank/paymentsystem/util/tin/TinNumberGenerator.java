package az.bank.paymentsystem.util.tin;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.repository.TinRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TinNumberGenerator {

    @Value("${bank.tin.number.prefix}")
    private String prefix;

    @Value("${bank.tin.number.total-length}")
    private int totalLength;

    private int randomPartLength;
    private final SecureRandom secureRandom = new SecureRandom();
    private final TinRepository tinRepository;

    @PostConstruct
    public void init() {
        this.randomPartLength = totalLength - prefix.length();
    }

    public String generate() {
        String tinNumber;
        do {
            tinNumber = buildTinNumber();
        } while (tinRepository.existsByTinNumber(tinNumber));
        return tinNumber;
    }

    private String buildTinNumber() {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < randomPartLength; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}