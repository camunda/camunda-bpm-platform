package org.camunda.bpm.engine.rest.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ManagementService;
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
import org.camunda.bpm.engine.rest.ProcessDefinitionService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.*;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class ProcessDefinitionServiceImpl extends AbstractEngineService implements ProcessDefinitionService {

  public ProcessDefinitionServiceImpl() {
    super();
  }

	@Override
	public List<ProcessDefinitionDto> getProcessDefinitions(ProcessDefinitionQueryDto queryDto,
	    Integer firstResult, Integer maxResults) {
	  List<ProcessDefinitionDto> definitions = new ArrayList<ProcessDefinitionDto>();

	  RepositoryService repoService = processEngine.getRepositoryService();

	  ProcessDefinitionQuery query;
	  try {
	     query = queryDto.toQuery(repoService);
	  } catch (InvalidRequestException e) {
	    throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
	  }

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
  public CountResultDto getProcessDefinitionsCount(ProcessDefinitionQueryDto queryDto) {
    RepositoryService repoService = processEngine.getRepositoryService();

    ProcessDefinitionQuery query;
    try {
       query = queryDto.toQuery(repoService);
    } catch (InvalidRequestException e) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
    }

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    return result;
  }

  @Override
  public ProcessDefinitionDto getProcessDefinition(String processDefinitionId) {
    RepositoryService repoService = processEngine.getRepositoryService();

    ProcessDefinition definition;
    try {
      definition = repoService.getProcessDefinition(processDefinitionId);
    } catch (ProcessEngineException e) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
    }

    ProcessDefinitionDto result = ProcessDefinitionDto.fromProcessDefinition(definition);

    return result;
  }

  @Override
  public ProcessInstanceDto startProcessInstance(UriInfo context, String processDefinitionId, StartProcessInstanceDto parameters) {
    RuntimeService runtimeService = processEngine.getRuntimeService();

    ProcessInstance instance = null;
    try {
      instance = runtimeService.startProcessInstanceById(processDefinitionId, parameters.getVariables());
    } catch (ProcessEngineException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    ProcessInstanceDto result = ProcessInstanceDto.fromProcessInstance(instance);
    result.addReflexiveLink(context, null, "self");
    return result;
  }

  /**
   * For the time being this is a stub implementation that returns a fixed data set.
   */
  @Override
  public List<StatisticsResultDto> getStatistics(Boolean includeFailedJobs) {
    ManagementService mgmtService = processEngine.getManagementService();
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
    ManagementService mgmtService = processEngine.getManagementService();
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
      processModelIn = processEngine.getRepositoryService().getProcessModel(processDefinitionId);
      byte[] processModel = IoUtil.readInputStream(processModelIn, "processModelBpmn20Xml");
      return ProcessDefinitionDiagramDto.create(processDefinitionId, new String(processModel, "UTF-8"));
    } catch (Exception e) {
      throw new WebApplicationException(e, Status.BAD_REQUEST);
    } finally {
      IoUtil.closeSilently(processModelIn);
    }
  }

  @Override
  public FormDto getStartForm(@PathParam("id") String processDefinitionId) {
    final FormService formService = processEngine.getFormService();
    
    final StartFormData formData;
    try {
      formData = formService.getStartFormData(processDefinitionId);
    } catch (ProcessEngineException e) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
    }

    return FormDto.fromFormData(formData);
  }
}
