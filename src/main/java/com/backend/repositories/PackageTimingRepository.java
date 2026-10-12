package com.backend.repositories;

import com.backend.models.PackageTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PackageTimingRepository extends JpaRepository<PackageTime, UUID> {
    Optional<PackageTime> findByName(String name);

    boolean existsByName(String name);
}
