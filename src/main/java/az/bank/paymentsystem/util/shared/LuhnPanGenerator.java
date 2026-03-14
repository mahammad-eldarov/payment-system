package az.bank.paymentsystem.util.shared;

import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.repository.CardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LuhnPanGenerator {

    @Value("${bank.card.number.prefix}")
    private String prefix;

    @Value("${bank.card.number.total-length}")
    private int totalLength;

    private final SecureRandom random = new SecureRandom();
    private final CardRepository cardRepository;

    public String generate() {
        String pan;
        do {
            pan = buildPan();
        } while (cardRepository.existsByPan(pan));
        return pan;
    }


    private String buildPan() {
        StringBuilder sb = new StringBuilder(prefix);
        int randomPartLength = totalLength - prefix.length() - 1;
        for (int i = 0; i < randomPartLength; i++) {
            sb.append(random.nextInt(10));
        }
        sb.append(luhnCheckDigit(sb.toString()));
        return sb.toString();
    }

    private int luhnCheckDigit(String number) {
        int sum = 0;
        boolean doubleIt = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int d = Character.getNumericValue(number.charAt(i));
            if (doubleIt && (d = d * 2) > 9) d -= 9;
            sum += d;
            doubleIt = !doubleIt;
        }
        return (10 - sum % 10) % 10;
    }




}