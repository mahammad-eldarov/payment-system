package az.bank.paymentsystem.scheduler;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import az.bank.paymentsystem.service.CurrentAccountService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAccountScheduler {

    private final CurrentAccountService currentAccountService;

    @Scheduled(cron = "${scheduler.currentAccount.cron}")
    @SchedulerLock(name = "updateExpiredCurrentAccounts", lockAtLeastFor = "PT5M", lockAtMostFor = "PT1H")
    public void updateExpiredCards() {
        currentAccountService.updateExpiredCurrentAccounts();

    }

}
