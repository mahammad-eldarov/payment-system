package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.enums.TinStatus;
import az.bank.paymentsystem.service.NotificationService;
import java.math.BigDecimal;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TinBalanceTransfer {
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    public void transfer(TinEntity tin, Locale locale) {
        BigDecimal balance = tin.getBalance();

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            messageSource.getMessage("currentTinBalanceTransfer.transfer.tinExpired", null, locale);
            return;
        }
        if (tin.getStatus() == TinStatus.SUSPICIOUS) {
            messageSource.getMessage("currentTinBalanceTransfer.transfer.tinSuspiciousActivity", new Object[]{balance, tin.getCurrency()}, locale);
            return;
        }

        String reason = getStatusReason(tin.getStatus(),locale);

        String message = messageSource.getMessage("currentTinBalanceTransfer.transfer.visitingBranch",new Object[]{reason,balance,tin.getCurrency()},locale);

        notificationService.send(tin.getCustomer(), message);

    }

    private String getStatusReason(TinStatus status, Locale locale) {

        return switch (status) {
            case EXPIRED -> messageSource.getMessage("currentTinBalanceTransfer.getStatusReason.expired",null,locale);
            case CLOSED -> messageSource.getMessage("currentTinBalanceTransfer.getStatusReason.closed",null,locale);
            default -> messageSource.getMessage("currentTinBalanceTransfer.getStatusReason.deactivated",null,locale);
        };
    }

}
