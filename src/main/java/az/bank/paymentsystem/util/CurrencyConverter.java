package az.bank.paymentsystem.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import az.bank.paymentsystem.enums.Currency;
import org.springframework.stereotype.Component;

@Component
public class CurrencyConverter {

    private static final BigDecimal USD_TO_AZN = new BigDecimal("1.7");
    private static final BigDecimal EUR_TO_AZN = new BigDecimal("1.97");
    private static final BigDecimal AZN_TO_USD = new BigDecimal("0.59");
    private static final BigDecimal AZN_TO_EUR = new BigDecimal("0.51");

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from == to) return amount;
        BigDecimal inAzn = switch (from) {
            case USD -> amount.multiply(USD_TO_AZN);
            case EUR -> amount.multiply(EUR_TO_AZN);
            case AZN -> amount;
        };
        return switch (to) {
            case AZN -> inAzn;
            case USD -> inAzn.multiply(AZN_TO_USD).setScale(2, RoundingMode.HALF_UP);
            case EUR -> inAzn.multiply(AZN_TO_EUR).setScale(2, RoundingMode.HALF_UP);
        };
    }

    public BigDecimal convertMinBalance(BigDecimal minBalance, String minBalanceCurrency, Currency targetCurrency) {
        return convert(minBalance, Currency.valueOf(minBalanceCurrency), targetCurrency);
    }
}