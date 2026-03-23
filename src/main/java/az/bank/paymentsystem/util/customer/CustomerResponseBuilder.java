package az.bank.paymentsystem.util.customer;

import az.bank.paymentsystem.dto.response.TransactionResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.base.ForbiddenException;
import az.bank.paymentsystem.mapper.CardMapper;
import az.bank.paymentsystem.mapper.CurrentAccountMapper;
import az.bank.paymentsystem.util.shared.MessageUtil;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.CardResponse;
import az.bank.paymentsystem.dto.response.CurrentAccountResponse;
import az.bank.paymentsystem.dto.response.CustomerResponse;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.service.TransactionService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerResponseBuilder {

    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final TransactionService transactionService;
    private final CardMapper cardMapper;
    private final CurrentAccountMapper currentAccountMapper;
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;

    public void setCardsAndAccounts(CustomerResponse response, Integer customerId, CustomerEntity customer) {
        List<CardResponse> cardResponses = cardRepository.findCardsByCustomerId(customerId)
                .stream().map(cardMapper::toResponse).collect(Collectors.toList());


        List<CurrentAccountResponse> accountResponses = currentAccountRepository
                .findCurrentAccountByCustomerId(customerId)
                .stream().map(currentAccountMapper::toResponse).collect(Collectors.toList());

        response.setCardResponse(cardResponses);
        response.setCardMessage(cardMessage(cardResponses,customer));
        response.setCurrentAccountResponse(accountResponses);
        response.setAccountMessage(accountMessage(accountResponses,customer));
    }

    private String cardMessage(List<CardResponse> cards, CustomerEntity customer) {
        Locale locale = messageUtil.resolveLocale(customer);

        return cards.isEmpty()
                ? messageSource.getMessage("customerResponseBuilder.cardMessage.cardsEmpty", null, locale)
                : messageSource.getMessage("customerResponseBuilder.cardMessage.cardsFound", new Object[]{cards.size()}, locale);
    }

    private String accountMessage(List<CurrentAccountResponse> accounts,CustomerEntity customer) {
        Locale locale = messageUtil.resolveLocale(customer);

        return accounts.isEmpty()
                ? messageSource.getMessage("customerResponseBuilder.accountMessage.accountsEmpty", null, locale)
                : messageSource.getMessage("customerResponseBuilder.accountMessage.accountsFound", new Object[]{accounts.size()}, locale);
    }

    public Page<TransactionResponse> buildCardTransactions(Integer customerId, String pan, int page) {
        Locale fallbackLocale = LocaleContextHolder.getLocale();
        CardEntity card = cardRepository.findByPanAndIsVisibleTrue(pan)
                .orElseThrow(() -> new CardNotFoundException(messageSource.getMessage("customerResponseBuilder.buildCardTransactions.cardNotFound", null, fallbackLocale)));
        Locale locale = messageUtil.resolveLocale(card.getCustomer());
        if (!card.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException(messageSource.getMessage("customerResponseBuilder.buildCardTransactions.cardNotBelong", null, locale));
        }

        return transactionService.getTransactionsByCardId(card.getId(), page);
    }

    public Page<TransactionResponse> buildAccountTransactions(Integer customerId, String accountNumber, int page) {
        Locale fallbackLocale = LocaleContextHolder.getLocale();
        CurrentAccountEntity account = currentAccountRepository.findByAccountNumberAndIsVisibleTrue(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(messageSource.getMessage("customerResponseBuilder.buildAccountTransactions.accountNotFound",null, fallbackLocale)));
        Locale locale = messageUtil.resolveLocale(account.getCustomer());
        if (!account.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException(messageSource.getMessage("customerResponseBuilder.buildAccountTransactions.accountNotBelong",null, locale));
        }

        return transactionService.getTransactionsByAccountId(account.getId(), page);
    }
}
