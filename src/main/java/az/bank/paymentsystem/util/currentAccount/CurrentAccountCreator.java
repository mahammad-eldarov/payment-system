package az.bank.paymentsystem.util.currentAccount;

import az.bank.paymentsystem.entity.CurrentAccountOrderEntity;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.OrderStatus;
//import az.bank.paymentsystem.util.shared.EnumParser;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.OrderCurrentAccountRequest;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAccountCreator {

    private final AccountNumberGenerator accountNumberGenerator;
//    private final EnumParser enumParser;

    public CurrentAccountEntity createOrderAccount(OrderCurrentAccountRequest request,
                                              CustomerEntity customer) {
        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setAccountNumber(accountNumberGenerator.generate());
        account.setCurrentAccountHolderName(request.getCurrentAccountHolderName());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(request.getCurrency());
//        account.setCurrency(Currency.valueOf(request.getCurrency().trim().toUpperCase()));
//        account.setCurrency(Currency.valueOf(request.getCurrency().toUpperCase()));
//        account.setCurrency(enumParser.parse(Currency.class, request.getCurrency(), "currency"));
        account.setStatus(CurrentAccountStatus.ACTIVE);
        account.setActivationDate(LocalDate.now());
        account.setExpiryDate(LocalDate.now().plusYears(5));
        account.setCustomer(customer);
        account.setIsVisible(true);
        account.setCreatedAt(Instant.now());
        return account;
    }

    public CurrentAccountOrderEntity createOrder(CustomerEntity customer,
                                                      OrderCurrentAccountRequest request) {
        CurrentAccountOrderEntity entity = new CurrentAccountOrderEntity();
        entity.setCustomer(customer);
        entity.setStatus(OrderStatus.PENDING);
        entity.setCurrency(request.getCurrency());
//        entity.setCurrency(Currency.valueOf(request.getCurrency().trim().toUpperCase()));
//        entity.setCurrency(Currency.valueOf(request.getCurrency().toUpperCase()));
//        entity.setCurrency(enumParser.parse(Currency.class, request.getCurrency(), "currency"));
        entity.setCreatedAt(Instant.now());
        return entity;
    }

//    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value, String fieldName) {
//        try {
//            return Enum.valueOf(enumClass, value.trim().toUpperCase());
//        } catch (IllegalArgumentException ex) {
//            String allowed = Arrays.stream(enumClass.getEnumConstants())
//                    .map(Enum::name)
//                    .collect(Collectors.joining(", "));
//            throw new IllegalArgumentException(
//                    "Invalid " + fieldName + ": '" + value + "'. Allowed values: " + allowed
//            );
//        }
//    }

}
