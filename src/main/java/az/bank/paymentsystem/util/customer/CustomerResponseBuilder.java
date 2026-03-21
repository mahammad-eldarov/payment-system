package az.bank.paymentsystem.util.customer;

import az.bank.paymentsystem.dto.response.CardForCustomerResponse;
import az.bank.paymentsystem.dto.response.CurrentAccountForCustomerResponse;
import az.bank.paymentsystem.dto.response.TransactionResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.TransactionEntity;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.exception.base.ForbiddenException;
import az.bank.paymentsystem.mapper.CardMapper;
import az.bank.paymentsystem.mapper.CurrentAccountMapper;
//import az.bank.paymentsystem.service.EntityFinderService;
import az.bank.paymentsystem.mapper.TransactionMapper;
import az.bank.paymentsystem.repository.TransactionRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerResponseBuilder {

    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final TransactionService transactionService;
    private final CardMapper cardMapper;
    private final CurrentAccountMapper currentAccountMapper;

    public void setCardsAndAccounts(CustomerResponse response, Integer customerId) {
        List<CardResponse> cardResponses = cardRepository.findCardsByCustomerId(customerId)
                .stream().map(cardMapper::toResponse).collect(Collectors.toList());


        List<CurrentAccountResponse> accountResponses = currentAccountRepository
                .findCurrentAccountByCustomerId(customerId)
                .stream().map(currentAccountMapper::toResponse).collect(Collectors.toList());

        response.setCardResponse(cardResponses);
        response.setCardMessage(cardMessage(cardResponses));
        response.setCurrentAccountResponse(accountResponses);
        response.setAccountMessage(accountMessage(accountResponses));
    }

//    public void setCardTransactions(CustomerResponse response, Integer customerId) {
//        List<CardResponse> cardResponses = cardRepository.findCardsByCustomerId(customerId)
//                .stream().map(card -> {
//                    CardResponse cardResponse = cardMapper.toResponse(card);
//                    cardResponse.setTransactions(transactionService.getTransactionsByCardId(card.getId(), 1).getContent());
//                    return cardResponse;
//                }).collect(Collectors.toList());
//
//        response.setCardResponse(cardResponses);
//        response.setCardMessage(cardMessage(cardResponses));
//    }
//
//    public void setAccountTransactions(CustomerResponse response, Integer customerId) {
//        List<CurrentAccountResponse> accountResponses = currentAccountRepository
//                .findCurrentAccountByCustomerId(customerId)
//                .stream().map(account -> {
//                    CurrentAccountResponse accountResponse = currentAccountMapper.toResponse(account);
//                    accountResponse.setTransactions(transactionService.getTransactionsByAccountId(account.getId(),1).getContent());
//                    return accountResponse;
//                }).collect(Collectors.toList());
//
//        response.setCurrentAccountResponse(accountResponses);
//        response.setAccountMessage(accountMessage(accountResponses));
//    }

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


    public List<CardForCustomerResponse> buildCardSummaries(Integer customerId) {
        List<CardEntity> cards = cardRepository.findCardsByCustomerId(customerId);
        if (cards.isEmpty()) throw new EmptyListException("No cards found.");
        return cards.stream().map(cardMapper::toSummary).toList();
    }

    public Page<TransactionResponse> buildCardTransactions(Integer customerId, String pan, int page) {
        CardEntity card = cardRepository.findByPanAndIsVisibleTrue(pan)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (!card.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("Card does not belong to this customer");
        }

        return transactionService.getTransactionsByCardId(card.getId(), page);
    }

    public List<CurrentAccountForCustomerResponse> buildAccountSummaries(Integer customerId) {
        List<CurrentAccountEntity> accounts = currentAccountRepository
                .findByCustomerIdAndIsVisibleTrue(customerId);
        if (accounts.isEmpty()) throw new EmptyListException("No accounts found.");
        return accounts.stream().map(currentAccountMapper::toSummary).toList();
    }

    public Page<TransactionResponse> buildAccountTransactions(Integer customerId, String accountNumber, int page) {
        CurrentAccountEntity account = currentAccountRepository.findByAccountNumberAndIsVisibleTrue(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!account.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("Account does not belong to this customer");
        }

        return transactionService.getTransactionsByAccountId(account.getId(), page);
    }
}
