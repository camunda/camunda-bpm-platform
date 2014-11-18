package org.camunda.bpm.engine.rest.mapper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.camunda.bpm.engine.rest.dto.history.HistoricFormFieldDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableUpdateDto;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS
)
@JsonSubTypes({
    @Type(value = HistoricFormFieldDto.class),
    @Type(value = HistoricVariableUpdateDto.class)
})
public abstract class PolymorphicHistoricDetailDtoMixIn {
}
