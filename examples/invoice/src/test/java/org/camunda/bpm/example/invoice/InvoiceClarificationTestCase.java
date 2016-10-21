package org.camunda.bpm.example.invoice;

import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;
import org.camunda.bpm.engine.variable.Variables;

public class InvoiceClarificationTestCase extends ProcessEngineTestCase {
  
  @Deployment(resources = "invoice-clarification.cmmn")
  public void testClarifyInvoice() {
    
    // given case instance started
    CaseInstance caseInstance = caseService.createCaseInstanceByKey("InvoiceClarificationCase");
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("clarifyInvoice", task.getTaskDefinitionKey());
    
    // when "Clarify Invoice" is completed
    taskService.complete(task.getId());
    
    // then the case is completed
    CaseInstance completedCase = caseService.createCaseInstanceQuery().active().caseInstanceId(caseInstance.getId()).singleResult();
    assertTrue(completedCase == null);
  }
  
  @Deployment(resources = "invoice-clarification.cmmn")
  public void testClarifyAndReviewInvoice() {
    
    // given the case instance started
    CaseInstance caseInstance = caseService.createCaseInstanceByKey("InvoiceClarificationCase");
    
    // and "Assign Reviewer" completed
    CaseExecution assignReviewerExecution = caseService.createCaseExecutionQuery().activityId("assignReviewer").singleResult();
    caseService.manuallyStartCaseExecution(assignReviewerExecution.getId());
    Task task = taskService.createTaskQuery().taskDefinitionKey("assignReviewer").singleResult();
    taskService.complete(task.getId(), Variables.createVariables().putValue("reviewer", "mary"));
    
    // when "Review Invoice" is completed
    Task reviewInvoiceTask = taskService.createTaskQuery().taskDefinitionKey("reviewInvoice").singleResult();
    taskService.complete(reviewInvoiceTask.getId());
    
    // and "Clarify Invoice" is completed
    Task clarifyInvoiceTask = taskService.createTaskQuery().taskDefinitionKey("clarifyInvoice").singleResult();
    taskService.complete(clarifyInvoiceTask.getId());
    
    // then the case is completed 
    CaseInstance completedCase = caseService.createCaseInstanceQuery().active().caseInstanceId(caseInstance.getId()).singleResult();
    assertTrue(completedCase == null);   
  }
  
  @Deployment(resources = "invoice-clarification.cmmn")
  public void testAssignReviewer() {
    // given case instance started 
    CaseInstance caseInstance = caseService.createCaseInstanceByKey("InvoiceClarificationCase");
    CaseExecution assignReviewerExecution = caseService.createCaseExecutionQuery().activityId("assignReviewer").singleResult();
    caseService.manuallyStartCaseExecution(assignReviewerExecution.getId());
    
    // when "Assign Reviewer" is started
    Task task = taskService.createTaskQuery().taskDefinitionKey("assignReviewer").singleResult();
    
    // and "Clarify Invoice" is completed
    Task clarifyInvoiceTask = taskService.createTaskQuery().taskDefinitionKey("clarifyInvoice").singleResult();
    taskService.complete(clarifyInvoiceTask.getId());
    
    // then the case is still active
    CaseInstance completedCase = caseService.createCaseInstanceQuery().active().caseInstanceId(caseInstance.getId()).singleResult();
    assertNotNull(completedCase);    
  }

}
