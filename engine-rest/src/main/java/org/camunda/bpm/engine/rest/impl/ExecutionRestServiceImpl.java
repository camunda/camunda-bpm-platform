package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.ExecutionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionQueryDto;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;

public class ExecutionRestServiceImpl extends AbstractRestProcessEngineAware implements ExecutionRestService {

  public ExecutionRestServiceImpl() {
    super();
  }

  public ExecutionRestServiceImpl(String engineName) {
    super(engineName);
  }
  
  @Override
  public List<ExecutionDto> getExecutions(UriInfo uriInfo, Integer firstResult,
      Integer maxResults) {
    ExecutionQueryDto queryDto = new ExecutionQueryDto(uriInfo.getQueryParameters());
    return queryExecutions(queryDto, firstResult, maxResults);
  }

  @Override
  public List<ExecutionDto> queryExecutions(
      ExecutionQueryDto queryDto, Integer firstResult, Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    ExecutionQuery query = queryDto.toQuery(engine);
    
    List<Execution> matchingExecutions;
    if (firstResult != null || maxResults != null) {
      matchingExecutions = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingExecutions = query.list();
    }
    
    List<ExecutionDto> executionResults = new ArrayList<ExecutionDto>();
    for (Execution execution : matchingExecutions) {
      ExecutionDto resultExecution = ExecutionDto.fromExecution(execution);
      executionResults.add(resultExecution);
    }
    return executionResults;
  }
  
  private List<Execution> executePaginatedQuery(ExecutionQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults); 
  }

  @Override
  public CountResultDto getExecutionsCount(UriInfo uriInfo) {
    ExecutionQueryDto queryDto = new ExecutionQueryDto(uriInfo.getQueryParameters());
    return queryExecutionsCount(queryDto);
  }

  @Override
  public CountResultDto queryExecutionsCount(ExecutionQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    ExecutionQuery query = queryDto.toQuery(engine);
    
    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    
    return result;
  }

  
}
