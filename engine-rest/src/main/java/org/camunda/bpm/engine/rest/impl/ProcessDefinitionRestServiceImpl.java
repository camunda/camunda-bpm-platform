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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.ProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionSuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.repository.ProcessDefinitionResource;
import org.camunda.bpm.engine.rest.sub.repository.ProcessDefinitionResourceImpl;

public class ProcessDefinitionRestServiceImpl extends AbstractRestProcessEngineAware implements ProcessDefinitionRestService {

  public ProcessDefinitionRestServiceImpl() {
    super();
  }

	public ProcessDefinitionRestServiceImpl(String engineName) {
    super(engineName);
  }

	@Override
	public ProcessDefinitionResource getProcessDefinitionByKey(String processDefinitionKey) {
    if(processDefinitionKey == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Query parameter 'processDefinitionKey' cannot be null");
    }

    ProcessDefinitionQuery processDefinitionQuery = getProcessEngine().getRepositoryService().createProcessDefinitionQuery();
    processDefinitionQuery = processDefinitionQuery.processDefinitionKey(processDefinitionKey);

    ProcessDefinition processDefinition = processDefinitionQuery.latestVersion().singleResult();

    if (processDefinition == null) {
      String errorMessage = String.format("No matching process definition with key: %s ", processDefinitionKey);
      throw new RestException(Status.NOT_FOUND, errorMessage);
    }

    ProcessDefinitionResource processDefinitionResource = getProcessDefinitionById(processDefinition.getId());

    return processDefinitionResource;
  }

  @Override
  public ProcessDefinitionResource getProcessDefinitionById(
      String processDefinitionId) {
    return new ProcessDefinitionResourceImpl(getProcessEngine(), processDefinitionId, relativeRootResourcePath);
  }

  @Override
	public List<ProcessDefinitionDto> getProcessDefinitions(UriInfo uriInfo,
	    Integer firstResult, Integer maxResults) {
    ProcessDefinitionQueryDto queryDto = new ProcessDefinitionQueryDto(uriInfo.getQueryParameters());
	  List<ProcessDefinitionDto> definitions = new ArrayList<ProcessDefinitionDto>();

	  ProcessEngine engine = getProcessEngine();
	  ProcessDefinitionQuery query = queryDto.toQuery(engine);

	  List<ProcessDefinition> matchingDefinitions = null;

	  if (firstResult != null || maxResults != null) {
	    matchingDefinitions = executePaginatedQuery(query, firstResult, maxResults);
	  } else {
	    matchingDefinitions = query.list();
	  }

	  for (ProcessDefinition definition : matchingDefinitions) {
	    ProcessDefinitionDto def = ProcessDefinitionDto.fromProcessDefinition(definition);
	    definitions.add(def);
	  }
	  return definitions;
	}

	private List<ProcessDefinition> executePaginatedQuery(ProcessDefinitionQuery query, Integer firstResult, Integer maxResults) {
	  if (firstResult == null) {
	    firstResult = 0;
	  }
	  if (maxResults == null) {
	    maxResults = Integer.MAX_VALUE;
	  }
	  return query.listPage(firstResult, maxResults);
	}

	@Override
  public CountResultDto getProcessDefinitionsCount(UriInfo uriInfo) {
	  ProcessDefinitionQueryDto queryDto = new ProcessDefinitionQueryDto(uriInfo.getQueryParameters());

	  ProcessEngine engine = getProcessEngine();
    ProcessDefinitionQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    return result;
  }


  @Override
  public List<StatisticsResultDto> getStatistics(Boolean includeFailedJobs, Boolean includeIncidents, String includeIncidentsForType) {
    if (includeIncidents != null && includeIncidentsForType != null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Only one of the query parameter includeIncidents or includeIncidentsForType can be set.");
    }

    ManagementService mgmtService = getProcessEngine().getManagementService();
    ProcessDefinitionStatisticsQuery query = mgmtService.createProcessDefinitionStatisticsQuery();

    if (includeFailedJobs != null && includeFailedJobs) {
      query.includeFailedJobs();
    }

    if (includeIncidents != null && includeIncidents) {
      query.includeIncidents();
    } else if (includeIncidentsForType != null) {
      query.includeIncidentsForType(includeIncidentsForType);
    }

    List<ProcessDefinitionStatistics> queryResults = query.list();

    List<StatisticsResultDto> results = new ArrayList<StatisticsResultDto>();
    for (ProcessDefinitionStatistics queryResult : queryResults) {
      StatisticsResultDto dto = ProcessDefinitionStatisticsResultDto.fromProcessDefinitionStatistics(queryResult);
      results.add(dto);
    }

    return results;
  }

  public void updateSuspensionState(ProcessDefinitionSuspensionStateDto dto) {
    if (dto.getProcessDefinitionId() != null) {
      String message = "Only processDefinitionKey can be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    try {
      dto.updateSuspensionState(getProcessEngine());

    } catch (IllegalArgumentException e) {
      String message = String.format("Could not update the suspension state of Process Definitions due to: %s", e.getMessage()) ;
      throw new InvalidRequestException(Status.BAD_REQUEST, e, message);
    }
  }

}
