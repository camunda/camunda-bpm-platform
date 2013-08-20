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
package org.camunda.bpm.engine.rest.sub.repository;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.ProcessInstanceRestService;
import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ActivityStatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.util.ApplicationContextPathUtil;
import org.camunda.bpm.engine.rest.util.DtoUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class ProcessDefinitionResourceImpl implements ProcessDefinitionResource {

  private ProcessEngine engine;
  private String processDefinitionId;
  private String rootResourcePath;

  public ProcessDefinitionResourceImpl(ProcessEngine engine, String processDefinitionId, String rootResourcePath) {
    this.engine = engine;
    this.processDefinitionId = processDefinitionId;
    this.rootResourcePath = rootResourcePath;
  }

  @Override
  public ProcessDefinitionDto getProcessDefinition() {
    RepositoryService repoService = engine.getRepositoryService();

    ProcessDefinition definition;
    try {
      definition = repoService.getProcessDefinition(processDefinitionId);
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "No matching definition with id " + processDefinitionId);
    }

    ProcessDefinitionDto result = ProcessDefinitionDto.fromProcessDefinition(definition);

    return result;
  }

  @Override
  public ProcessInstanceDto startProcessInstance(UriInfo context, StartProcessInstanceDto parameters) {
    RuntimeService runtimeService = engine.getRuntimeService();

    ProcessInstance instance = null;
    try {
      Map<String, Object> variables = DtoUtil.toMap(parameters.getVariables());
      instance = runtimeService.startProcessInstanceById(processDefinitionId, variables);

    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot instantiate process definition %s: %s", processDefinitionId, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);

    } catch (NumberFormatException e) {
      String errorMessage = String.format("Cannot instantiate process definition %s due to number format exception: %s", processDefinitionId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);

    } catch (ParseException e) {
      String errorMessage = String.format("Cannot instantiate process definition %s due to parse exception: %s", processDefinitionId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);

    } catch (IllegalArgumentException e) {
      String errorMessage = String.format("Cannot instantiate process definition %s: %s", processDefinitionId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, errorMessage);
    }

    ProcessInstanceDto result = ProcessInstanceDto.fromProcessInstance(instance);

    URI uri = context.getBaseUriBuilder()
      .path(rootResourcePath)
      .path(ProcessInstanceRestService.class)
      .path(instance.getId())
      .build();

    result.addReflexiveLink(uri, HttpMethod.GET, "self");

    return result;
  }


  @Override
  public List<StatisticsResultDto> getActivityStatistics(Boolean includeFailedJobs, Boolean includeIncidents, String includeIncidentsForType) {
    if (includeIncidents != null && includeIncidentsForType != null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Only one of the query parameter includeIncidents or includeIncidentsForType can be set.");
    }

    ManagementService mgmtService = engine.getManagementService();
    ActivityStatisticsQuery query = mgmtService.createActivityStatisticsQuery(processDefinitionId);

    if (includeFailedJobs != null && includeFailedJobs) {
      query.includeFailedJobs();
    }

    if (includeIncidents != null && includeIncidents) {
      query.includeIncidents();
    } else if (includeIncidentsForType != null) {
      query.includeIncidentsForType(includeIncidentsForType);
    }

    List<ActivityStatistics> queryResults = query.list();

    List<StatisticsResultDto> results = new ArrayList<StatisticsResultDto>();
    for (ActivityStatistics queryResult : queryResults) {
      StatisticsResultDto dto = ActivityStatisticsResultDto.fromActivityStatistics(queryResult);
      results.add(dto);
    }

    return results;
  }

  @Override
  public ProcessDefinitionDiagramDto getProcessDefinitionBpmn20Xml() {
    InputStream processModelIn = null;
    try {
      processModelIn = engine.getRepositoryService().getProcessModel(processDefinitionId);
      byte[] processModel = IoUtil.readInputStream(processModelIn, "processModelBpmn20Xml");
      return ProcessDefinitionDiagramDto.create(processDefinitionId, new String(processModel, "UTF-8"));
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "No matching definition with id " + processDefinitionId);
    } catch (UnsupportedEncodingException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);
    } finally {
      IoUtil.closeSilently(processModelIn);
    }
  }

  @Override
  public FormDto getStartForm() {
    final FormService formService = engine.getFormService();

    final StartFormData formData;
    try {
      formData = formService.getStartFormData(processDefinitionId);
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Cannot get start form data for process definition " + processDefinitionId);
    }
    FormDto dto = FormDto.fromFormData(formData);
    dto.setContextPath(ApplicationContextPathUtil.getApplicationPath(engine, processDefinitionId));

    return dto;
  }
}
