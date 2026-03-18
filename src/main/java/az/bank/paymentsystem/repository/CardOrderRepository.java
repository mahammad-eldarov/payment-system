package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.entity.CardOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardOrderRepository extends JpaRepository<CardOrderEntity, Integer> {

}
