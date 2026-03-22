package az.bank.paymentsystem.config;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bank")
@Getter
@Setter
public class BankConfig {
    private String bin;
    private Card card = new Card();
    private Account account = new Account();
    private Transaction transaction = new Transaction();
    private FraudConfig fraud;

    @Getter @Setter
    public static class FraudConfig {
        private int maxAccountCreations;
    }

    @Getter @Setter
    public static class Card {
        private BigDecimal minBalance;
        private String minBalanceCurrency;
    }

    @Getter @Setter
    public static class Account {
        private BigDecimal minBalance;
        private String minBalanceCurrency;
    }

    @Getter @Setter
    public static class Transaction {
        private BigDecimal suspiciousThreshold;
        private String suspiciousCurrency;
    }
}
