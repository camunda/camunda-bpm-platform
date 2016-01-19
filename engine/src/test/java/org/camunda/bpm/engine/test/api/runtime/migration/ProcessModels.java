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
package org.camunda.bpm.engine.test.api.runtime.migration;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessModels {

  public static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess("UserTaskProcess")
      .startEvent()
      .userTask("userTask").name("User Task")
      .endEvent()
      .done();

  public static final BpmnModelInstance SUBPROCESS_PROCESS = Bpmn.createExecutableProcess("SubProcess")
    .startEvent()
    .subProcess("subProcess")
     .embeddedSubProcess()
        .startEvent()
        .userTask("userTask").name("User Task")
        .endEvent()
    .subProcessDone()
    .endEvent()
    .done();

  public static final BpmnModelInstance ONE_RECEIVE_TASK_PROCESS = Bpmn.createExecutableProcess("ReceiveTaskProcess")
    .startEvent()
    .receiveTask("receiveTask")
    .endEvent()
    .done();

  public static final BpmnModelInstance PARALLEL_GATEWAY_PROCESS = Bpmn.createExecutableProcess("ParallelGatewayProcess")
      .startEvent()
      .parallelGateway()
      .userTask("userTask1").name("User Task 1")
      .endEvent()
      .moveToLastGateway()
      .userTask("userTask2").name("User Task 2")
      .endEvent()
      .done();

  static {
    addMessageToReceiveTask(ONE_RECEIVE_TASK_PROCESS, "receiveTask", "Message");
  }

  protected static void addMessageToReceiveTask(BpmnModelInstance modelInstance, String receiveTaskId, String messageName) {
    ReceiveTask receiveTask = modelInstance.getModelElementById(receiveTaskId);
    Message message = modelInstance.newInstance(Message.class);
    modelInstance.getDefinitions().addChildElement(message);
    message.setName(messageName);
    receiveTask.setMessage(message);
  }

}
