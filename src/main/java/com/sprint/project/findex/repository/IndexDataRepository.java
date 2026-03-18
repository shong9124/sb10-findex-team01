package com.sprint.project.findex.repository;

import com.sprint.project.findex.entity.IndexData;
import com.sprint.project.findex.entity.IndexInfo;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>,
    IndexDataQDSLRepository {
  boolean existsByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);
}
