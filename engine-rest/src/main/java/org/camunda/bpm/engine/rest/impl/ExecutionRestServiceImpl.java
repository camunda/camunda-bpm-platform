/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.ExecutionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionQueryDto;
import org.camunda.bpm.engine.rest.sub.runtime.ExecutionResource;
import org.camunda.bpm.engine.rest.sub.runtime.impl.ExecutionResourceImpl;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class ExecutionRestServiceImpl extends AbstractRestProcessEngineAware implements ExecutionRestService {

  public ExecutionRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public ExecutionResource getExecution(String executionId) {
    return new ExecutionResourceImpl(getProcessEngine(), executionId, getObjectMapper());
  }

  @Override
  public List<ExecutionDto> getExecutions(UriInfo uriInfo, Integer firstResult,
      Integer maxResults) {
    ExecutionQueryDto queryDto = new ExecutionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryExecutions(queryDto, firstResult, maxResults);
  }

  @Override
  public List<ExecutionDto> queryExecutions(
      ExecutionQueryDto queryDto, Integer firstResult, Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
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
    ExecutionQueryDto queryDto = new ExecutionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryExecutionsCount(queryDto);
  }

  @Override
  public CountResultDto queryExecutionsCount(ExecutionQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    ExecutionQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }
}
