package com.codesync.version.repository;

import com.codesync.version.model.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, String> {
    List<Snapshot> findByFileIdOrderByCreatedAtDesc(Long fileId);
    List<Snapshot> findByFileIdAndBranchOrderByCreatedAtDesc(Long fileId, String branch);
}
