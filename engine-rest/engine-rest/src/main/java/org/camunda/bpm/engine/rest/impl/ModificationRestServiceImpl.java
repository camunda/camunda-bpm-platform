package org.camunda.bpm.engine.rest.impl;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.rest.ModificationRestService;
import org.camunda.bpm.engine.rest.dto.ModificationDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.ModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ModificationRestServiceImpl extends AbstractRestProcessEngineAware implements ModificationRestService {

  public ModificationRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public void executeModification(ModificationDto modificationExecutionDto) {
     try {
       createModificationBuilder(modificationExecutionDto).execute();
     } catch (BadUserRequestException e) {
       throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
     }
  }

  @Override
  public BatchDto executeModificationAsync(ModificationDto modificationExecutionDto) {
    Batch batch = null;
    try {
      batch = createModificationBuilder(modificationExecutionDto).executeAsync();
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
    return BatchDto.fromBatch(batch);
  }

  private ModificationBuilder createModificationBuilder(ModificationDto dto) {
    ModificationBuilder builder = getProcessEngine().getRuntimeService().createModification(dto.getProcessDefinitionId());

    if (dto.getInstructions() != null && !dto.getInstructions().isEmpty()) {
      dto.applyTo(builder, getProcessEngine(), objectMapper);
    }

    List<String> processInstanceIds = dto.getProcessInstanceIds();
    builder.processInstanceIds(processInstanceIds);

    ProcessInstanceQueryDto processInstanceQueryDto = dto.getProcessInstanceQuery();
    if (processInstanceQueryDto != null) {
      ProcessInstanceQuery processInstanceQuery = processInstanceQueryDto.toQuery(getProcessEngine());
      builder.processInstanceQuery(processInstanceQuery);
    }

    if (dto.isSkipCustomListeners()) {
      builder.skipCustomListeners();
    }

    if (dto.isSkipIoMappings()) {
      builder.skipIoMappings();
    }

    return builder;
  }
  
}
