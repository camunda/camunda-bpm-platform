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

package org.camunda.bpm.engine.test.bpmn.servicetask;

import java.util.Collection;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * @author Sebastian Menski
 */
public class ServiceTaskBpmnModelExecutionContextTest extends PluggableProcessEngineTestCase {

  private static final String PROCESS_ID = "process";
  private String deploymentId;

  public void testJavaDelegateModelExecutionContext() {
    deploy();

    runtimeService.startProcessInstanceByKey(PROCESS_ID);

    BpmnModelInstance modelInstance = ModelExecutionContextServiceTask.modelInstance;
    assertNotNull(modelInstance);

    Model model = modelInstance.getModel();
    Collection<ModelElementInstance> events = modelInstance.getModelElementsByType(model.getType(Event.class));
    assertEquals(2, events.size());
    Collection<ModelElementInstance> tasks = modelInstance.getModelElementsByType(model.getType(Task.class));
    assertEquals(1, tasks.size());

    Process process = (Process) modelInstance.getDefinitions().getRootElements().iterator().next();
    assertEquals(PROCESS_ID, process.getId());
    assertTrue(process.isExecutable());

    ServiceTask serviceTask = ModelExecutionContextServiceTask.serviceTask;
    assertNotNull(serviceTask);
    assertEquals(ModelExecutionContextServiceTask.class.getName(), serviceTask.getCamundaClass());
  }

  private void deploy() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
      .startEvent()
      .serviceTask()
        .camundaClass(ModelExecutionContextServiceTask.class.getName())
      .endEvent()
      .done();

    deploymentId = repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy().getId();
  }

  public void tearDown() {
    ModelExecutionContextServiceTask.clear();
    repositoryService.deleteDeployment(deploymentId, true);
  }

}
