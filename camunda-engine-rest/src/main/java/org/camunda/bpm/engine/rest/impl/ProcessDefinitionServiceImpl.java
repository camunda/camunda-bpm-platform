package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.rest.ProcessDefinitionService;
import org.camunda.bpm.engine.rest.dto.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

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
  public ProcessInstanceDto startProcessInstance(UriInfo context, String processDefinitionId, StartProcessInstanceDto parameters) {
    RuntimeService runtimeService = processEngine.getRuntimeService();
    
    ProcessInstance instance = null;
    try {
      instance = runtimeService.startProcessInstanceById(processDefinitionId, parameters.getVariables());
    } catch (ActivitiException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    
    ProcessInstanceDto result = ProcessInstanceDto.fromProcessInstance(instance);
    result.addReflexiveLink(context, null, "self");
    return result;
  }

}
