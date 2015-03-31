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
package org.camunda.bpm.example.invoice;

import static org.camunda.bpm.engine.variable.Variables.createVariables;

import java.util.Arrays;
import java.util.Calendar;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

/**
 * Process Application exposing this application's resources the process engine.
 */
@ProcessApplication
public class InvoiceProcessApplication extends ServletProcessApplication {

  /**
   * In a @PostDeploy Hook you can interact with the process engine and access
   * the processes the application has deployed.
   */
  @PostDeploy
  public void startFirstProcess(ProcessEngine processEngine) {

    createUsers(processEngine);
    startProcessInstance(processEngine);
  }

  private void startProcessInstance(ProcessEngine processEngine) {
    // process instance 1
    processEngine.getRuntimeService().startProcessInstanceByKey("invoice", createVariables()
        .putValue("creditor", "Great Pizza for Everyone Inc.")
        .putValue("amount", "30€")
        .putValue("invoiceNumber", "GPFE-23232323"));

    // process instance 2
    ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceByKey("invoice", createVariables()
        .putValue("creditor", "Bobby's Office Supplies")
        .putValue("amount", "312.99$")
        .putValue("invoiceNumber", "BOS-43934"));
    try {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, -14);
      ClockUtil.setCurrentTime(calendar.getTime());
      processEngine.getIdentityService().setAuthentication("demo", Arrays.asList(Groups.CAMUNDA_ADMIN));
      Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getId()).singleResult();
      processEngine.getTaskService().complete(task.getId(), createVariables().putValue("approver", "john"));
    }
    finally{
      ClockUtil.reset();
      processEngine.getIdentityService().clearAuthentication();
    }

    // process instance 3
    pi = processEngine.getRuntimeService().startProcessInstanceByKey("invoice", createVariables()
        .putValue("creditor", "Papa Steve's all you can eat")
        .putValue("invoiceNumber", "PSACE-5342"));
    try {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, -5);
      ClockUtil.setCurrentTime(calendar.getTime());
      processEngine.getIdentityService().setAuthentication("demo", Arrays.asList(Groups.CAMUNDA_ADMIN));
      Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getId()).singleResult();
      processEngine.getTaskService().complete(task.getId(), createVariables().putValue("approver", "mary"));
    }
    finally{
      processEngine.getIdentityService().clearAuthentication();
    }
    try {
      processEngine.getIdentityService().setAuthenticatedUserId("mary");
      Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getId()).singleResult();
      processEngine.getTaskService().createComment(null, pi.getId(), "I cannot approve this invoice: the amount is missing.\n\n Could you please provide the amount?");
      processEngine.getTaskService().complete(task.getId(), createVariables().putValue("approved", "false"));
    }
    finally{
      ClockUtil.reset();
      processEngine.getIdentityService().clearAuthentication();
    }
  }

  private void createUsers(ProcessEngine processEngine) {

    // create demo users
    new DemoDataGenerator().createUsers(processEngine);
  }
}
