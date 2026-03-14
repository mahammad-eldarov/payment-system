package az.bank.paymentsystem.util.currentAccount;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
    private final CurrentAccountRepository currentAccountRepository;

    public CurrentAccountEntity createAccount(OrderCurrentAccountRequest request,
                                              CustomerEntity customer) {
        String accountNumber = accountNumberGenerator.generate();
        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setAccountNumber(accountNumber);
        account.setCurrentAccountHolderName(request.getCurrentAccountHolderName());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(request.getCurrency());
        account.setStatus(CurrentAccountStatus.ACTIVE);
        account.setActivationDate(LocalDate.now());
        account.setExpiryDate(LocalDate.now().plusYears(5));
        account.setCustomer(customer);
        account.setIsVisible(true);
        account.setCreatedAt(Instant.now());
        return account;
    }


}
