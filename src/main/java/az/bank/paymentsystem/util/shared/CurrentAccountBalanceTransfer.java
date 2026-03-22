package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.TransactionEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.TransactionStatus;
import az.bank.paymentsystem.enums.TransactionType;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.service.NotificationService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAccountBalanceTransfer {
    private final CurrentAccountRepository currentAccountRepository;
    private final TransactionCreator transactionCreator;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    public String transfer(CurrentAccountEntity account) {
        Locale locale = LocaleContextHolder.getLocale();
        BigDecimal balance = account.getBalance();

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return messageSource.getMessage("currentAccountBalanceTransfer.transfer.accountExpired",null,locale);
        }
        if (account.getStatus() == CurrentAccountStatus.SUSPICIOUS) {
            return messageSource.getMessage("currentAccountBalanceTransfer.transfer.accountSuspiciousActivity",new Object[]{balance,account.getCurrency()},locale);
        }

        String reason = getStatusReason(account.getStatus());

        String message = messageSource.getMessage("currentAccountBalanceTransfer.transfer.visitingBranch",new Object[]{reason,balance,account.getCurrency()},locale);

        notificationService.send(account.getCustomer(), message);

        return message;
    }

    private String getStatusReason(CurrentAccountStatus status) {
        Locale locale = LocaleContextHolder.getLocale();

        return switch (status) {
            case EXPIRED -> messageSource.getMessage("currentAccountBalanceTransfer.getStatusReason.expired",null,locale);
            case CLOSED -> messageSource.getMessage("currentAccountBalanceTransfer.getStatusReason.closed",null,locale);
            default -> messageSource.getMessage("currentAccountBalanceTransfer.getStatusReason.deactivated",null,locale);
        };
    }

//    public String transfer(CurrentAccountEntity account) {
//        BigDecimal balance = account.getBalance();
//
//        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
//            return "Current account was successfully expired.";
//        }
//        if (account.getStatus() == CurrentAccountStatus.SUSPICIOUS) {
//            return "Your balance of " + balance + " " + account.getCurrency()
//                    + " has been frozen due to suspicious activity on your account.";
//        }
//
//        return "Your remaining balance of " + balance + " " + account.getCurrency()
//                + " can be collected by visiting your nearest branch.";
//    }

}
