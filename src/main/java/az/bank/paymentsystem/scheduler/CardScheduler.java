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
    @SchedulerLock(name = "updateExpiredCards", lockAtLeastFor = "PT20S", lockAtMostFor = "PT5M")
    public void updateExpiredCards() {
        cardService.updateExpiredCards();
    }
}
