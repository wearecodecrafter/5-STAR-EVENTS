package com.backend.repositories;

import com.backend.models.PackageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PackageCategoryRepository extends JpaRepository<PackageCategory, UUID> {

    Optional<PackageCategory> findByName(String name);

    boolean existsByName(String name);
}
