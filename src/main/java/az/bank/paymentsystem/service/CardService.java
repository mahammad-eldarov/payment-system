package az.bank.paymentsystem.service;

import az.bank.paymentsystem.util.shared.CardBalanceTransfer;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.UpdateCardPasswordRequest;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.dto.response.CardResponse;
import az.bank.paymentsystem.mapper.CardMapper;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.card.CardCreator;
import az.bank.paymentsystem.util.card.CardValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final CardValidator cardValidator;
    private final CardCreator cardCreator;
    private final CardMapper cardMapper;
    private final CardBalanceTransfer cardBalanceTransfer;


    // CREATE
    public CardResponse orderCard(Integer customerId, OrderCardRequest request) {
        CustomerEntity customer = findActiveCustomer(customerId);
        cardValidator.validateCardOrder(customerId);

        CardEntity card = cardCreator.createCard(request, customer);
        cardRepository.save(card);

        CardResponse response = toResponse(card);
        response.setCvv(card.getCvv());
        response.setPassword(request.getPassword());

        return response;
    }

    // DELETE
//    public MessageResponse deleteCard(Integer cardId) {
//        CardEntity card = findActiveCard(cardId);
//        cardValidator.validateCardDeletion(card);
//        card.setStatus(CardStatus.CLOSED);
//        card.setIsVisible(false);
//        card.setUpdatedAt(Instant.now());
//        cardRepository.save(card);
//        return new MessageResponse("Card was successfully deleted.");
//    }
    public MessageResponse deleteCard(Integer cardId) {
        CardEntity card = findActiveCard(cardId);
        cardValidator.validateCardDeletion(card);

        card.setStatus(CardStatus.CLOSED);
        card.setIsVisible(false);
        card.setUpdatedAt(Instant.now());

        String message = cardBalanceTransfer.transfer(card);
        cardRepository.save(card);
        return new MessageResponse(message);
    }

    // UPDATE
//    public void updateExpiredCards() {
//        List<CardEntity> expiredCards = cardRepository
//                .findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate.now(), CardStatus.EXPIRED);
//        expiredCards.forEach(card -> {
//            card.setStatus(CardStatus.EXPIRED);
//            card.setUpdatedAt(Instant.now());
//        });
//        cardRepository.saveAll(expiredCards);
//    }

    @Transactional
    public void updateExpiredCards() {
        List<CardEntity> expiredCards = cardRepository
                .findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate.now(), CardStatus.EXPIRED);

        expiredCards.forEach(card -> {
            card.setStatus(CardStatus.EXPIRED);
//            card.setIsVisible(false); //why?
            card.setUpdatedAt(Instant.now());
            cardBalanceTransfer.transfer(card);
        });

        cardRepository.saveAll(expiredCards);
    }

    public MessageResponse updateCardStatus(Integer id, CardStatus status) {
        CardEntity card = findActiveCard(id);
        card.setStatus(status);
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);
        return new MessageResponse("Card status updated successfully");
    }

    public MessageResponse updateCardPassword(Integer id, UpdateCardPasswordRequest request) {
        CardEntity card = findActiveCard(id);
        card.setPassword(request.getPassword());
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);
        return new MessageResponse("Card password updated successfully");
    }

    // GET
    public List<CardResponse> getCardsByCustomerId(Integer customerId) {
        findActiveCustomer(customerId);
        List<CardEntity> cards = cardRepository.findCardsByCustomerId(customerId);
        if (cards.isEmpty()) {
            throw new EmptyListException("This customer does not have any cards.");
        }
        return cards.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CardResponse getCardByPan(String pan) {
        CardEntity card = cardRepository.findByPanAndIsVisibleTrue(pan)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        return toResponse(card);
    }

    public List<CardResponse> getCardsByStatus(CardStatus status) {
        List<CardEntity> cards = cardRepository.findByStatusAndIsVisibleTrue(status);
        if (cards.isEmpty()) {
            throw new CardNotFoundException("No cards found with this status");
        }
        return cards.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // RESPONSE
    public CardResponse toResponse(CardEntity card) {
        return cardMapper.toResponse(card);
    }

    // AUXILIARY METHODS
    public CardEntity findActiveCard(Integer id) {
        return cardRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
    }

    public CustomerEntity findActiveCustomer(Integer id) {
        return customerRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }
}