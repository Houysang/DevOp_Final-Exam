package com.example.demo.repository;

import com.example.demo.model.ProfileType;
import com.example.demo.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByName(String name);

    List<Template> findByProfileType(ProfileType profileType);

    Optional<Template> findByIsDefaultTrue();

    List<Template> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    @Query("SELECT t FROM Template t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Template> searchTemplates(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Template t ORDER BY t.createdAt DESC")
    List<Template> findAllByOrderByCreatedAtDesc();
}