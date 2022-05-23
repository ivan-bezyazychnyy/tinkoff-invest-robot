package ibez89.tinkoffinvestrobot.repository;

import ibez89.tinkoffinvestrobot.model.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, UUID> {

    /*
     * Extracts strategies that were not executed yet or they
     * were executed more than `executionPeriod` time ago.
     * Also, it extracts only strategies that belong to accounts
     * that do not have active execution for other strategies.
     */
    @Query(value = "SELECT s.* FROM strategy s " +
            "WHERE s.status = 'ACTIVE' AND " +
            "    s.id IN ( " +
            "        SELECT t1.id FROM ( " +
            "            SELECT s1.id, s1.execution_period, max(se1.ended_at) AS last_executed_at FROM strategy s1 " +
            "        LEFT JOIN strategy_execution se1" +
            "            ON s1.id = se1.strategy_id " +
            "            GROUP BY s1.id, s1.execution_period " +
            "        ) t1 " +
            "        WHERE t1.last_executed_at IS NULL OR " +
            "            (t1.last_executed_at + cast(concat(t1.execution_period, ' milliseconds') AS interval) < now()) " +
            "    ) AND " +
            "    s.tinkoff_account_id NOT IN ( " +
            "        SELECT DISTINCT tinkoff_account_id FROM strategy_execution " +
            "        WHERE ended_at IS NULL " +
            "    );",
            nativeQuery = true)
    List<Strategy> findPendingActiveStrategiesWithoutAccountExecutions();
}
