package az.bank.paymentsystem.util.payment;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class PaymentCooldownChecker {

    private final Map<String, Instant> cooldownCache = new ConcurrentHashMap<>();
    private static final Duration COOLDOWN_DURATION = Duration.ofSeconds(10);

    public boolean isInCooldown(Integer customerId, BigDecimal amount,
                                String fromSource, String toSource) {
        String key = customerId + ":" + amount + ":" + fromSource + ":" + toSource;
        Instant lastAttempt = cooldownCache.get(key);

        if (lastAttempt != null && Instant.now().isBefore(lastAttempt.plus(COOLDOWN_DURATION))) {
            return true;
        }

        cooldownCache.put(key, Instant.now());
        return false;
    }


}
