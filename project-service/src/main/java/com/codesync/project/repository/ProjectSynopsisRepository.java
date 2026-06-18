package com.codesync.project.repository;

import com.codesync.project.entity.ProjectSynopsis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectSynopsisRepository extends JpaRepository<ProjectSynopsis, Long> {
    Optional<ProjectSynopsis> findByProjectId(Long projectId);
}
