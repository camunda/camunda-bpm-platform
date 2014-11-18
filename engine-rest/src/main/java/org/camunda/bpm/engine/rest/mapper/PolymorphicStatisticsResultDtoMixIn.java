package org.camunda.bpm.engine.rest.mapper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.camunda.bpm.engine.rest.dto.repository.ActivityStatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ActivityStatisticsResultDto.class),
    @JsonSubTypes.Type(value = ProcessDefinitionStatisticsResultDto.class)
})
public class PolymorphicStatisticsResultDtoMixIn {
}