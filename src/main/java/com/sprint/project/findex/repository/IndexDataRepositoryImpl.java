package com.sprint.project.findex.repository;

import static com.sprint.project.findex.entity.QIndexData.indexData;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.project.findex.dto.indexdata.CursorPageIndexDataRequest;
import com.sprint.project.findex.entity.DeletedStatus;
import com.sprint.project.findex.entity.IndexData;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class IndexDataRepositoryImpl implements IndexDataRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<IndexData> findCursorPage(CursorPageIndexDataRequest request) {
    List<IndexData> content = queryFactory
        .selectFrom(indexData)
        .where(
            indexData.isDeleted.eq(DeletedStatus.ACTIVE),
            eqIndexInfoId(request.indexInfoId()),
            betweenDates(request.startTime(), request.endDate()),
            cursorCondition(request)
        )
        .orderBy(
            sortIndexDataList(request.sortField(), request.sortDirection()),
            indexData.id.desc()
        )
        .limit(request.size() + 1)
        .fetch();

    boolean hasNext = false;
    if (content.size() > request.size()) {
      content.remove(request.size());
      hasNext = true;
    }
    return new SliceImpl<>(content, PageRequest.of(0, request.size()), hasNext);
  }

  private BooleanExpression eqIndexInfoId(Long indexInfoId) {
    return indexInfoId != null ? indexData.indexInfo.id.eq(indexInfoId) : null;
  }

  private BooleanExpression betweenDates(LocalDate startTime, LocalDate endDate) {
    if (startTime != null && endDate != null) {
      return indexData.baseDate.between(startTime, endDate);
    }
    if (startTime != null) {
      return indexData.baseDate.goe(startTime);
    }
    if (endDate != null) {
      return indexData.baseDate.loe(endDate);
    }
    return null;
  }

  private BooleanExpression cursorCondition(CursorPageIndexDataRequest request) {
    if (request.cursor() == null || request.idAfter() == null) {
      return null;
    }

    String sortDirection = request.sortDirection();
    Long idAfter = request.idAfter();
    String cursor = request.cursor();

    return switch (request.sortField()) {
      case "marketPrice" -> compareCursor(indexData.marketPrice, Double.valueOf(cursor), sortDirection, idAfter);
      case "closingPrice" -> compareCursor(indexData.closingPrice, Double.valueOf(cursor), sortDirection, idAfter);
      case "highPrice" -> compareCursor(indexData.highPrice, Double.valueOf(cursor), sortDirection, idAfter);
      case "lowPrice" -> compareCursor(indexData.lowPrice, Double.valueOf(cursor), sortDirection, idAfter);
      case "versus" -> compareCursor(indexData.versus, Double.valueOf(cursor), sortDirection, idAfter);
      case "fluctuationRate" -> compareCursor(indexData.fluctuationRate, Double.valueOf(cursor), sortDirection, idAfter);
      case "tradingQuantity" -> compareCursor(indexData.tradingQuantity, Long.valueOf(cursor), sortDirection, idAfter);
      case "tradingPrice" -> compareCursor(indexData.tradingPrice, new BigInteger(cursor), sortDirection, idAfter);
      case "marketTotalAmount" -> compareCursor(indexData.marketTotalAmount, new BigInteger(cursor), sortDirection, idAfter);
      default -> compareCursor(indexData.baseDate, LocalDate.parse(cursor), sortDirection, idAfter);
    };

  }

  private <T extends Comparable<?>> BooleanExpression compareCursor(ComparableExpression<T> path,
      T cursorValue, String sortDirection, Long idAfter) {
    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

    if (isAsc) {
      return path.gt(cursorValue)
          .or(path.eq(cursorValue)
              .and(indexData.id.lt(idAfter)));
    } else {
      return path.lt(cursorValue)
          .or(path.eq(cursorValue)
              .and(indexData.id.lt(idAfter)));
    }
  }

  private <T extends Number & Comparable<?>> BooleanExpression compareCursor(NumberExpression<T> path,
      T cursorValue, String sortDirection, Long idAfter) {
    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

    if (isAsc) {
      return path.gt(cursorValue)
          .or(path.eq(cursorValue)
              .and(indexData.id.lt(idAfter)));
    } else {
      return path.lt(cursorValue)
          .or(path.eq(cursorValue)
              .and(indexData.id.lt(idAfter)));
    }
  }

  private OrderSpecifier<?> sortIndexDataList(String sortField, String sortDirection) {
    Order direction = "asc".equalsIgnoreCase(sortDirection) ? Order.ASC : Order.DESC;

    return switch (sortField) {
      case "marketPrice" -> new OrderSpecifier<>(direction, indexData.marketPrice);
      case "closingPrice" -> new OrderSpecifier<>(direction, indexData.closingPrice);
      case "highPrice" -> new OrderSpecifier<>(direction, indexData.highPrice);
      case "lowPrice" -> new OrderSpecifier<>(direction, indexData.lowPrice);
      case "versus" -> new OrderSpecifier<>(direction, indexData.versus);
      case "fluctuationRate" -> new OrderSpecifier<>(direction, indexData.fluctuationRate);
      case "tradingQuantity" -> new OrderSpecifier<>(direction, indexData.tradingQuantity);
      case "tradingPrice" -> new OrderSpecifier<>(direction, indexData.tradingPrice);
      case "marketTotalAmount" -> new OrderSpecifier<>(direction, indexData.marketTotalAmount);
      default -> new OrderSpecifier<>(direction, indexData.baseDate);
    };
  }
}
