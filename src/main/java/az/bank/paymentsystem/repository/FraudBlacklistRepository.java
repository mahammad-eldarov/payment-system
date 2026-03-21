package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.entity.FraudBlacklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudBlacklistRepository extends JpaRepository<FraudBlacklistEntity, Integer> {

    Boolean existsByPin(String pin);
    Boolean existsByPhoneNumber(String phone);
    Boolean existsByEmail(String email);
}