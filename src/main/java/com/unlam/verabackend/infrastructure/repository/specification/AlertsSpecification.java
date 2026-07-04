package com.unlam.verabackend.infrastructure.repository.specification;

import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class AlertsSpecification {

    public static Specification<AlertsEntity> filterAlerts(
            List<Long> trustContactIds,
            Boolean isResolved,
            RiskLevel riskLevel,
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("trustContact").get("id").in(trustContactIds));

            if (isResolved != null) {
                predicates.add(criteriaBuilder.equal(root.get("isResolved"), isResolved));
            }

            if (riskLevel != null) {
                predicates.add(criteriaBuilder.equal(root.get("riskLevel"), riskLevel));
            }

            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), keyword
                );
                Predicate summaryPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("contentSummary")), keyword
                );
                predicates.add(criteriaBuilder.or(titlePredicate, summaryPredicate));
            }

            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}