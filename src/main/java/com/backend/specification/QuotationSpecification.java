package com.backend.specification;
import com.backend.models.quotation.Quotation;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class QuotationSpecification {

    public static Specification<Quotation> search(String keyword) {

        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String searchKeyword = keyword.trim().toLowerCase();

            var predicates = cb.or(
                    cb.like(
                            cb.lower(root.get("eventNature")),
                            "%" + searchKeyword + "%"
                    ),
                    cb.like(
                            cb.lower(root.get("eventVenue")),
                            "%" + searchKeyword + "%"
                    ),
                    cb.like(
                            cb.lower(root.get("customer").get("fullName")),
                            "%" + searchKeyword + "%"
                    ),
                    cb.like(
                            cb.lower(root.get("customer").get("phoneNumber")),
                            "%" + searchKeyword + "%"
                    )
            );

            try {
                LocalDate date = LocalDate.parse(keyword.trim());

                predicates = cb.or(
                        predicates,
                        cb.equal(root.get("eventDate"), date)
                );

            } catch (DateTimeParseException ignored) {
            }

            return predicates;
        };
    }

}