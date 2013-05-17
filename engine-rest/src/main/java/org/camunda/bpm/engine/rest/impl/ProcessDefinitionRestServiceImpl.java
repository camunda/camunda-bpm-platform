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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.ProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ActivityStatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class ProcessDefinitionRestServiceImpl extends AbstractRestProcessEngineAware implements ProcessDefinitionRestService {

  public ProcessDefinitionRestServiceImpl() {
    super();
  }

	public ProcessDefinitionRestServiceImpl(String engineName) {
    super(engineName);
  }

  @Override
	public List<ProcessDefinitionDto> getProcessDefinitions(UriInfo uriInfo,
	    Integer firstResult, Integer maxResults) {
    ProcessDefinitionQueryDto queryDto = new ProcessDefinitionQueryDto(uriInfo.getQueryParameters());
	  List<ProcessDefinitionDto> definitions = new ArrayList<ProcessDefinitionDto>();

	  RepositoryService repoService = getProcessEngine().getRepositoryService();

	  ProcessDefinitionQuery query = queryDto.toQuery(repoService);

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
	  
    RepositoryService repoService = getProcessEngine().getRepositoryService();
    ProcessDefinitionQuery query = queryDto.toQuery(repoService);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    return result;
  }

  @Override
  public ProcessDefinitionDto getProcessDefinition(String processDefinitionId) {
    RepositoryService repoService = getProcessEngine().getRepositoryService();

    ProcessDefinition definition;
    try {
      definition = repoService.getProcessDefinition(processDefinitionId);
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "No matching definition with id " + processDefinitionId);
    }

    ProcessDefinitionDto result = ProcessDefinitionDto.fromProcessDefinition(definition);

    return result;
  }

  @Override
  public ProcessInstanceDto startProcessInstance(UriInfo context, String processDefinitionId, StartProcessInstanceDto parameters) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();

    ProcessInstance instance = null;
    try {
      instance = runtimeService.startProcessInstanceById(processDefinitionId, parameters.getVariables());
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot instantiate process definition " + processDefinitionId);
    }

    ProcessInstanceDto result = ProcessInstanceDto.fromProcessInstance(instance);
    UriBuilder rootUriBuilder = context.getBaseUriBuilder().path(relativeRootResourcePath);
    result.addReflexiveLink(rootUriBuilder, null, "self");
    return result;
  }

  @Override
  public List<StatisticsResultDto> getStatistics(Boolean includeFailedJobs) {
    ManagementService mgmtService = getProcessEngine().getManagementService();
    ProcessDefinitionStatisticsQuery query = mgmtService.createProcessDefinitionStatisticsQuery();
    if (includeFailedJobs != null && includeFailedJobs) {
      query.includeFailedJobs();
    }

    List<ProcessDefinitionStatistics> queryResults = query.list();

    List<StatisticsResultDto> results = new ArrayList<StatisticsResultDto>();
    for (ProcessDefinitionStatistics queryResult : queryResults) {
      StatisticsResultDto dto = ProcessDefinitionStatisticsResultDto.fromProcessDefinitionStatistics(queryResult);
      results.add(dto);
    }

    return results;
  }

  @Override
  public List<StatisticsResultDto> getActivityStatistics(String processDefinitionId, Boolean includeFailedJobs) {
    ManagementService mgmtService = getProcessEngine().getManagementService();
    ActivityStatisticsQuery query = mgmtService.createActivityStatisticsQuery(processDefinitionId);
    if (includeFailedJobs != null && includeFailedJobs) {
      query.includeFailedJobs();
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
  public ProcessDefinitionDiagramDto getProcessDefinitionBpmn20Xml(String processDefinitionId) {
    InputStream processModelIn = null;
    try {
      processModelIn = getProcessEngine().getRepositoryService().getProcessModel(processDefinitionId);
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
  public FormDto getStartForm(String processDefinitionId) {
    final FormService formService = getProcessEngine().getFormService();
    
    final StartFormData formData;
    try {
      formData = formService.getStartFormData(processDefinitionId);
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Cannot get start form data for process definition " + processDefinitionId);
    }

    return FormDto.fromFormData(formData);
  }
}
