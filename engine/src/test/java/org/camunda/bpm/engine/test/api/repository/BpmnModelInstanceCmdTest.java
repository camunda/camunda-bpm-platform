/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.test.api.repository;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Collection;

/**
 * @author Sebastian Menski
 */
public class BpmnModelInstanceCmdTest extends PluggableProcessEngineTestCase {

  private final static String PROCESS_KEY = "process";
  private String deploymentId;

  public void testRepositoryService() {
    deployTestProcess();
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_KEY).singleResult().getId();

    BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
    assertNotNull(modelInstance);
    Collection<ModelElementInstance> events = modelInstance.getModelElementsByType(modelInstance.getModel().getType(Event.class));
    assertEquals(2, events.size());
    Collection<ModelElementInstance> tasks = modelInstance.getModelElementsByType(modelInstance.getModel().getType(UserTask.class));
    assertEquals(1, tasks.size());

    Task task = taskService.createTaskQuery().singleResult();
    UserTask userTask = (UserTask) modelInstance.getModelElementById(task.getTaskDefinitionKey());
    assertNotNull(userTask);

    taskService.complete(task.getId());
  }

  private void deployTestProcess() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_KEY)
      .startEvent().userTask().endEvent().done();
    deploymentId = repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy().getId();
  }

  public void tearDown() {
    repositoryService.deleteDeployment(deploymentId, true);
  }
}
