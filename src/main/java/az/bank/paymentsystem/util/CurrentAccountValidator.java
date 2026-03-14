package az.bank.paymentsystem.util;

import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.exception.AccountAlreadyCancelledException;
import az.bank.paymentsystem.exception.AccountExpiredException;
import az.bank.paymentsystem.exception.AccountLimitExceededException;
import org.springframework.stereotype.Component;

@Component
public class CurrentAccountValidator {

    public void validateDeletion(CurrentAccountEntity account) {
        if (account.getStatus() == CurrentAccountStatus.CLOSED) {
            throw new AccountAlreadyCancelledException("The current account has already been canceled.");
        }
        if (account.getStatus() == CurrentAccountStatus.EXPIRED) {
            throw new AccountExpiredException("An expired current account cannot be canceled.");
        }
    }

    public void validateAccountOrder(int accountCount) {
        if (accountCount >= 3) {
            throw new AccountLimitExceededException(
                    "The customer already has 3 current accounts. A new account cannot be ordered.");
        }
    }
}
