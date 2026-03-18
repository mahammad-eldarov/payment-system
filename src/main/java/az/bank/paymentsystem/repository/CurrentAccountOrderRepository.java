package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.entity.CurrentAccountOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrentAccountOrderRepository extends JpaRepository<CurrentAccountOrderEntity, Integer> {
}
