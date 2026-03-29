package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderCurrentAccountRequest;
import az.bank.paymentsystem.dto.response.CurrentAccountOrderResponse;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CurrentAccountOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.mapper.CurrentAccountMapper;
import az.bank.paymentsystem.repository.CurrentAccountOrderRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountCreator;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountOrderRejectionHandler;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentAccountOrderServiceTest {

    @Mock private CurrentAccountOrderRepository currentAccountOrderRepository;
    @Mock private CurrentAccountValidator currentAccountValidator;
    @Mock private OrderRateLimitService orderRateLimitService;
    @Mock private CurrentAccountRepository currentAccountRepository;
    @Mock private CurrentAccountCreator currentAccountCreator;
    @Mock private CurrentAccountMapper currentAccountMapper;
    @Mock private CustomerService customerService;
    @Mock private CurrentAccountOrderRejectionHandler currentAccountOrderRejectionHandler;

    @InjectMocks
    private CurrentAccountOrderService currentAccountOrderService;

    private CustomerEntity customer;
    private OrderCurrentAccountRequest request;
    private CurrentAccountOrderEntity orderEntity;
    private CurrentAccountEntity account;

    @BeforeEach
    void setUp() {
        customer = new CustomerEntity();
        customer.setId(1);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setIsVisible(true);

        request = new OrderCurrentAccountRequest();

        orderEntity = new CurrentAccountOrderEntity();
        orderEntity.setStatus(OrderStatus.PENDING);

        account = new CurrentAccountEntity();
    }

    @Test
    void shouldReturnCurrentAccountOrderResponseWhenOrderIsSuccessful() {
        CurrentAccountOrderResponse expected = new CurrentAccountOrderResponse();

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(currentAccountCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(currentAccountCreator.createOrderAccount(request, customer)).thenReturn(account);
        when(currentAccountMapper.toOrderResponse(account)).thenReturn(expected);

        CurrentAccountOrderResponse actual = currentAccountOrderService.orderCurrentAccount(1, request);

        assertEquals(expected, actual);
    }

    @Test
    void shouldSaveAccountAndOrderEntityWhenOrderIsSuccessful() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(currentAccountCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(currentAccountCreator.createOrderAccount(request, customer)).thenReturn(account);
        when(currentAccountMapper.toOrderResponse(account)).thenReturn(new CurrentAccountOrderResponse());

        currentAccountOrderService.orderCurrentAccount(1, request);

        verify(currentAccountRepository).save(account);
        verify(currentAccountOrderRepository).save(orderEntity);
    }

    @Test
    void shouldSetOrderStatusApprovedAndUpdatedAtWhenOrderIsSuccessful() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(currentAccountCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(currentAccountCreator.createOrderAccount(request, customer)).thenReturn(account);
        when(currentAccountMapper.toOrderResponse(account)).thenReturn(new CurrentAccountOrderResponse());

        currentAccountOrderService.orderCurrentAccount(1, request);

        ArgumentCaptor<CurrentAccountOrderEntity> captor =
                ArgumentCaptor.forClass(CurrentAccountOrderEntity.class);
        verify(currentAccountOrderRepository).save(captor.capture());
        CurrentAccountOrderEntity saved = captor.getValue();

        assertEquals(OrderStatus.APPROVED, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldCheckCooldownWithCurrentAccountOrderTypeWhenOrderIsPlaced() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(currentAccountCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(currentAccountCreator.createOrderAccount(request, customer)).thenReturn(account);
        when(currentAccountMapper.toOrderResponse(account)).thenReturn(new CurrentAccountOrderResponse());

        currentAccountOrderService.orderCurrentAccount(1, request);

        verify(orderRateLimitService).checkCooldown(customer, OrderType.CURRENT_ACCOUNT);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        when(customerService.findActiveCustomer(99)).thenThrow(CustomerNotFoundException.class);

        assertThrows(CustomerNotFoundException.class,
                () -> currentAccountOrderService.orderCurrentAccount(99, request));
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenCurrentAccountValidationFails() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(currentAccountCreator.createOrder(customer, request)).thenReturn(orderEntity);
        doThrow(MultiValidationException.class)
                .when(currentAccountValidator).validateCurrentAccountOrder(1);

        assertThrows(MultiValidationException.class,
                () -> currentAccountOrderService.orderCurrentAccount(1, request));
    }

    @Test
    void shouldCallRejectionHandlerWhenCurrentAccountValidationFails() {
        MultiValidationException ex = new MultiValidationException(List.of());

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(currentAccountCreator.createOrder(customer, request)).thenReturn(orderEntity);
        doThrow(ex).when(currentAccountValidator).validateCurrentAccountOrder(1);

        assertThrows(MultiValidationException.class,
                () -> currentAccountOrderService.orderCurrentAccount(1, request));

        verify(currentAccountOrderRejectionHandler).handleRejection(orderEntity, customer, ex);
    }

    @Test
    void shouldNotSaveAccountWhenCurrentAccountValidationFails() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(currentAccountCreator.createOrder(customer, request)).thenReturn(orderEntity);
        doThrow(MultiValidationException.class)
                .when(currentAccountValidator).validateCurrentAccountOrder(1);

        assertThrows(MultiValidationException.class,
                () -> currentAccountOrderService.orderCurrentAccount(1, request));

        verify(currentAccountRepository, never()).save(any());
        verify(currentAccountOrderRepository, never()).save(any());
    }
}