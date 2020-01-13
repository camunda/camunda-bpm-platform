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
package org.camunda.bpm.example.invoice;

import static org.camunda.bpm.engine.variable.Variables.fileValue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

public class InvoiceTestCase extends ProcessEngineTestCase {

  @Deployment(resources= {"invoice.v1.bpmn", "invoiceBusinessDecisions.dmn"})
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

  @Deployment(resources= {"invoice.v2.bpmn", "invoiceBusinessDecisions.dmn"})
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

  @Deployment(resources= {"invoice.v2.bpmn", "invoiceBusinessDecisions.dmn"})
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
      .execute();

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

  @Deployment(resources= {"invoice.v2.bpmn", "reviewInvoice.bpmn", "invoiceBusinessDecisions.dmn"})
  public void testNonSuccessfulPath() {
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
    variables.put("approved", Boolean.FALSE);
    taskService.complete(task.getId(), variables);

    task = taskService.createTaskQuery().singleResult();

    assertEquals("assignReviewer", task.getTaskDefinitionKey());
    variables.clear();
    variables.put("reviewer", "peter");
    taskService.complete(task.getId(), variables);

    task = taskService.createTaskQuery().singleResult();

    assertEquals("reviewInvoice", task.getTaskDefinitionKey());
    variables.clear();
    variables.put("clarified", Boolean.FALSE);
    taskService.complete(task.getId(), variables);

    assertProcessEnded(task.getProcessInstanceId());
    assertProcessEnded(pi.getId());
  }


}
