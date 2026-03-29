package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.dto.response.CardOrderResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CardOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.mapper.CardMapper;
import az.bank.paymentsystem.repository.CardOrderRepository;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.util.card.CardCreator;
import az.bank.paymentsystem.util.card.CardOrderRejectionHandler;
import az.bank.paymentsystem.util.card.CardValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardOrderServiceTest {

    @Mock private CardOrderRepository cardOrderRepository;
    @Mock private CustomerService customerService;
    @Mock private CardValidator cardValidator;
    @Mock private OrderRateLimitService orderRateLimitService;
    @Mock private CardMapper cardMapper;
    @Mock private CardCreator cardCreator;
    @Mock private CardRepository cardRepository;
    @Mock private CardOrderRejectionHandler cardOrderRejectionHandler;

    @InjectMocks
    private CardOrderService cardOrderService;

    private CustomerEntity customer;
    private OrderCardRequest request;
    private CardOrderEntity orderEntity;
    private CardEntity card;

    @BeforeEach
    void setUp() {
        customer = new CustomerEntity();
        customer.setId(1);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setIsVisible(true);

        request = new OrderCardRequest();
        request.setPassword("1234");

        orderEntity = new CardOrderEntity();
        orderEntity.setStatus(OrderStatus.PENDING);

        card = new CardEntity();
        card.setCvv("123");
    }

    @Test
    void shouldReturnCardOrderResponseWithCvvAndPasswordWhenOrderIsSuccessful() {
        CardOrderResponse expected = new CardOrderResponse();
        expected.setCvv("123");
        expected.setPassword("1234");

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(cardCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(cardCreator.createCard(request, customer)).thenReturn(card);
        when(cardMapper.toOrderResponse(card)).thenReturn(expected);

        CardOrderResponse actual = cardOrderService.orderCard(1, request);

        assertEquals(expected.getCvv(), actual.getCvv());
        assertEquals(expected.getPassword(), actual.getPassword());
    }

    @Test
    void shouldSaveCardAndOrderEntityWhenOrderIsSuccessful() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(cardCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(cardCreator.createCard(request, customer)).thenReturn(card);
        when(cardMapper.toOrderResponse(card)).thenReturn(new CardOrderResponse());

        cardOrderService.orderCard(1, request);

        verify(cardRepository).save(card);
        verify(cardOrderRepository).save(orderEntity);
    }

    @Test
    void shouldSetOrderStatusApprovedAndUpdatedAtWhenOrderIsSuccessful() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(cardCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(cardCreator.createCard(request, customer)).thenReturn(card);
        when(cardMapper.toOrderResponse(card)).thenReturn(new CardOrderResponse());

        cardOrderService.orderCard(1, request);

        ArgumentCaptor<CardOrderEntity> captor = ArgumentCaptor.forClass(CardOrderEntity.class);
        verify(cardOrderRepository).save(captor.capture());
        CardOrderEntity saved = captor.getValue();

        assertEquals(OrderStatus.APPROVED, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldCheckCooldownWithCardOrderTypeWhenOrderIsPlaced() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(cardCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(cardCreator.createCard(request, customer)).thenReturn(card);
        when(cardMapper.toOrderResponse(card)).thenReturn(new CardOrderResponse());

        cardOrderService.orderCard(1, request);

        verify(orderRateLimitService).checkCooldown(customer, OrderType.CARD);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        when(customerService.findActiveCustomer(99))
                .thenThrow(CustomerNotFoundException.class);

        assertThrows(CustomerNotFoundException.class,
                () -> cardOrderService.orderCard(99, request));
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenCardValidationFails() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(cardCreator.createOrder(customer, request)).thenReturn(orderEntity);
        doThrow(MultiValidationException.class)
                .when(cardValidator).validateCardOrder(1);

        assertThrows(MultiValidationException.class,
                () -> cardOrderService.orderCard(1, request));
    }

    @Test
    void shouldCallRejectionHandlerWhenCardValidationFails() {
        MultiValidationException ex = new MultiValidationException(List.of());

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(cardCreator.createOrder(customer, request)).thenReturn(orderEntity);
        doThrow(ex).when(cardValidator).validateCardOrder(1);

        assertThrows(MultiValidationException.class,
                () -> cardOrderService.orderCard(1, request));

        verify(cardOrderRejectionHandler).handleRejection(orderEntity, customer, ex);
    }

    @Test
    void shouldNotSaveCardWhenCardValidationFails() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(cardCreator.createOrder(customer, request)).thenReturn(orderEntity);
        doThrow(MultiValidationException.class)
                .when(cardValidator).validateCardOrder(1);

        assertThrows(MultiValidationException.class,
                () -> cardOrderService.orderCard(1, request));

        verify(cardRepository, never()).save(any());
        verify(cardOrderRepository, never()).save(any());
    }
}