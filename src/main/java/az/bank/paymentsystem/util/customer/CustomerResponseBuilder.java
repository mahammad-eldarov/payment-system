package az.bank.paymentsystem.util.customer;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.CardResponse;
import az.bank.paymentsystem.dto.response.CurrentAccountResponse;
import az.bank.paymentsystem.dto.response.CustomerResponse;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.service.CardService;
import az.bank.paymentsystem.service.CurrentAccountService;
import az.bank.paymentsystem.service.TransactionService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerResponseBuilder {

    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CardService cardService;
    private final CurrentAccountService currentAccountService;
    private final TransactionService transactionService;

    public void setCardsAndAccounts(CustomerResponse response, Integer customerId) {
        List<CardResponse> cardResponses = cardRepository.findCardsByCustomerId(customerId)
                .stream().map(cardService::toResponse).collect(Collectors.toList());

        List<CurrentAccountResponse> accountResponses = currentAccountRepository
                .findCurrentAccountByCustomerId(customerId)
                .stream().map(currentAccountService::toResponse).collect(Collectors.toList());

        response.setCardResponse(cardResponses);
        response.setCardMessage(cardMessage(cardResponses));
        response.setCurrentAccountResponse(accountResponses);
        response.setAccountMessage(accountMessage(accountResponses));
    }

    public void setCardTransactions(CustomerResponse response, Integer customerId) {
        List<CardResponse> cardResponses = cardRepository.findCardsByCustomerId(customerId)
                .stream().map(card -> {
                    CardResponse cardResponse = cardService.toResponse(card);
                    cardResponse.setTransactions(transactionService.getTransactionsByCardId(card.getId(), 1).getContent());
                    return cardResponse;
                }).collect(Collectors.toList());

        response.setCardResponse(cardResponses);
        response.setCardMessage(cardMessage(cardResponses));
    }

    public void setAccountTransactions(CustomerResponse response, Integer customerId) {
        List<CurrentAccountResponse> accountResponses = currentAccountRepository
                .findCurrentAccountByCustomerId(customerId)
                .stream().map(account -> {
                    CurrentAccountResponse accountResponse = currentAccountService.toResponse(account);
                    accountResponse.setTransactions(transactionService.getTransactionsByAccountId(account.getId(),1).getContent());
                    return accountResponse;
                }).collect(Collectors.toList());

        response.setCurrentAccountResponse(accountResponses);
        response.setAccountMessage(accountMessage(accountResponses));
    }

    private String cardMessage(List<CardResponse> cards) {
        return cards.isEmpty()
                ? "This customer does not have a card."
                : "The customer has " + cards.size() + " card(s).";
    }

    private String accountMessage(List<CurrentAccountResponse> accounts) {
        return accounts.isEmpty()
                ? "This customer does not have a current account."
                : "The customer has " + accounts.size() + " current account(s).";
    }
}
