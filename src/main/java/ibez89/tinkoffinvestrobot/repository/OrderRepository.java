package ibez89.tinkoffinvestrobot.repository;

import ibez89.tinkoffinvestrobot.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStrategyExecutionId(UUID strategyExecutionId);
}
