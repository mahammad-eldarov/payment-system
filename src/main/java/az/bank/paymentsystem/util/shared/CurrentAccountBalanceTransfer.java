package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.TransactionEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.TransactionStatus;
import az.bank.paymentsystem.enums.TransactionType;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAccountBalanceTransfer {
    private final CurrentAccountRepository currentAccountRepository;
    private final TransactionCreator transactionCreator;

    public String transfer(CurrentAccountEntity account) {
        BigDecimal balance = account.getBalance();

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return "Current account was successfully expired.";
        }
        if (account.getStatus() == CurrentAccountStatus.SUSPICIOUS) {
            return "Your balance of " + balance + " " + account.getCurrency()
                    + " has been frozen due to suspicious activity on your account.";
        }

//        transactionCreator.createAccountExpiredTransfer(account, balance);
//        account.setBalance(BigDecimal.ZERO);
        return "Your remaining balance of " + balance + " " + account.getCurrency()
                + " can be collected by visiting your nearest branch.";
    }

}
