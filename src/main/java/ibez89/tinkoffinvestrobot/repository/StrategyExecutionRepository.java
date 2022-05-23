package ibez89.tinkoffinvestrobot.repository;

import ibez89.tinkoffinvestrobot.model.StrategyExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StrategyExecutionRepository extends
        JpaRepository<StrategyExecution, UUID>, JpaSpecificationExecutor<StrategyExecution> {
}
