package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.client.CbarCurrencyClient;
import az.bank.paymentsystem.dto.response.CbarResponse;
import az.bank.paymentsystem.exception.RateNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import az.bank.paymentsystem.enums.Currency;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrencyConverter {

    private final CbarCurrencyClient cbarClient;
    private final MessageSource messageSource;

    private BigDecimal getRate(String currencyCode) {
        Locale locale = LocaleContextHolder.getLocale();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        CbarResponse response = cbarClient.getRates(today);

        return response.getValTypes().stream()
                .flatMap(valType -> valType.getValuteList().stream())
                .filter(v -> v.getCode().equalsIgnoreCase(currencyCode))
                .findFirst()
                .map(CbarResponse.Valute::getValue)
                .orElseThrow(() -> new RateNotFoundException(messageSource.getMessage("currencyConverter.getRate.rateNotFound",new Object[]{currencyCode},locale)));
    }

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from == to) return amount;

//        BigDecimal inAzn = switch (from) {
//            case AZN -> amount;
//            default -> amount.multiply(getRate(from.name()));
//        };
        BigDecimal inAzn = switch (from) {
            case USD, EUR -> amount.multiply(getRate(from.name()));
            case AZN -> amount;
        };

//        return switch (to) {
//            case AZN -> inAzn;
//            default -> inAzn.divide(getRate(to.name()), 2, RoundingMode.HALF_UP);
//        };
        return switch (to) {
            case AZN -> inAzn;
            case USD, EUR -> inAzn.divide(getRate(to.name()), 2, RoundingMode.HALF_UP);
        };
    }

    public BigDecimal convertMinBalance(BigDecimal minBalance, String minBalanceCurrency, Currency targetCurrency) {
        return convert(minBalance, Currency.valueOf(minBalanceCurrency), targetCurrency);
    }


//    private static final BigDecimal USD_TO_AZN = new BigDecimal("1.7");
//    private static final BigDecimal EUR_TO_AZN = new BigDecimal("1.97");
//    private static final BigDecimal AZN_TO_USD = new BigDecimal("0.59");
//    private static final BigDecimal AZN_TO_EUR = new BigDecimal("0.51");
//
//    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
//        if (from == to) return amount;
//        BigDecimal inAzn = switch (from) {
//            case USD -> amount.multiply(USD_TO_AZN);
//            case EUR -> amount.multiply(EUR_TO_AZN);
//            case AZN -> amount;
//        };
//        return switch (to) {
//            case AZN -> inAzn;
//            case USD -> inAzn.multiply(AZN_TO_USD).setScale(2, RoundingMode.HALF_UP);
//            case EUR -> inAzn.multiply(AZN_TO_EUR).setScale(2, RoundingMode.HALF_UP);
//        };
//    }
//
//    public BigDecimal convertMinBalance(BigDecimal minBalance, String minBalanceCurrency, Currency targetCurrency) {
//        return convert(minBalance, Currency.valueOf(minBalanceCurrency), targetCurrency);
//    }
}