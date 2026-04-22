package az.bank.paymentsystem.scheduler;

import az.bank.paymentsystem.service.TinService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TinScheduler {

    private final TinService tinService;

    @Scheduled(cron = "${scheduler.tin.cron}")
    @SchedulerLock(name = "updateExpiredTin", lockAtLeastFor = "PT20S", lockAtMostFor = "PT5M")
    public void updateExpiredCards() {
        tinService.updateExpiredTin();

    }

}
