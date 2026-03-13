package com.sprint.project.findex.indexinfo.entity;

import com.sprint.project.findex.global.entity.DeletedStatus;
import com.sprint.project.findex.global.entity.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(name = "index_infos")
public class IndexInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column
  private Instant updatedAt;

  @Column(nullable = false)
  @Enumerated(value = EnumType.STRING)
  private SourceType sourceType;

  @Column(nullable = false, columnDefinition = "false")
  private boolean favorite;

  @Column(nullable = false)
  private String indexClassification;

  @Column(nullable = false)
  private String indexName;

  @Column(nullable = false)
  private Long employedItemsCount;

  @Column(nullable = false)
  private LocalDate basePointInTime;

  @Column(nullable = false)
  private Double baseIndex;

  @Column(nullable = false, columnDefinition = "ACTIVE")
  @Enumerated(value = EnumType.STRING)
  private DeletedStatus isDeleted;

  @Builder
  public IndexInfo(SourceType sourceType, boolean favorite,
      String indexClassification, String indexName, Long employedItemsCount,
      LocalDate basePointInTime, Double baseIndex, DeletedStatus isDeleted) {
    this.sourceType = sourceType;
    this.favorite = favorite;
    this.indexClassification = indexClassification;
    this.indexName = indexName;
    this.employedItemsCount = employedItemsCount;
    this.basePointInTime = basePointInTime;
    this.baseIndex = baseIndex;
    this.isDeleted = isDeleted;
  }
}
