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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.repository.DeleteProcessDefinitionsBuilder;
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
import org.camunda.bpm.engine.rest.sub.repository.impl.ProcessDefinitionResourceImpl;
import org.camunda.bpm.engine.rest.util.QueryUtil;

public class ProcessDefinitionRestServiceImpl extends AbstractRestProcessEngineAware implements ProcessDefinitionRestService {

  public ProcessDefinitionRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public ProcessDefinitionResource getProcessDefinitionByKey(String processDefinitionKey) {

    ProcessDefinition processDefinition = getProcessEngine()
        .getRepositoryService()
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .withoutTenantId()
        .latestVersion()
        .singleResult();

    if(processDefinition == null){
      String errorMessage = String.format("No matching process definition with key: %s and no tenant-id", processDefinitionKey);
      throw new RestException(Status.NOT_FOUND, errorMessage);

    } else {
      return getProcessDefinitionById(processDefinition.getId());
    }
  }

  @Override
  public ProcessDefinitionResource getProcessDefinitionByKeyAndTenantId(String processDefinitionKey, String tenantId) {

    ProcessDefinition processDefinition = getProcessEngine()
        .getRepositoryService()
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .tenantIdIn(tenantId)
        .latestVersion()
        .singleResult();

    if (processDefinition == null) {
      String errorMessage = String.format("No matching process definition with key: %s and tenant-id: %s", processDefinitionKey, tenantId);
      throw new RestException(Status.NOT_FOUND, errorMessage);

    } else {
      return getProcessDefinitionById(processDefinition.getId());
    }
  }

  @Override
  public ProcessDefinitionResource getProcessDefinitionById(
      String processDefinitionId) {
    return new ProcessDefinitionResourceImpl(getProcessEngine(), processDefinitionId, relativeRootResourcePath, getObjectMapper());
  }

  @Override
  public List<ProcessDefinitionDto> getProcessDefinitions(UriInfo uriInfo,
      Integer firstResult, Integer maxResults) {
    ProcessDefinitionQueryDto queryDto = new ProcessDefinitionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    List<ProcessDefinitionDto> definitions = new ArrayList<>();

    ProcessEngine engine = getProcessEngine();
    ProcessDefinitionQuery query = queryDto.toQuery(engine);

    List<ProcessDefinition> matchingDefinitions = QueryUtil.list(query, firstResult, maxResults);

    for (ProcessDefinition definition : matchingDefinitions) {
      ProcessDefinitionDto def = ProcessDefinitionDto.fromProcessDefinition(definition);
      definitions.add(def);
    }
    return definitions;
  }

  @Override
  public CountResultDto getProcessDefinitionsCount(UriInfo uriInfo) {
    ProcessDefinitionQueryDto queryDto = new ProcessDefinitionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());

    ProcessEngine engine = getProcessEngine();
    ProcessDefinitionQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    return result;
  }


  @Override
  public List<StatisticsResultDto> getStatistics(Boolean includeFailedJobs, Boolean includeRootIncidents, Boolean includeIncidents, String includeIncidentsForType) {
    if (includeIncidents != null && includeIncidentsForType != null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Only one of the query parameter includeIncidents or includeIncidentsForType can be set.");
    }

    if (includeIncidents != null && includeRootIncidents != null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Only one of the query parameter includeIncidents or includeRootIncidents can be set.");
    }

    if (includeRootIncidents != null && includeIncidentsForType != null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Only one of the query parameter includeRootIncidents or includeIncidentsForType can be set.");
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
    } else if (includeRootIncidents != null && includeRootIncidents) {
      query.includeRootIncidents();
    }

    List<ProcessDefinitionStatistics> queryResults = query.unlimitedList();

    List<StatisticsResultDto> results = new ArrayList<>();
    for (ProcessDefinitionStatistics queryResult : queryResults) {
      StatisticsResultDto dto = ProcessDefinitionStatisticsResultDto.fromProcessDefinitionStatistics(queryResult);
      results.add(dto);
    }

    return results;
  }

  @Override
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

  @Override
  public void deleteProcessDefinitionsByKey(String processDefinitionKey, boolean cascade, boolean skipCustomListeners, boolean skipIoMappings) {
    RepositoryService repositoryService = getProcessEngine().getRepositoryService();

    DeleteProcessDefinitionsBuilder builder = repositoryService.deleteProcessDefinitions()
        .byKey(processDefinitionKey);

    deleteProcessDefinitions(builder, cascade, skipCustomListeners, skipIoMappings);
  }

  @Override
  public void deleteProcessDefinitionsByKeyAndTenantId(String processDefinitionKey, boolean cascade, boolean skipCustomListeners, boolean skipIoMappings, String tenantId) {
    RepositoryService repositoryService = getProcessEngine().getRepositoryService();

    DeleteProcessDefinitionsBuilder builder = repositoryService.deleteProcessDefinitions()
        .byKey(processDefinitionKey)
        .withTenantId(tenantId);

    deleteProcessDefinitions(builder, cascade, skipCustomListeners, skipIoMappings);
  }

  protected void deleteProcessDefinitions(DeleteProcessDefinitionsBuilder builder, boolean cascade, boolean skipCustomListeners, boolean skipIoMappings) {
    if (skipCustomListeners) {
      builder = builder.skipCustomListeners();
    }

    if (cascade) {
      builder = builder.cascade();
    }

    if (skipIoMappings) {
      builder = builder.skipIoMappings();
    }

    try {
      builder.delete();
    } catch (NotFoundException e) { // rewrite status code from bad request (400) to not found (404)
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    }
  }

}
