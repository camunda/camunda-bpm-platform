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
import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.impl.stub.StubStatisticsBuilder;

public class ProcessDefinitionServiceImpl extends AbstractEngineService implements ProcessDefinitionService {

  // stub data for the statistics query while not implemented in the engine
  private static final String EXAMPLE_PROCESS_DEFINITION_ID = "processDefinition1";
  private static final int EXAMPLE_PROCESS_INSTANCES = 42;  
  private static final int EXAMPLE_FAILED_JOBS = 47;  
  private static final String ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID = "processDefinition2";
  private static final int ANOTHER_EXAMPLE_PROCESS_INSTANCES = 123;
  private static final int ANOTHER_EXAMPLE_FAILED_JOBS = 125;  
  
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

  /**
   * For the time being this is a stub implementation that returns a fixed data set.
   */
  @Override
  public List<StatisticsResultDto> getStatistics(String groupBy, Boolean includeFailedJobs) {
    if (groupBy == null || groupBy.equals("definition")) {
      if (includeFailedJobs != null && includeFailedJobs) {
        return getStubDataPerDefinitionWithFailedJobs();
      } else {
        return getStubDataPerDefinition();
      }
    } else if (groupBy.equals("version")) {
      if (includeFailedJobs != null && includeFailedJobs) {
        return getStubDataPerDefinitionVersionWithFailedJobs();
      } else {
        return getStubDataPerDefinitionVersion();
      }
    }
    throw new WebApplicationException(Status.BAD_REQUEST);
  }
 

  private List<StatisticsResultDto> getStubDataPerDefinition() {
    return StubStatisticsBuilder
        .addResult().id(EXAMPLE_PROCESS_DEFINITION_ID).instances(EXAMPLE_PROCESS_INSTANCES)
        .nextResult().id(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID).instances(ANOTHER_EXAMPLE_PROCESS_INSTANCES)
        .build();
  }
  
  private List<StatisticsResultDto> getStubDataPerDefinitionWithFailedJobs() {
    return StubStatisticsBuilder
        .addResult().id(EXAMPLE_PROCESS_DEFINITION_ID)
          .instances(EXAMPLE_PROCESS_INSTANCES)
          .failedJobs(EXAMPLE_FAILED_JOBS)
        .nextResult().id(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
          .instances(ANOTHER_EXAMPLE_PROCESS_INSTANCES)
          .failedJobs(ANOTHER_EXAMPLE_FAILED_JOBS)
        .build();
  }
  
  private List<StatisticsResultDto> getStubDataPerDefinitionVersion() {
    return StubStatisticsBuilder
         .addResult().id(EXAMPLE_PROCESS_DEFINITION_ID + ":1").instances(EXAMPLE_PROCESS_INSTANCES)
         .nextResult().id(EXAMPLE_PROCESS_DEFINITION_ID + ":2").instances(EXAMPLE_PROCESS_INSTANCES)
         .nextResult()
           .id(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID + ":1")
           .instances(ANOTHER_EXAMPLE_PROCESS_INSTANCES)
         .nextResult()
           .id(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID + ":2")
           .instances(ANOTHER_EXAMPLE_PROCESS_INSTANCES)
         .build();
  }
  
  private List<StatisticsResultDto> getStubDataPerDefinitionVersionWithFailedJobs() {
    return StubStatisticsBuilder
         .addResult()
           .id(EXAMPLE_PROCESS_DEFINITION_ID + ":1")
           .instances(EXAMPLE_PROCESS_INSTANCES)
           .failedJobs(EXAMPLE_FAILED_JOBS)
         .nextResult()
           .id(EXAMPLE_PROCESS_DEFINITION_ID + ":2")
           .instances(EXAMPLE_PROCESS_INSTANCES)
           .failedJobs(EXAMPLE_FAILED_JOBS)
         .nextResult()
           .id(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID + ":1")
           .instances(ANOTHER_EXAMPLE_PROCESS_INSTANCES)
           .failedJobs(ANOTHER_EXAMPLE_FAILED_JOBS)
         .nextResult()
           .id(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID + ":2")
           .instances(ANOTHER_EXAMPLE_PROCESS_INSTANCES)
           .failedJobs(ANOTHER_EXAMPLE_FAILED_JOBS)
         .build();
  }

}
