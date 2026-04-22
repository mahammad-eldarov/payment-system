package az.bank.paymentsystem.util.tin;

import az.bank.paymentsystem.entity.TinOrderEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.OrderTinRequest;
import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.TinStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TinCreator {

    private final TinNumberGenerator tinNumberGenerator;

    public TinEntity createOrderTin(OrderTinRequest request,
                                        CustomerEntity customer) {
        TinEntity tin = new TinEntity();
        tin.setTinNumber(tinNumberGenerator.generate());
        tin.setTinHolderName(request.getTinHolderName());
        tin.setBalance(BigDecimal.ZERO);
        tin.setCurrency(request.getCurrency());
        tin.setStatus(TinStatus.ACTIVE);
        tin.setActivationDate(LocalDate.now());
        tin.setExpiryDate(LocalDate.now().plusYears(5));
        tin.setCustomer(customer);
        tin.setIsVisible(true);
        tin.setCreatedAt(Instant.now());
        return tin;
    }

    public TinOrderEntity createOrder(CustomerEntity customer,
                                                      OrderTinRequest request) {
        TinOrderEntity entity = new TinOrderEntity();
        entity.setCustomer(customer);
        entity.setStatus(OrderStatus.PENDING);
        entity.setCurrency(request.getCurrency());
        entity.setCreatedAt(Instant.now());
        return entity;
    }

}