package az.bank.paymentsystem.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class CvvGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        return String.format("%03d", secureRandom.nextInt(1000));
    }
}
