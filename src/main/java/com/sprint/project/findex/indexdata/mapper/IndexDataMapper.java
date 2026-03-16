package com.sprint.project.findex.indexdata.mapper;

import com.sprint.project.findex.indexdata.dto.IndexDataDto;
import com.sprint.project.findex.indexdata.entity.IndexData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IndexDataMapper {
  IndexDataDto toDto(IndexData indexData);
}
