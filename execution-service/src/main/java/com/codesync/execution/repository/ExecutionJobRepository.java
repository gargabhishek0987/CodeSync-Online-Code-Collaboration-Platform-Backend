package com.codesync.execution.repository;

import com.codesync.execution.model.ExecutionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionJobRepository extends JpaRepository<ExecutionJob, String> {
}
