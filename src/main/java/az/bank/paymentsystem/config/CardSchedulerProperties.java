package az.bank.paymentsystem.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "scheduler.card")
@Getter
@Setter
public class CardSchedulerProperties {
    private String cron;
}