package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderTinRequest;
import az.bank.paymentsystem.dto.response.TinOrderResponse;
import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.entity.TinOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.mapper.TinMapper;
import az.bank.paymentsystem.repository.TinOrderRepository;
import az.bank.paymentsystem.repository.TinRepository;
import az.bank.paymentsystem.util.tin.TinCreator;
import az.bank.paymentsystem.util.tin.TinOrderRejectionHandler;
import az.bank.paymentsystem.util.tin.TinValidator;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TinOrderService {

    private final TinOrderRepository tinOrderRepository;
    private final TinValidator tinValidator;
    private final OrderRateLimitService  orderRateLimitService;
    private final TinRepository tinRepository;
    private final TinCreator tinCreator;
    private final TinMapper tinMapper;
    private final CustomerService customerService;
    private final TinOrderRejectionHandler tinOrderRejectionHandler;

    public TinOrderResponse orderTin(Integer customerId,
                                                           OrderTinRequest request) {
        CustomerEntity customer = customerService.findActiveCustomer(customerId);

        orderRateLimitService.checkCooldown(customer, OrderType.TIN);

        TinOrderEntity orderEntity = tinCreator.createOrder(customer, request);

        try {
            tinValidator.validateTinOrder(customerId);
        } catch (MultiValidationException ex) {
            tinOrderRejectionHandler.handleRejection(orderEntity, customer, ex);
            throw ex;
        }

        TinEntity tin = tinCreator.createOrderTin(request, customer);
        tinRepository.save(tin);

        orderEntity.setStatus(OrderStatus.APPROVED);
        orderEntity.setUpdatedAt(Instant.now());
        tinOrderRepository.save(orderEntity);

        return tinMapper.toOrderResponse(tin);
    }
}