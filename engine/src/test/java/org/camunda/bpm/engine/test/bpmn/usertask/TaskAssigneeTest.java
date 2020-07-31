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
package org.camunda.bpm.engine.test.bpmn.usertask;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;


/**
 * Simple process test to validate the current implementation protoype.
 * 
 * @author Joram Barrez 
 */
public class TaskAssigneeTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testTaskAssignee() {    
    
    // Start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeExampleProcess");

    // Get task list
    List<Task> tasks = taskService
      .createTaskQuery()
      .taskAssignee("kermit")
      .list();
    assertEquals(1, tasks.size());
    Task myTask = tasks.get(0);
    assertEquals("Schedule meeting", myTask.getName());
    assertEquals("Schedule an engineering meeting for next week with the new hire.", myTask.getDescription());

    // Complete task. Process is now finished
    taskService.complete(myTask.getId());
    // assert if the process instance completed
    testRule.assertProcessEnded(processInstance.getId());
  }

}
