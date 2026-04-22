package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.entity.TinOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TinOrderRepository extends JpaRepository<TinOrderEntity, Integer> {
}
