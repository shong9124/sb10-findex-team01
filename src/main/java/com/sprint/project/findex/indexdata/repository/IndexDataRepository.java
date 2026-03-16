package com.sprint.project.findex.indexdata.repository;

import com.sprint.project.findex.global.entity.DeletedStatus;
import com.sprint.project.findex.indexdata.entity.IndexData;
import com.sprint.project.findex.indexinfo.entity.IndexInfo;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {
  boolean existsByIndexInfoAndBaseDateAndIsDeleted(IndexInfo indexInfo, LocalDate baseDate, DeletedStatus deletedStatus);
}
