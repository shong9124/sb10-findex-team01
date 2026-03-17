package com.sprint.project.findex.service;

import com.sprint.project.findex.dto.SyncJobDto;
import com.sprint.project.findex.dto.openapi.StockMarketIndexResponse;
import com.sprint.project.findex.dto.openapi.StockMarketIndexResponse.StockIndexDto;
import com.sprint.project.findex.entity.DeletedStatus;
import com.sprint.project.findex.entity.IndexInfo;
import com.sprint.project.findex.entity.SourceType;
import com.sprint.project.findex.entity.SyncJob;
import com.sprint.project.findex.global.entity.JobType;
import com.sprint.project.findex.global.entity.ResultType;
import com.sprint.project.findex.global.exception.BusinessLogicException;
import com.sprint.project.findex.global.exception.ExceptionCode;
import com.sprint.project.findex.mapper.SyncJobMapper;
import com.sprint.project.findex.repository.IndexInfoRepository;
import com.sprint.project.findex.repository.SyncJobRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional
public class SyncJobService {

  private final IndexInfoRepository indexInfoRepository;
  private final SyncJobRepository syncJobRepository;

  @Qualifier("openapi")
  private final WebClient openapi;
  private final SyncJobMapper syncJobMapper;


  // 가장 최신의 지수 정보를 로드해 저장합니다.
  public List<SyncJobDto> syncIndexInfos(HttpServletRequest request) {

    List<SyncJobDto> syncJobDtos = new ArrayList<>(); // 컨트롤러 응답
    String requestIpAddr = request.getRemoteAddr();
    int pageNo = 1;

    try {
      while (true) {
        // 최신 지수 정보 요청
        StockMarketIndexResponse apiResponse = getOpenApiIndexInfo(pageNo, getLastWeekday());

        if (apiResponse.response().header().resultCode().equals("00")
            && !apiResponse.response().body().items().item()
            .isEmpty()) {

          List<StockIndexDto> stockIndexDtoList = apiResponse.response().body().items().item();

          // 지수 정보 갱신
          for (StockIndexDto stockIndexDto : stockIndexDtoList) {
            IndexInfo resolvedIndexInfo =
                indexInfoRepository.findByIndexClassificationAndIndexName(
                        stockIndexDto.indexClassification(),
                        stockIndexDto.indexName()
                    )
                    .map(idxInfo -> {
                      idxInfo.updateByOpenAPI(stockIndexDto);
                      return idxInfo;
                    })
                    .orElseGet(() ->
                        indexInfoRepository.save(
                            IndexInfo.builder()
                                .indexClassification(stockIndexDto.indexClassification())
                                .indexName(stockIndexDto.indexName())
                                .employedItemsCount(stockIndexDto.employedItemsCount())
                                .basePointInTime(stockIndexDto.basePointInTime())
                                .baseIndex(stockIndexDto.baseIndex())
                                .sourceType(SourceType.OPEN_API)
                                .favorite(false)
                                .isDeleted(DeletedStatus.ACTIVE)
                                .build()
                        )
                    );

            // 연동 기록 저장
            SyncJob syncJob = new SyncJob(resolvedIndexInfo, JobType.INDEX_INFO, null,
                requestIpAddr,
                ResultType.SUCCESS);
            syncJobRepository.save(syncJob);
            syncJobDtos.add(syncJobMapper.toDto(syncJob));
          }

          pageNo++;

        } else {
          break;
        }
      }
    } catch (Exception e) {
      throw new BusinessLogicException(ExceptionCode.OPEN_API_REQUEST_FAILED, e.getMessage());
    }

    return syncJobDtos;
  }

  private StockMarketIndexResponse getOpenApiIndexInfo(int pageNo, LocalDate baseDate) {
    return openapi.get()
        .uri(urlBuilder -> urlBuilder
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", 50)
            .queryParam("basDt", baseDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
            .build()
        )
        .retrieve()
        .bodyToMono(StockMarketIndexResponse.class)
        .block(Duration.ofSeconds(5));
  }

  // 오늘 이전의 가장 마지막 평일 구하기
  private LocalDate getLastWeekday() {
    LocalDate yesterday = LocalDate.now().minusDays(1);

    LocalDate lastWeekday = yesterday;
    if (yesterday.getDayOfWeek() == DayOfWeek.SATURDAY) {
      lastWeekday = yesterday.minusDays(1);
    } else if (yesterday.getDayOfWeek() == DayOfWeek.SUNDAY) {
      lastWeekday = yesterday.minusDays(2);
    }

    return lastWeekday;
  }
}
