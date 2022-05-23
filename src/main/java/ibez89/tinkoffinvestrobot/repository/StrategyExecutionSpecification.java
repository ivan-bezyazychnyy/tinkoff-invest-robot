package ibez89.tinkoffinvestrobot.repository;

import ibez89.tinkoffinvestrobot.model.StrategyExecution;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@Builder
public class StrategyExecutionSpecification implements Specification<StrategyExecution> {

    private final Set<UUID> strategyIds;

    private final Instant from;

    private final Instant to;

    @Override
    public Predicate toPredicate(
            Root<StrategyExecution> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder) {
        var predicates = new ArrayList<Predicate>();

        if (strategyIds != null && !strategyIds.isEmpty()) {
            predicates.add(root.get("strategyId").in(strategyIds));
        }

        if (from != null) {
            predicates.add(
                criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("endedAt")),
                        criteriaBuilder.greaterThan(root.get("endedAt"), from)));
        }

        if (to != null) {
            predicates.add(criteriaBuilder.lessThan(root.get("startedAt"), to));
        }

        return predicates.stream()
                .reduce(criteriaBuilder::and)
                .orElse(criteriaBuilder.conjunction());
    }
}
