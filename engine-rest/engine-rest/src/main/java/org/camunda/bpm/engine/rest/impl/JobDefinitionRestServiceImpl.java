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
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.rest.JobDefinitionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.management.JobDefinitionDto;
import org.camunda.bpm.engine.rest.dto.management.JobDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.management.JobDefinitionSuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.management.JobDefinitionResource;
import org.camunda.bpm.engine.rest.sub.management.JobDefinitionResourceImpl;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author roman.smirnov
 */
public class JobDefinitionRestServiceImpl extends AbstractRestProcessEngineAware implements JobDefinitionRestService {

  public JobDefinitionRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  public JobDefinitionResource getJobDefinition(String jobDefinitionId) {
    return new JobDefinitionResourceImpl(getProcessEngine(), jobDefinitionId);
  }

  public List<JobDefinitionDto> getJobDefinitions(UriInfo uriInfo, Integer firstResult,
      Integer maxResults) {
    JobDefinitionQueryDto queryDto = new JobDefinitionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryJobDefinitions(queryDto, firstResult, maxResults);

  }

  public CountResultDto getJobDefinitionsCount(UriInfo uriInfo) {
    JobDefinitionQueryDto queryDto = new JobDefinitionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryJobDefinitionsCount(queryDto);
  }

  public List<JobDefinitionDto> queryJobDefinitions(JobDefinitionQueryDto queryDto, Integer firstResult, Integer maxResults) {
    queryDto.setObjectMapper(getObjectMapper());
    JobDefinitionQuery query = queryDto.toQuery(getProcessEngine());

    List<JobDefinition> matchingJobDefinitions;
    if (firstResult != null || maxResults != null) {
      matchingJobDefinitions = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingJobDefinitions = query.list();
    }

    List<JobDefinitionDto> jobDefinitionResults = new ArrayList<JobDefinitionDto>();
    for (JobDefinition jobDefinition : matchingJobDefinitions) {
      JobDefinitionDto result = JobDefinitionDto.fromJobDefinition(jobDefinition);
      jobDefinitionResults.add(result);
    }

    return jobDefinitionResults;
  }

  public CountResultDto queryJobDefinitionsCount(JobDefinitionQueryDto queryDto) {
    queryDto.setObjectMapper(getObjectMapper());
    JobDefinitionQuery query = queryDto.toQuery(getProcessEngine());

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  private List<JobDefinition> executePaginatedQuery(JobDefinitionQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  public void updateSuspensionState(JobDefinitionSuspensionStateDto dto) {
    if (dto.getJobDefinitionId() != null) {
      String message = "Either processDefinitionId or processDefinitionKey can be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    try {
      dto.updateSuspensionState(getProcessEngine());

    } catch (IllegalArgumentException e) {
      String message = String.format("Could not update the suspension state of Job Definitions due to: %s", e.getMessage()) ;
      throw new InvalidRequestException(Status.BAD_REQUEST, e, message);
    }
  }

}
