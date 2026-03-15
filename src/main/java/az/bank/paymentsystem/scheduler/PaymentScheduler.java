package az.bank.paymentsystem.scheduler;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import az.bank.paymentsystem.service.PaymentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentService paymentService;

    @Scheduled(cron = "${scheduler.payment.cron}")
    @SchedulerLock(name = "processPendingPayments", lockAtLeastFor = "PT20S",
            lockAtMostFor = "PT5M")
    public void processPendingPayments() {
        paymentService.processPayments();
    }
}


