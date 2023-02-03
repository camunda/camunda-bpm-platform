/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.camunda.bpm.engine.rest.util.QueryUtil;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;

public class CaseExecutionRestServiceImpl extends AbstractRestProcessEngineAware implements CaseExecutionRestService {

  public CaseExecutionRestServiceImpl(String engineName, final ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public CaseExecutionResource getCaseExecution(String caseExecutionId) {
    return new CaseExecutionResourceImpl(getProcessEngine(), caseExecutionId, getObjectMapper());
  }

  @Override
  public List<CaseExecutionDto> getCaseExecutions(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    CaseExecutionQueryDto queryDto = new CaseExecutionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryCaseExecutions(queryDto, firstResult, maxResults);
  }

  @Override
  public List<CaseExecutionDto> queryCaseExecutions(CaseExecutionQueryDto queryDto, Integer firstResult, Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    CaseExecutionQuery query = queryDto.toQuery(engine);

    List<CaseExecution> matchingExecutions = QueryUtil.list(query, firstResult, maxResults);

    List<CaseExecutionDto> executionResults = new ArrayList<CaseExecutionDto>();
    for (CaseExecution execution : matchingExecutions) {
      CaseExecutionDto resultExecution = CaseExecutionDto.fromCaseExecution(execution);
      executionResults.add(resultExecution);
    }
    return executionResults;
  }

  @Override
  public CountResultDto getCaseExecutionsCount(UriInfo uriInfo) {
    CaseExecutionQueryDto queryDto = new CaseExecutionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryCaseExecutionsCount(queryDto);
  }

  @Override
  public CountResultDto queryCaseExecutionsCount(CaseExecutionQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    CaseExecutionQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

}
