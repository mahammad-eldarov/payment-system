package az.bank.paymentsystem.scheduler;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import az.bank.paymentsystem.service.CardService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardScheduler {

    private final CardService cardService;

    @Scheduled(cron = "${scheduler.card.cron}")
    @SchedulerLock(name = "updateExpiredCards", lockAtLeastFor = "PT5M", lockAtMostFor = "PT1H")
    public void updateExpiredCards() {
        cardService.updateExpiredCards();
    }
}
