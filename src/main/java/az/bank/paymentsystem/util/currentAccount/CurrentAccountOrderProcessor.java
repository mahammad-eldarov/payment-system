//package az.bank.paymentsystem.util.currentAccount;
//
//import az.bank.paymentsystem.entity.CurrentAccountEntity;
//import az.bank.paymentsystem.entity.CurrentAccountOrderEntity;
//import az.bank.paymentsystem.entity.CustomerEntity;
//import az.bank.paymentsystem.enums.CustomerStatus;
//import az.bank.paymentsystem.enums.OrderStatus;
//import az.bank.paymentsystem.repository.CurrentAccountRepository;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class CurrentAccountOrderProcessor {
//
//    private final CurrentAccountRepository currentAccountRepository;
//    private final CurrentAccountCreator currentAccountCreator;
//
//    public void process(CurrentAccountOrderEntity request) {
//        List<String> reasons = new ArrayList<>();
//        CustomerEntity customer = request.getCustomer();
//
//        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
//            reasons.add("Customer is suspended due to suspicious activity.");
//        }
//        if (currentAccountRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 3) {
//            reasons.add("Current account limit exceeded.");
//        }
//
//        if (!reasons.isEmpty()) {
//            request.setStatus(OrderStatus.REJECTED);
//            request.setRejectionReason(String.join(", ", reasons));
//        } else {
//            CurrentAccountEntity account = currentAccountCreator.createOrderAccount(request);
//            currentAccountRepository.save(account);
//            request.setStatus(OrderStatus.APPROVED);
//            request.setUpdatedAt(Instant.now());
//        }
//    }
//}
