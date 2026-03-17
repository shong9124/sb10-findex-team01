package com.sprint.project.findex.dto.indexdata;

import java.time.LocalDate;

public record CursorPageIndexDataRequest(
    Long indexInfoId,
    LocalDate startTime,
    LocalDate endDate,
    Long idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    int size
) {

  public CursorPageIndexDataRequest {
    if (size <= 0) {
      size = 10;
    }

    if (sortField == null || sortField.isBlank()) {
      sortField = "baseDate";
    }

    if (sortDirection == null || sortDirection.isBlank()) {
      sortDirection = "desc";
    }
  }
}
