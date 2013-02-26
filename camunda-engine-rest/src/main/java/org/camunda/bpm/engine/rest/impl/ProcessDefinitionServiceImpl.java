package org.camunda.bpm.engine.rest.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.rest.ProcessDefinitionService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.impl.stub.ActivityStubStatisticsBuilder;
import org.camunda.bpm.engine.rest.impl.stub.ProcessDefinitionStubStatisticsBuilder;

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
    } catch (ActivitiException e) {
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
    if (groupBy == null || groupBy.equals("version")) {
      if (includeFailedJobs != null && includeFailedJobs) {
        return getStubDataPerDefinitionVersionWithFailedJobs();
      } else {
        return getStubDataPerDefinitionVersion();
      }
    } else if (groupBy.equals("definition")) {
      if (includeFailedJobs != null && includeFailedJobs) {
        return getStubDataPerDefinitionWithFailedJobs();
      } else {
        return getStubDataPerDefinition();
      }
    }
    throw new WebApplicationException(Status.BAD_REQUEST);
  }
  
  @Override
  public List<StatisticsResultDto> getActivityStatistics(String processDefinitionId) {
    if (processDefinitionId != null && !processDefinitionId.isEmpty()) {
      return getStubDataPerActivity(processDefinitionId);
    }
    throw new WebApplicationException(Status.BAD_REQUEST);
  }

  private List<StatisticsResultDto> getStubDataPerActivity(String processDefinitionId) {
    return ActivityStubStatisticsBuilder
        .addResult()
          .id("assignApprover")
          .instances(10)
          .activityName("Assign Approver")
        .nextResult()
          .id("approveInvoice")
          .instances(1010)
          .activityName("Approve Invoice")
        .nextResult()
          .id("reviewInvoice")
          .instances(0)
          .activityName("Review Invoice")
        .nextResult()
          .id("prepareBankTransfer")
          .instances(130077)
          .activityName("Prepare Bank Transfer")
         .nextResult()
          .id("saveInvoiceToSVN")
          .activityName("Save Invoice To SVN")
        .build();    
  }

  private List<StatisticsResultDto> getStubDataPerDefinition() {
    return ProcessDefinitionStubStatisticsBuilder
        .addResult()
          .id("order_process_key").instances(17).definitionId("3")
          .definitionName("Order Process").definitionKey("order_process_key").definitionVersion(3)
        .nextResult()
          .id("fox_invoice").instances(8).definitionId("5")
          .definitionName("invoice receipt (fox)").definitionKey("fox_invoice").definitionVersion(2)
        .nextResult()
          .id("loan_applicant_process").instances(14).definitionId("8")
          .definitionName(null).definitionKey("loan_applicant_process").definitionVersion(3)
        .nextResult()
          .id("loan_applicant_process_long_name").instances(100).definitionId("9")
          .definitionName("Loan applicant, with a very long process definition name").definitionKey("loan_applicant_process_long_name")
          .definitionVersion(1)
        .nextResult()
          .id("order_process_key_1").instances(17).definitionId("12")
          .definitionName("Order Process, the second one").definitionKey("order_process_key_1")
          .definitionVersion(3)     
        .nextResult()
          .id("fox_invoice_1").instances(8).definitionId("14")
          .definitionName("invoice receipt (fox) new").definitionKey("fox_invoice_1")
          .definitionVersion(2)     
       .nextResult()
          .id("loan_applicant_process_1").instances(14).definitionId("17")
          .definitionName(null).definitionKey("loan_applicant_process_1")
          .definitionVersion(3)      
       .nextResult()
          .id("loan_applicant_process_long_name_1").instances(100).definitionId("18")
          .definitionName("Loan applicant, with a very long process definition name 1").definitionKey("loan_applicant_process_long_name_1")
          .definitionVersion(1)        
        .build();
  }
  
  private List<StatisticsResultDto> getStubDataPerDefinitionWithFailedJobs() {
    return ProcessDefinitionStubStatisticsBuilder
        .addResult()
          .id("order_process_key").instances(17).failedJobs(36).definitionId("3")
          .definitionName("Order Process").definitionKey("order_process_key").definitionVersion(3)
        .nextResult()
          .id("fox_invoice").instances(8).failedJobs(24).definitionId("5")
          .definitionName("invoice receipt (fox)").definitionKey("fox_invoice").definitionVersion(2)
        .nextResult()
          .id("loan_applicant_process").instances(14).failedJobs(36).definitionId("8")
          .definitionName(null).definitionKey("loan_applicant_process").definitionVersion(3)
        .nextResult()
          .id("loan_applicant_process_long_name").instances(100).failedJobs(12).definitionId("9")
          .definitionName("Loan applicant, with a very long process definition name").definitionKey("loan_applicant_process_long_name")
          .definitionVersion(1)
        .nextResult()
          .id("order_process_key_1").instances(17).failedJobs(36).definitionId("12")
          .definitionName("Order Process, the second one").definitionKey("order_process_key_1")
          .definitionVersion(3)     
        .nextResult()
          .id("fox_invoice_1").instances(8).failedJobs(24).definitionId("14")
          .definitionName("invoice receipt (fox) new").definitionKey("fox_invoice_1")
          .definitionVersion(2)     
       .nextResult()
          .id("loan_applicant_process_1").instances(14).failedJobs(36).definitionId("17")
          .definitionName(null).definitionKey("loan_applicant_process_1")
          .definitionVersion(3)      
       .nextResult()
          .id("loan_applicant_process_long_name_1").instances(100).failedJobs(12).definitionId("18")
          .definitionName("Loan applicant, with a very long process definition name 1").definitionKey("loan_applicant_process_long_name_1")
          .definitionVersion(1)        
        .build();
  }
  
  private List<StatisticsResultDto> getStubDataPerDefinitionVersion() {
    return ProcessDefinitionStubStatisticsBuilder
        .addResult()
          .id("1").instances(5).definitionId("1")
          .definitionName("Order Process").definitionKey("order_process_key").definitionVersion(1)
        .nextResult()
          .id("2").instances(10).definitionId("2")
          .definitionName("Order Process").definitionKey("order_process_key").definitionVersion(2)
        .nextResult()
          .id("3").instances(2).definitionId("3")
          .definitionName("Order Process").definitionKey("order_process_key").definitionVersion(3)
        .nextResult()
          .id("4").instances(5).definitionId("4")
          .definitionName("invoice receipt").definitionKey("fox_invoice").definitionVersion(1)
        .nextResult()
          .id("5").instances(3).definitionId("5")
          .definitionName("invoice receipt (fox)").definitionKey("fox_invoice").definitionVersion(2)
        .nextResult()
          .id("6").instances(4).definitionId("6")
          .definitionName("Loan applicant").definitionKey("loan_applicant_process").definitionVersion(1)
        .nextResult()
          .id("7").instances(8).definitionId("7")
          .definitionName("Loan applicant").definitionKey("loan_applicant_process").definitionVersion(2)
        .nextResult()
          .id("8").instances(2).definitionId("8")
          .definitionName(null).definitionKey("loan_applicant_process").definitionVersion(3)
        .nextResult()
          .id("9").instances(100).definitionId("9")
          .definitionName("Loan applicant, with a very long process definition name").definitionKey("loan_applicant_process_long_name")
          .definitionVersion(1)
        .nextResult()
          .id("10").instances(5).definitionId("10")
          .definitionName("Order Process, the second one").definitionKey("order_process_key_1")
          .definitionVersion(1)  
        .nextResult()
          .id("11").instances(10).definitionId("11")
          .definitionName("Order Process, the second one").definitionKey("order_process_key_1")
          .definitionVersion(2)    
        .nextResult()
          .id("12").instances(2).definitionId("12")
          .definitionName("Order Process, the second one").definitionKey("order_process_key_1")
          .definitionVersion(3)     
        .nextResult()
          .id("13").instances(5).definitionId("13")
          .definitionName("invoice receipt new").definitionKey("fox_invoice_1")
          .definitionVersion(1)     
        .nextResult()
          .id("14").instances(3).definitionId("14")
          .definitionName("invoice receipt (fox) new").definitionKey("fox_invoice_1")
          .definitionVersion(2)     
        .nextResult()
          .id("15").instances(4).definitionId("15")
          .definitionName("Loan applicant 2").definitionKey("loan_applicant_process_1")
          .definitionVersion(1)     
       .nextResult()
          .id("16").instances(8).definitionId("16")
          .definitionName("Loan applicant 2").definitionKey("loan_applicant_process_1")
          .definitionVersion(2)       
       .nextResult()
          .id("17").instances(2).definitionId("17")
          .definitionName(null).definitionKey("loan_applicant_process_1")
          .definitionVersion(3)      
       .nextResult()
          .id("18").instances(100).definitionId("18")
          .definitionName("Loan applicant, with a very long process definition name 1").definitionKey("loan_applicant_process_long_name_1")
          .definitionVersion(1)        
        .build();
  }
  
  private List<StatisticsResultDto> getStubDataPerDefinitionVersionWithFailedJobs() {
    return ProcessDefinitionStubStatisticsBuilder
         .addResult()
           .id("1").instances(5).failedJobs(12).definitionId("1")
           .definitionName("Order Process").definitionKey("order_process_key").definitionVersion(1)
         .nextResult()
           .id("2").instances(10).failedJobs(12).definitionId("2")
           .definitionName("Order Process").definitionKey("order_process_key").definitionVersion(2)
         .nextResult()
           .id("3").instances(2).failedJobs(12).definitionId("3")
           .definitionName("Order Process").definitionKey("order_process_key").definitionVersion(3)
         .nextResult()
           .id("4").instances(5).failedJobs(12).definitionId("4")
           .definitionName("invoice receipt").definitionKey("fox_invoice").definitionVersion(1)
         .nextResult()
           .id("5").instances(3).failedJobs(12).definitionId("5")
           .definitionName("invoice receipt (fox)").definitionKey("fox_invoice").definitionVersion(2)
         .nextResult()
           .id("6").instances(4).failedJobs(12).definitionId("6")
           .definitionName("Loan applicant").definitionKey("loan_applicant_process").definitionVersion(1)
         .nextResult()
           .id("7").instances(8).failedJobs(12).definitionId("7")
           .definitionName("Loan applicant").definitionKey("loan_applicant_process").definitionVersion(2)
         .nextResult()
           .id("8").instances(2).failedJobs(12).definitionId("8")
           .definitionName(null).definitionKey("loan_applicant_process").definitionVersion(3)
         .nextResult()
           .id("9").instances(100).failedJobs(12).definitionId("9")
           .definitionName("Loan applicant, with a very long process definition name").definitionKey("loan_applicant_process_long_name")
           .definitionVersion(1)
         .nextResult()
           .id("10").instances(5).failedJobs(12).definitionId("10")
           .definitionName("Order Process, the second one").definitionKey("order_process_key_1")
           .definitionVersion(1)  
         .nextResult()
           .id("11").instances(10).failedJobs(12).definitionId("11")
           .definitionName("Order Process, the second one").definitionKey("order_process_key_1")
           .definitionVersion(2)    
         .nextResult()
           .id("12").instances(2).failedJobs(12).definitionId("12")
           .definitionName("Order Process, the second one").definitionKey("order_process_key_1")
           .definitionVersion(3)     
         .nextResult()
           .id("13").instances(5).failedJobs(12).definitionId("13")
           .definitionName("invoice receipt new").definitionKey("fox_invoice_1")
           .definitionVersion(1)     
         .nextResult()
           .id("14").instances(3).failedJobs(12).definitionId("14")
           .definitionName("invoice receipt (fox) new").definitionKey("fox_invoice_1")
           .definitionVersion(2)     
         .nextResult()
           .id("15").instances(4).failedJobs(12).definitionId("15")
           .definitionName("Loan applicant 2").definitionKey("loan_applicant_process_1")
           .definitionVersion(1)     
        .nextResult()
           .id("16").instances(8).failedJobs(12).definitionId("16")
           .definitionName("Loan applicant 2").definitionKey("loan_applicant_process_1")
           .definitionVersion(2)       
        .nextResult()
           .id("17").instances(2).failedJobs(12).definitionId("17")
           .definitionName(null).definitionKey("loan_applicant_process_1")
           .definitionVersion(3)      
        .nextResult()
           .id("18").instances(100).failedJobs(12).definitionId("18")
           .definitionName("Loan applicant, with a very long process definition name 1").definitionKey("loan_applicant_process_long_name_1")
           .definitionVersion(1)        
         .build();
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
}
