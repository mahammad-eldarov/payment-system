package az.bank.paymentsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "fraud_blacklist")
public class FraudBlacklistEntity extends BaseEntity{

    @NotNull
    @Column(name = "pin", nullable = false, length = Integer.MAX_VALUE)
    private String pin;

    @Column(name = "phone_number", length = Integer.MAX_VALUE)
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @NotNull
    @Column(name = "reason", nullable = false, length = Integer.MAX_VALUE)
    private String reason;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "blacklisted_at", nullable = false)
    private Instant blacklistedAt;


}