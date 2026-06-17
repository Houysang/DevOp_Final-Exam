package com.example.demo.repository;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUniqueId(String uniqueId);

    List<Profile> findByProfileType(ProfileType profileType);

    List<Profile> findByLastNameContainingIgnoreCase(String lastName);

    List<Profile> findByFirstNameContainingIgnoreCase(String firstName);

    List<Profile> findByDepartmentContainingIgnoreCase(String department);

    boolean existsByUniqueId(String uniqueId);

    boolean existsByEmail(String email);

    @Query("SELECT p FROM Profile p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.uniqueId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.department) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Profile> searchProfiles(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(p) FROM Profile p WHERE p.profileType = :type AND p.department = :department")
    long countByProfileTypeAndDepartment(@Param("type") ProfileType type, @Param("department") String department);

    @Query("SELECT p FROM Profile p ORDER BY p.createdAt DESC")
    List<Profile> findAllByOrderByCreatedAtDesc();
}