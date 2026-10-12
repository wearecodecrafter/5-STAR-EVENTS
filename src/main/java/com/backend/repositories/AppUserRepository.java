package com.backend.repositories;

import com.backend.models.AppUser;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    @Query(nativeQuery = true, value = "SELECT * FROM app_user WHERE username = :username")
    Optional<AppUser> getMyUserByUsername(String username);

    Optional<AppUser> getAppUserByEmployeeCode(String employeeCode);
    List<AppUser> getAllByRoleRoleName(String roleName);
    List<AppUser> getAppUsersByRoleRoleName(String roleName);
    List<AppUser> findAll(Specification<AppUser> spec);

    @Modifying
    @Transactional
    @Query(value = "UPDATE app_user SET fullName = :fullName, email = :email, contact = :contact WHERE id = :id", nativeQuery = true)
    void updateEmpUser(@Param("fullName") String firstName,
                       @Param("email") String middleName,
                       @Param("contact") String contact,
                       @Param("id") UUID id);

    @Query("SELECT COUNT(u) FROM AppUser u WHERE u.role IS NULL OR UPPER(u.role.roleName) <> 'SUPER_ADMIN'")
    Integer countAllExceptSuperAdmin();
}
