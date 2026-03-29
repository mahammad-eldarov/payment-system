package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.UpdateCardPasswordRequest;
import az.bank.paymentsystem.dto.response.CardResponse;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.Language;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.mapper.CardMapper;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.card.CardValidator;
import az.bank.paymentsystem.util.shared.CardBalanceTransfer;
import az.bank.paymentsystem.util.shared.MessageUtil;
import az.bank.paymentsystem.util.shared.StatusAuditLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private CardValidator cardValidator;
    @Mock private CardMapper cardMapper;
    @Mock private CardBalanceTransfer cardBalanceTransfer;
    @Mock private StatusAuditLogger statusAuditLogger;
    @Mock private MessageSource messageSource;
    @Mock private MessageUtil messageUtil;

    @Captor private ArgumentCaptor<List<CardEntity>> cardListCaptor;

    @InjectMocks
    private CardService cardService;

    private CardEntity card;
    private CustomerEntity customer;

    @BeforeEach
    void setUp() {
        customer = new CustomerEntity();
        customer.setId(1);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setLanguage(Language.EN);
        customer.setIsVisible(true);

        card = new CardEntity();
        card.setId(1);
        card.setStatus(CardStatus.ACTIVE);
        card.setIsVisible(true);
        card.setCustomer(customer);

        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("mocked-message");
        lenient().when(messageUtil.resolveLocale(any(CustomerEntity.class)))
                .thenReturn(Language.EN.toLocale());
    }

    @Test
    void shouldSetCardStatusClosedAndSaveWhenCardIsDeleted() {
        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));

        cardService.deleteCard(1);

        ArgumentCaptor<CardEntity> captor = ArgumentCaptor.forClass(CardEntity.class);
        verify(cardRepository).save(captor.capture());
        CardEntity saved = captor.getValue();

        assertEquals(CardStatus.CLOSED, saved.getStatus());
        assertFalse(saved.getIsVisible());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldCallBalanceTransferWhenCardIsDeleted() {
        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));

        cardService.deleteCard(1);

        verify(cardBalanceTransfer).transfer(eq(card), any(Locale.class));
    }

    @Test
    void shouldCallStatusAuditLoggerWhenCardIsDeleted() {
        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));

        cardService.deleteCard(1);

        verify(statusAuditLogger).logCard(eq(card), eq(CardStatus.CLOSED.name()), anyString());
    }

    @Test
    void shouldCallValidatorWhenCardIsDeleted() {
        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));

        cardService.deleteCard(1);

        verify(cardValidator).validateCardDeletion(card);
    }

    @Test
    void shouldReturnMessageResponseWhenCardIsDeleted() {
        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));

        MessageResponse expected = new MessageResponse("mocked-message");

        MessageResponse actual = cardService.deleteCard(1);

        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExistForDelete() {
        Class<CardNotFoundException> expected = CardNotFoundException.class;

        when(cardRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> cardService.deleteCard(99));
    }

    @Test
    void shouldSetExpiredStatusAndSaveAllExpiredCards() {
        CardEntity expiredCard = new CardEntity();
        expiredCard.setId(2);
        expiredCard.setStatus(CardStatus.ACTIVE);
        expiredCard.setCustomer(customer);

        when(cardRepository.findAllByExpiryDateLessThanEqualAndStatusNot(
                any(LocalDate.class), eq(CardStatus.EXPIRED)))
                .thenReturn(List.of(expiredCard));

        cardService.updateExpiredCards();

        verify(cardRepository).saveAll(cardListCaptor.capture());
        CardEntity saved = cardListCaptor.getValue().get(0);

        assertEquals(CardStatus.EXPIRED, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldCallBalanceTransferForEachExpiredCard() {
        CardEntity expiredCard = new CardEntity();
        expiredCard.setId(2);
        expiredCard.setStatus(CardStatus.ACTIVE);
        expiredCard.setCustomer(customer);

        when(cardRepository.findAllByExpiryDateLessThanEqualAndStatusNot(
                any(LocalDate.class), eq(CardStatus.EXPIRED)))
                .thenReturn(List.of(expiredCard));

        cardService.updateExpiredCards();

        verify(cardBalanceTransfer).transfer(eq(expiredCard), any(Locale.class));
    }

    @Test
    void shouldNotSaveAnyCardWhenNoExpiredCardsFound() {
        when(cardRepository.findAllByExpiryDateLessThanEqualAndStatusNot(
                any(LocalDate.class), eq(CardStatus.EXPIRED)))
                .thenReturn(Collections.emptyList());

        cardService.updateExpiredCards();

        verify(cardRepository).saveAll(Collections.emptyList());
        verify(cardBalanceTransfer, never()).transfer(any(), any());
    }

    @Test
    void shouldUpdateCardStatusAndSaveWhenCardIsActive() {
        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));

        MessageResponse expected = new MessageResponse("mocked-message");

        MessageResponse actual = cardService.updateCardStatus(1, CardStatus.BLOCKED);

        ArgumentCaptor<CardEntity> captor = ArgumentCaptor.forClass(CardEntity.class);
        verify(cardRepository).save(captor.capture());
        CardEntity saved = captor.getValue();

        assertEquals(CardStatus.BLOCKED, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void shouldCallStatusAuditLoggerWhenCardStatusIsUpdated() {
        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));

        cardService.updateCardStatus(1, CardStatus.BLOCKED);

        verify(statusAuditLogger).logCard(eq(card), eq(CardStatus.BLOCKED.name()), anyString());
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExistForStatusUpdate() {
        Class<CardNotFoundException> expected = CardNotFoundException.class;

        when(cardRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> cardService.updateCardStatus(99, CardStatus.BLOCKED));
    }

    @Test
    void shouldUpdateCardPasswordAndSaveWhenCardIsActive() {
        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));

        UpdateCardPasswordRequest request = new UpdateCardPasswordRequest();
        request.setPassword("newPass123");

        MessageResponse expected = new MessageResponse("mocked-message");

        MessageResponse actual = cardService.updateCardPassword(1, request);

        ArgumentCaptor<CardEntity> captor = ArgumentCaptor.forClass(CardEntity.class);
        verify(cardRepository).save(captor.capture());
        CardEntity saved = captor.getValue();

        assertEquals("newPass123", saved.getPassword());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExistForPasswordUpdate() {
        Class<CardNotFoundException> expected = CardNotFoundException.class;

        when(cardRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> cardService.updateCardPassword(99, new UpdateCardPasswordRequest()));
    }

    @Test
    void shouldReturnCardResponseListWhenCustomerHasCards() {
        CardResponse expected = new CardResponse();
        expected.setId(1);

        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(customer));
        when(cardRepository.findCardsByCustomerId(1)).thenReturn(List.of(card));
        when(cardMapper.toResponse(card)).thenReturn(expected);

        List<CardResponse> actual = cardService.getCardsByCustomerId(1);

        assertEquals(1, actual.size());
        assertEquals(expected.getId(), actual.get(0).getId());
    }

    @Test
    void shouldThrowEmptyListExceptionWhenCustomerHasNoCards() {
        Class<EmptyListException> expected = EmptyListException.class;

        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(customer));
        when(cardRepository.findCardsByCustomerId(1)).thenReturn(Collections.emptyList());

        assertThrows(expected, () -> cardService.getCardsByCustomerId(1));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExistForGetCards() {
        Class<CustomerNotFoundException> expected = CustomerNotFoundException.class;

        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> cardService.getCardsByCustomerId(99));
    }

    @Test
    void shouldReturnCardResponseWhenCardFoundByPan() {
        CardResponse expected = new CardResponse();
        expected.setId(1);

        when(cardRepository.findByPanAndIsVisibleTrue("4000123456789012"))
                .thenReturn(Optional.of(card));
        when(cardMapper.toResponse(card)).thenReturn(expected);

        CardResponse actual = cardService.getCardByPan("4000123456789012");

        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenCardNotFoundByPan() {
        Class<CardNotFoundException> expected = CardNotFoundException.class;

        when(cardRepository.findByPanAndIsVisibleTrue("0000000000000000"))
                .thenReturn(Optional.empty());

        assertThrows(expected, () -> cardService.getCardByPan("0000000000000000"));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForGetByStatus() {
        Class<PageRequestException> expected = PageRequestException.class;

        assertThrows(expected, () -> cardService.getCardsByStatus(CardStatus.ACTIVE, 0));
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenNoCardsMatchGivenStatus() {
        Class<CardNotFoundException> expected = CardNotFoundException.class;

        when(cardRepository.findByStatus(eq(CardStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(expected, () -> cardService.getCardsByStatus(CardStatus.ACTIVE, 1));
    }

    @Test
    void shouldReturnMappedPageWhenCardsFoundByStatus() {
        CardResponse expected = new CardResponse();
        expected.setId(1);

        Page<CardEntity> entityPage = new PageImpl<>(List.of(card));
        when(cardRepository.findByStatus(eq(CardStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(entityPage);
        when(cardMapper.toResponse(card)).thenReturn(expected);

        Page<CardResponse> actual = cardService.getCardsByStatus(CardStatus.ACTIVE, 1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldReturnCardEntityWhenActiveCardIsFound() {
        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));

        CardEntity actual = cardService.findActiveCard(1);

        assertEquals(card.getId(), actual.getId());
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenActiveCardDoesNotExist() {
        Class<CardNotFoundException> expected = CardNotFoundException.class;

        when(cardRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> cardService.findActiveCard(99));
    }

    @Test
    void shouldReturnCustomerEntityWhenActiveCustomerIsFound() {
        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(customer));

        CustomerEntity actual = cardService.findActiveCustomer(1);

        assertEquals(customer.getId(), actual.getId());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenActiveCustomerDoesNotExist() {
        Class<CustomerNotFoundException> expected = CustomerNotFoundException.class;

        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> cardService.findActiveCustomer(99));
    }
}