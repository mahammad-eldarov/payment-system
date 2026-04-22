package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderTinRequest;
import az.bank.paymentsystem.dto.response.TinOrderResponse;
import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.entity.TinOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.mapper.TinMapper;
import az.bank.paymentsystem.repository.TinOrderRepository;
import az.bank.paymentsystem.repository.TinRepository;
import az.bank.paymentsystem.util.tin.TinCreator;
import az.bank.paymentsystem.util.tin.TinOrderRejectionHandler;
import az.bank.paymentsystem.util.tin.TinValidator;
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
class TinOrderServiceTest {

    @Mock private TinOrderRepository tinOrderRepository;
    @Mock private TinValidator tinValidator;
    @Mock private OrderRateLimitService orderRateLimitService;
    @Mock private TinRepository tinRepository;
    @Mock private TinCreator tinCreator;
    @Mock private TinMapper tinMapper;
    @Mock private CustomerService customerService;
    @Mock private TinOrderRejectionHandler tinOrderRejectionHandler;

    @InjectMocks
    private TinOrderService tinOrderService;

    private CustomerEntity customer;
    private OrderTinRequest request;
    private TinOrderEntity orderEntity;
    private TinEntity tin;

    @BeforeEach
    void setUp() {
        customer = new CustomerEntity();
        customer.setId(1);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setIsVisible(true);

        request = new OrderTinRequest();

        orderEntity = new TinOrderEntity();
        orderEntity.setStatus(OrderStatus.PENDING);

        tin = new TinEntity();
    }

    @Test
    void shouldReturnTinOrderResponseWhenOrderIsSuccessful() {
        TinOrderResponse expected = new TinOrderResponse();

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(tinCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(tinCreator.createOrderTin(request, customer)).thenReturn(tin);
        when(tinMapper.toOrderResponse(tin)).thenReturn(expected);

        TinOrderResponse actual = tinOrderService.orderTin(1, request);

        assertEquals(expected, actual);
    }

    @Test
    void shouldSaveTinAndOrderEntityWhenOrderIsSuccessful() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(tinCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(tinCreator.createOrderTin(request, customer)).thenReturn(tin);
        when(tinMapper.toOrderResponse(tin)).thenReturn(new TinOrderResponse());

        tinOrderService.orderTin(1, request);

        verify(tinRepository).save(tin);
        verify(tinOrderRepository).save(orderEntity);
    }

    @Test
    void shouldSetOrderStatusApprovedAndUpdatedAtWhenOrderIsSuccessful() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(tinCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(tinCreator.createOrderTin(request, customer)).thenReturn(tin);
        when(tinMapper.toOrderResponse(tin)).thenReturn(new TinOrderResponse());

        tinOrderService.orderTin(1, request);

        ArgumentCaptor<TinOrderEntity> captor =
                ArgumentCaptor.forClass(TinOrderEntity.class);
        verify(tinOrderRepository).save(captor.capture());
        TinOrderEntity saved = captor.getValue();

        assertEquals(OrderStatus.APPROVED, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldCheckCooldownWithTinOrderTypeWhenOrderIsPlaced() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(tinCreator.createOrder(customer, request)).thenReturn(orderEntity);
        when(tinCreator.createOrderTin(request, customer)).thenReturn(tin);
        when(tinMapper.toOrderResponse(tin)).thenReturn(new TinOrderResponse());

        tinOrderService.orderTin(1, request);

        verify(orderRateLimitService).checkCooldown(customer, OrderType.TIN);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        Class<CustomerNotFoundException> expected = CustomerNotFoundException.class;

        when(customerService.findActiveCustomer(99)).thenThrow(expected);

        assertThrows(expected, () -> tinOrderService.orderTin(99, request));
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenTinValidationFails() {
        Class<MultiValidationException> expected = MultiValidationException.class;

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(tinCreator.createOrder(customer, request)).thenReturn(orderEntity);
        doThrow(expected).when(tinValidator).validateTinOrder(1);

        assertThrows(expected, () -> tinOrderService.orderTin(1, request));
    }

    @Test
    void shouldCallRejectionHandlerWhenTinValidationFails() {
        Class<MultiValidationException> expected = MultiValidationException.class;
        MultiValidationException ex = new MultiValidationException(List.of());

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(tinCreator.createOrder(customer, request)).thenReturn(orderEntity);
        doThrow(ex).when(tinValidator).validateTinOrder(1);

        assertThrows(expected, () -> tinOrderService.orderTin(1, request));

        verify(tinOrderRejectionHandler).handleRejection(orderEntity, customer, ex);
    }

    @Test
    void shouldNotSaveTinWhenTinValidationFails() {
        Class<MultiValidationException> expected = MultiValidationException.class;

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(tinCreator.createOrder(customer, request)).thenReturn(orderEntity);
        doThrow(expected).when(tinValidator).validateTinOrder(1);

        assertThrows(expected, () -> tinOrderService.orderTin(1, request));

        verify(tinRepository, never()).save(any());
        verify(tinOrderRepository, never()).save(any());
    }
}