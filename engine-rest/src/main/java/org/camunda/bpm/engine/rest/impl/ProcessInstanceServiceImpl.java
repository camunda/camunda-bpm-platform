package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.ProcessInstanceService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableListDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.spi.AbstractProcessEngineAware;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

public class ProcessInstanceServiceImpl extends AbstractProcessEngineAware implements
    ProcessInstanceService {

  @Override
  public List<ProcessInstanceDto> getProcessInstances(
      ProcessInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ProcessInstanceQuery query;
    try {
      query = queryDto.toQuery(runtimeService);
    } catch (InvalidRequestException e) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
    }
    
    List<ProcessInstance> matchingInstances;
    if (firstResult != null || maxResults != null) {
      matchingInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingInstances = query.list();
    }
    
    List<ProcessInstanceDto> instanceResults = new ArrayList<ProcessInstanceDto>();
    for (ProcessInstance instance : matchingInstances) {
      ProcessInstanceDto resultInstance = ProcessInstanceDto.fromProcessInstance(instance);
      instanceResults.add(resultInstance);
    }
    return instanceResults;
  }

  private List<ProcessInstance> executePaginatedQuery(ProcessInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults); 
  }

  @Override
  public List<ProcessInstanceDto> queryProcessInstances(
      ProcessInstanceQueryDto query, Integer firstResult, Integer maxResults) {
    return getProcessInstances(query, firstResult, maxResults);
  }

  @Override
  public CountResultDto getProcessInstancesCount(
      ProcessInstanceQueryDto queryDto) {
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ProcessInstanceQuery query;
    try {
      query = queryDto.toQuery(runtimeService);
    } catch (InvalidRequestException e) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
    }
    
    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    
    return result;
  }

  @Override
  public CountResultDto queryProcessInstancesCount(ProcessInstanceQueryDto queryDto) {
    return getProcessInstancesCount(queryDto);
  }

  @Override
  public VariableListDto getVariables(@PathParam("id") String processInstanceId) {
    List<VariableValueDto> values = new ArrayList<VariableValueDto>();

    for (Map.Entry<String, Object> entry : processEngine.getRuntimeService().getVariables(processInstanceId).entrySet()) {
      values.add(new VariableValueDto(entry.getKey(), entry.getValue(), entry.getValue().getClass().getSimpleName()));
    }

    return new VariableListDto(values);
  }

}
