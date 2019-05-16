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
package org.camunda.bpm.qa.upgrade.useroperationlog;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

/**
 * @author Yana.Vasileva
 *
 */
public class SetAssigneeProcessInstanceTaskScenario {

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/upgrade/useroperationlog/oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("createUserOperationLogEntries")
  public static ScenarioSetup createUserOperationLogEntries() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        IdentityService identityService = engine.getIdentityService();
        String processInstanceBusinessKey = "SetAssigneeProcessInstanceTaskScenario";
        engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess_userOpLog", processInstanceBusinessKey);
        Date date = new Date();
        User user = identityService.newUser("mary"+date.getTime());
        identityService.saveUser(user);
        identityService.setAuthentication(user.getId(), null);

        TaskService taskService = engine.getTaskService();
        List<Task> list = taskService.createTaskQuery().processInstanceBusinessKey(processInstanceBusinessKey).list();
        Task task = list.get(0);
        taskService.setAssignee(task.getId(), "john");
        
        List<UserOperationLogEntry> list2 = engine.getHistoryService().createUserOperationLogQuery().list();
        System.out.println(list2.size());
      }
    };
  }
}
