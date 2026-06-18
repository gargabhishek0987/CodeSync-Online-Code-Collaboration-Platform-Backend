package com.codesync.project.repository;

import com.codesync.project.entity.Project;
import com.codesync.project.entity.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(String ownerId);
    List<Project> findByVisibility(Visibility visibility);
    List<Project> findByNameContainingIgnoreCaseAndVisibility(String name, Visibility visibility);
    List<Project> findByLanguageIgnoreCaseAndVisibility(String language, Visibility visibility);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Project p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.ownerId) LIKE LOWER(CONCAT('%', :query, '%'))) AND p.visibility = :visibility")
    List<Project> searchPublicProjects(@org.springframework.data.repository.query.Param("query") String query, @org.springframework.data.repository.query.Param("visibility") Visibility visibility);
}
