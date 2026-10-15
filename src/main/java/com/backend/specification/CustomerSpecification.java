package com.backend.specification;

import com.backend.models.Customer;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {

    public static Specification<Customer> searchCustomer(String keyword) {

        return (root, query, criteriaBuilder) -> {

            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchKeyword = "%" + keyword.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fullName")),
                            searchKeyword
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("email")),
                            searchKeyword
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("phoneNumber")),
                            searchKeyword
                    )
            );
        };
    }
}
