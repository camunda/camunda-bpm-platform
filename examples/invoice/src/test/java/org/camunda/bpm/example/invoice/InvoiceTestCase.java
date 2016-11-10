package org.camunda.bpm.example.invoice;

import static org.camunda.bpm.engine.variable.Variables.fileValue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

public class InvoiceTestCase extends ProcessEngineTestCase {

  @Deployment(resources= {"invoice.v1.bpmn", "assign-approver-groups.dmn"})
  public void testHappyPathV1() {
    InputStream invoiceInputStream = InvoiceProcessApplication.class.getClassLoader().getResourceAsStream("invoice.pdf");
    VariableMap variables = Variables.createVariables()
      .putValue("creditor", "Great Pizza for Everyone Inc.")
      .putValue("amount", 300.0d)
      .putValue("invoiceCategory", "Travel Expenses")
      .putValue("invoiceNumber", "GPFE-23232323")
      .putValue("invoiceDocument", fileValue("invoice.pdf")
        .file(invoiceInputStream)
        .mimeType("application/pdf")
        .create());

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("invoice", variables);

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("approveInvoice", task.getTaskDefinitionKey());

    List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
    Set<String> approverGroups = new HashSet<String>();
    for (IdentityLink link : links) {
      approverGroups.add(link.getGroupId());
    }
    assertEquals(2, approverGroups.size());
    assertTrue(approverGroups.contains("accounting"));
    assertTrue(approverGroups.contains("sales"));

    variables.clear();
    variables.put("approved", Boolean.TRUE);
    taskService.complete(task.getId(), variables);

    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();

    assertEquals("prepareBankTransfer", task.getTaskDefinitionKey());
    taskService.complete(task.getId());

    Job archiveInvoiceJob = managementService.createJobQuery().singleResult();
    assertNotNull(archiveInvoiceJob);
    managementService.executeJob(archiveInvoiceJob.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment(resources= {"invoice.v2.bpmn", "assign-approver-groups.dmn"})
  public void testHappyPathV2() {
    InputStream invoiceInputStream = InvoiceProcessApplication.class.getClassLoader().getResourceAsStream("invoice.pdf");
    VariableMap variables = Variables.createVariables()
      .putValue("creditor", "Great Pizza for Everyone Inc.")
      .putValue("amount", 300.0d)
      .putValue("invoiceCategory", "Travel Expenses")
      .putValue("invoiceNumber", "GPFE-23232323")
      .putValue("invoiceDocument", fileValue("invoice.pdf")
        .file(invoiceInputStream)
        .mimeType("application/pdf")
        .create());

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("invoice", variables);

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("approveInvoice", task.getTaskDefinitionKey());

    List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
    Set<String> approverGroups = new HashSet<String>();
    for (IdentityLink link : links) {
      approverGroups.add(link.getGroupId());
    }
    assertEquals(2, approverGroups.size());
    assertTrue(approverGroups.contains("accounting"));
    assertTrue(approverGroups.contains("sales"));

    variables.clear();
    variables.put("approved", Boolean.TRUE);
    taskService.complete(task.getId(), variables);

    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();

    assertEquals("prepareBankTransfer", task.getTaskDefinitionKey());
    taskService.complete(task.getId());

    Job archiveInvoiceJob = managementService.createJobQuery().singleResult();
    assertNotNull(archiveInvoiceJob);
    managementService.executeJob(archiveInvoiceJob.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment(resources= {"invoice.v2.bpmn", "assign-approver-groups.dmn"})
  public void testApproveInvoiceAssignment() {
    InputStream invoiceInputStream = InvoiceProcessApplication.class.getClassLoader().getResourceAsStream("invoice.pdf");

    VariableMap variables = Variables.createVariables()
      .putValue("creditor", "Great Pizza for Everyone Inc.")
      .putValue("amount", 300.0d)
      .putValue("invoiceCategory", "Travel Expenses")
      .putValue("invoiceNumber", "GPFE-23232323")
      .putValue("invoiceDocument", fileValue("invoice.pdf")
        .file(invoiceInputStream)
        .mimeType("application/pdf")
        .create())
      .putValue("approverGroups", Arrays.asList("sales", "accounting"));

    ProcessInstance pi = runtimeService.createProcessInstanceByKey("invoice")
      .setVariables(variables)
      .startBeforeActivity("approveInvoice")
      .executeWithVariablesInReturn();

    // given that the process instance is waiting at task "approveInvoice"
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("approveInvoice", task.getTaskDefinitionKey());

    // and task has candidate groups
    List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
    Set<String> approverGroups = new HashSet<String>();
    for (IdentityLink link : links) {
      approverGroups.add(link.getGroupId());
    }
    assertEquals(2, approverGroups.size());
    assertTrue(approverGroups.contains("accounting"));
    assertTrue(approverGroups.contains("sales"));

    // and variable approver is null
    assertNull(taskService.getVariable(task.getId(), "approver"));

    // if mary claims the task
    taskService.claim(task.getId(), "mary");

    // then the variable "approver" exists and is set to mary
    assertEquals("mary", taskService.getVariable(task.getId(), "approver"));

  }
  
  @Deployment(resources = {"invoice.v2.bpmn", "invoice-clarification.cmmn"})
  public void testInvoiceClarification() {
    InputStream invoiceInputStream = InvoiceProcessApplication.class.getClassLoader().getResourceAsStream("invoice.pdf");

    VariableMap variables = Variables.createVariables()
      .putValue("creditor", "Great Pizza for Everyone Inc.")
      .putValue("amount", 300.0d)
      .putValue("invoiceCategory", "Travel Expenses")
      .putValue("invoiceNumber", "GPFE-23232323")
      .putValue("invoiceDocument", fileValue("invoice.pdf")
        .file(invoiceInputStream)
        .mimeType("application/pdf")
        .create())
      .putValue("approverGroups", Arrays.asList("sales", "accounting"))
      .putValue("approved", Boolean.FALSE);

    runtimeService.createProcessInstanceByKey("invoice")
      .setVariables(variables)
      .startAfterActivity("approveInvoice")
      .executeWithVariablesInReturn();

    // given that the process instance is waiting at case "Invoice clarification"
    Task task = taskService.createTaskQuery().caseDefinitionKey("InvoiceClarificationCase").singleResult();
    assertEquals("clarifyInvoice", task.getTaskDefinitionKey());
    
    // when the task "Assign Reviewer" is manually activated
    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId("assignReviewer").singleResult();
    caseService.manuallyStartCaseExecution(caseExecution.getId());
    Task assignReviewer = taskService
        .createTaskQuery().caseDefinitionKey("InvoiceClarificationCase")
        .taskDefinitionKey("assignReviewer").singleResult();
    // and completed
    taskService.complete(assignReviewer.getId(), Variables.createVariables().putValue("reviewer", "mary"));
    
    // then the task "Review Invoice" is active and assigned to mary
    Task marysTask = taskService.createTaskQuery().taskAssignee("mary").singleResult();
    assertEquals("reviewInvoice", marysTask.getTaskDefinitionKey());
  }

}
