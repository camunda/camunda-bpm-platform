package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.CaseExecutionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.CaseExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.CaseExecutionQueryDto;
import org.camunda.bpm.engine.rest.sub.runtime.CaseExecutionResource;
import org.camunda.bpm.engine.rest.sub.runtime.impl.CaseExecutionResourceImpl;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;

public class CaseExecutionRestServiceImpl extends AbstractRestProcessEngineAware implements CaseExecutionRestService {

  public CaseExecutionRestServiceImpl() {
    super();
  }

  public CaseExecutionRestServiceImpl(String engineName) {
    super(engineName);
  }

  public CaseExecutionResource getCaseExecution(String caseExecutionId) {
    return new CaseExecutionResourceImpl(getProcessEngine(), caseExecutionId);
  }

  public List<CaseExecutionDto> getCaseExecutions(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    CaseExecutionQueryDto queryDto = new CaseExecutionQueryDto(uriInfo.getQueryParameters());
    return queryCaseExecutions(queryDto, firstResult, maxResults);
  }

  public List<CaseExecutionDto> queryCaseExecutions(CaseExecutionQueryDto queryDto, Integer firstResult, Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    CaseExecutionQuery query = queryDto.toQuery(engine);

    List<CaseExecution> matchingExecutions;
    if (firstResult != null || maxResults != null) {
      matchingExecutions = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingExecutions = query.list();
    }

    List<CaseExecutionDto> executionResults = new ArrayList<CaseExecutionDto>();
    for (CaseExecution execution : matchingExecutions) {
      CaseExecutionDto resultExecution = CaseExecutionDto.fromCaseExecution(execution);
      executionResults.add(resultExecution);
    }
    return executionResults;
  }

  private List<CaseExecution> executePaginatedQuery(CaseExecutionQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  public CountResultDto getCaseExecutionsCount(UriInfo uriInfo) {
    CaseExecutionQueryDto queryDto = new CaseExecutionQueryDto(uriInfo.getQueryParameters());
    return queryCaseExecutionsCount(queryDto);
  }

  public CountResultDto queryCaseExecutionsCount(CaseExecutionQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    CaseExecutionQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

}
