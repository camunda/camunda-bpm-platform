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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.variables.FailingJavaSerializable;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.value.ObjectValue;


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

  public void testIntermediateMessageEventRedeployment() {

    // deploy version 1
    repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/message/MessageIntermediateEventTest.testSingleIntermediateMessageEvent.bpmn20.xml")
      .deploy();
    // now there is one process deployed
    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    List<String> activeActivityIds = runtimeService.getActiveActivityIds(pi.getId());
    assertNotNull(activeActivityIds);
    assertEquals(1, activeActivityIds.size());
    assertTrue(activeActivityIds.contains("messageCatch"));

    // deploy version 2
    repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/message/MessageIntermediateEventTest.testSingleIntermediateMessageEvent.bpmn20.xml")
      .deploy();

    // now there are two versions deployed:
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    // assert process is still waiting in message event:
    activeActivityIds = runtimeService.getActiveActivityIds(pi.getId());
    assertNotNull(activeActivityIds);
    assertEquals(1, activeActivityIds.size());
    assertTrue(activeActivityIds.contains("messageCatch"));

    // delete both versions:
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

  public void testEmptyMessageNameFails() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/message/MessageIntermediateEventTest.testEmptyMessageNameFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    }catch (ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Cannot have a message event subscription with an empty or missing name"));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/message/MessageIntermediateEventTest.testSingleIntermediateMessageEvent.bpmn20.xml")
  public void testSetSerializedVariableValues() throws IOException, ClassNotFoundException {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    EventSubscription messageEventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();

    // when
    FailingJavaSerializable javaSerializable = new FailingJavaSerializable("foo");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(javaSerializable);
    String serializedObject = StringUtil.fromBytes(Base64.encodeBase64(baos.toByteArray()), processEngine);

    // then it is not possible to deserialize the object
    try {
      new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    } catch (RuntimeException e) {
      assertTextPresent("Exception while deserializing object.", e.getMessage());
    }

    // but it can be set as a variable when delivering a message:
    runtimeService
      .messageEventReceived(
          "newInvoiceMessage",
          messageEventSubscription.getExecutionId(),
          Variables.createVariables().putValueTyped("var",
            Variables
            .serializedObjectValue(serializedObject)
            .objectTypeName(FailingJavaSerializable.class.getName())
            .serializationDataFormat(SerializationDataFormats.JAVA)
            .create()));

    // then
    ObjectValue variableTyped = runtimeService.getVariableTyped(processInstance.getId(), "var", false);
    assertNotNull(variableTyped);
    assertFalse(variableTyped.isDeserialized());
    assertEquals(serializedObject, variableTyped.getValueSerialized());
    assertEquals(FailingJavaSerializable.class.getName(), variableTyped.getObjectTypeName());
    assertEquals(SerializationDataFormats.JAVA.getName(), variableTyped.getSerializationDataFormat());
  }

}
