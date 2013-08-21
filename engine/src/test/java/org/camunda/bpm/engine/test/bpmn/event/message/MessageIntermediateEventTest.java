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

package org.camunda.bpm.engine.test.bpmn.event.message;

import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Daniel Meyer
 * @author Nico Rehwaldt
 */
public class MessageIntermediateEventTest extends PluggableProcessEngineTestCase {


  @Deployment
  public void testSingleIntermediateMessageEvent() {

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    List<String> activeActivityIds = runtimeService.getActiveActivityIds(pi.getId());
    assertNotNull(activeActivityIds);
    assertEquals(1, activeActivityIds.size());
    assertTrue(activeActivityIds.contains("messageCatch"));

    String messageName = "newInvoiceMessage";
    Execution execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName(messageName)
      .singleResult();

    assertNotNull(execution);

    runtimeService.messageEventReceived(messageName, execution.getId());

    Task task = taskService.createTaskQuery()
      .singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

  }

  @Deployment
  public void testConcurrentIntermediateMessageEvent() {

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    List<String> activeActivityIds = runtimeService.getActiveActivityIds(pi.getId());
    assertNotNull(activeActivityIds);
    assertEquals(2, activeActivityIds.size());
    assertTrue(activeActivityIds.contains("messageCatch1"));
    assertTrue(activeActivityIds.contains("messageCatch2"));

    String messageName = "newInvoiceMessage";
    List<Execution> executions = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName(messageName)
      .list();

    assertNotNull(executions);
    assertEquals(2, executions.size());

    runtimeService.messageEventReceived(messageName, executions.get(0).getId());

    Task task = taskService.createTaskQuery()
            .singleResult();
    assertNull(task);

    runtimeService.messageEventReceived(messageName, executions.get(1).getId());

    task = taskService.createTaskQuery()
      .singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());
  }

}
