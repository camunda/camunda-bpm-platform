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

package org.camunda.bpm.engine.test.api.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.variables.FailingJavaSerializable;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.value.ObjectValue;

/**
 * @author Thorben Lindhauer
 */
public class MessageCorrelationTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testCatchingMessageEventCorrelation() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    variables = new HashMap<String, Object>();
    variables.put("aKey", "anotherValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    correlationKeys.put("aKey", "aValue");
    Map<String, Object> messagePayload = new HashMap<String, Object>();
    messagePayload.put("aNewKey", "aNewVariable");

    runtimeService.correlateMessage(messageName, correlationKeys, messagePayload);

    long uncorrelatedExecutions = runtimeService.createExecutionQuery()
        .processVariableValueEquals("aKey", "anotherValue").messageEventSubscriptionName("newInvoiceMessage")
        .count();
    assertEquals(1, uncorrelatedExecutions);

    // the execution that has been correlated should have advanced
    long correlatedExecutions = runtimeService.createExecutionQuery()
        .activityId("task").processVariableValueEquals("aKey", "aValue").processVariableValueEquals("aNewKey", "aNewVariable")
        .count();
    assertEquals(1, correlatedExecutions);

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // this time: use the builder ////////////////

    variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    // use the fluent builder
    runtimeService.createMessageCorrelation(messageName)
      .processInstanceVariableEquals("aKey", "aValue")
      .setVariable("aNewKey", "aNewVariable")
      .correlate();

    uncorrelatedExecutions = runtimeService.createExecutionQuery()
        .processVariableValueEquals("aKey", "anotherValue").messageEventSubscriptionName("newInvoiceMessage")
        .count();
    assertEquals(1, uncorrelatedExecutions);

    // the execution that has been correlated should have advanced
    correlatedExecutions = runtimeService.createExecutionQuery()
        .activityId("task").processVariableValueEquals("aKey", "aValue").processVariableValueEquals("aNewKey", "aNewVariable")
        .count();
    assertEquals(1, correlatedExecutions);

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testOneMatchinProcessInstanceUsingFluentCorrelateAll() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    variables = new HashMap<String, Object>();
    variables.put("aKey", "anotherValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    String messageName = "newInvoiceMessage";

    // use the fluent builder: correlate to first started process instance
    runtimeService.createMessageCorrelation(messageName)
      .processInstanceVariableEquals("aKey", "aValue")
      .setVariable("aNewKey", "aNewVariable")
      .correlateAll();

    // there exists an uncorrelated executions (the second process instance)
    long uncorrelatedExecutions = runtimeService
        .createExecutionQuery()
        .processVariableValueEquals("aKey", "anotherValue")
        .messageEventSubscriptionName("newInvoiceMessage")
        .count();
    assertEquals(1, uncorrelatedExecutions);

    // the execution that has been correlated should have advanced
    long correlatedExecutions = runtimeService.createExecutionQuery()
        .activityId("task")
        .processVariableValueEquals("aKey", "aValue")
        .processVariableValueEquals("aNewKey", "aNewVariable")
        .count();
    assertEquals(1, correlatedExecutions);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testTwoMatchingProcessInstancesCorrelation() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    correlationKeys.put("aKey", "aValue");

    try {
      runtimeService.correlateMessage(messageName, correlationKeys);
      fail("Expected an Exception");
    } catch (MismatchingMessageCorrelationException e) {
      assertTextPresent("2 executions match the correlation keys.", e.getMessage());
    }

    // fluent builder fails as well
    try {
      runtimeService.createMessageCorrelation(messageName)
        .processInstanceVariableEquals("aKey", "aValue")
        .correlate();
      fail("Expected an Exception");
    } catch (MismatchingMessageCorrelationException e) {
      assertTextPresent("2 executions match the correlation keys.", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testTwoMatchingProcessInstancesUsingFluentCorrelateAll() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    correlationKeys.put("aKey", "aValue");

    // fluent builder multiple should not fail
    runtimeService.createMessageCorrelation(messageName)
      .processInstanceVariableEquals("aKey", "aValue")
      .setVariable("aNewKey", "aNewVariable")
      .correlateAll();

    long uncorrelatedExecutions = runtimeService
        .createExecutionQuery()
        .messageEventSubscriptionName("newInvoiceMessage")
        .count();
    assertEquals(0, uncorrelatedExecutions);

    // the executions that has been correlated should have advanced
    long correlatedExecutions = runtimeService.createExecutionQuery()
        .activityId("task")
        .processVariableValueEquals("aKey", "aValue")
        .processVariableValueEquals("aNewKey", "aNewVariable")
        .count();
    assertEquals(2, correlatedExecutions);

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testExecutionCorrelationByBusinessKey() {
    String businessKey = "aBusinessKey";
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", businessKey);
    runtimeService.correlateMessage("newInvoiceMessage", businessKey);

    // the execution that has been correlated should have advanced
    long correlatedExecutions = runtimeService.createExecutionQuery().activityId("task").count();
    assertEquals(1, correlatedExecutions);

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // use fluent builder //////////////////////

    processInstance = runtimeService.startProcessInstanceByKey("process", businessKey);
    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .processInstanceBusinessKey(businessKey)
      .correlate();

    // the execution that has been correlated should have advanced
    correlatedExecutions = runtimeService.createExecutionQuery().activityId("task").count();
    assertEquals(1, correlatedExecutions);

    runtimeService.deleteProcessInstance(processInstance.getId(), null);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testExecutionCorrelationByBusinessKeyUsingFluentCorrelateAll() {
    String businessKey = "aBusinessKey";
    runtimeService.startProcessInstanceByKey("process", businessKey);
    runtimeService.startProcessInstanceByKey("process", businessKey);

    runtimeService
      .createMessageCorrelation("newInvoiceMessage")
      .processInstanceBusinessKey(businessKey)
      .correlateAll();

    // the executions that has been correlated should be in the task
    long correlatedExecutions = runtimeService.createExecutionQuery().activityId("task").count();
    assertEquals(2, correlatedExecutions);

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testExecutionCorrelationByBusinessKeyWithVariables() {
    String businessKey = "aBusinessKey";
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", businessKey);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    runtimeService.correlateMessage("newInvoiceMessage", businessKey, variables);

    // the execution that has been correlated should have advanced
    long correlatedExecutions = runtimeService.createExecutionQuery()
        .processVariableValueEquals("aKey", "aValue").count();
    assertEquals(1, correlatedExecutions);

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // use fluent builder /////////////////////////

    processInstance = runtimeService.startProcessInstanceByKey("process", businessKey);

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .processInstanceBusinessKey(businessKey)
      .setVariable("aKey", "aValue")
      .correlate();

    // the execution that has been correlated should have advanced
    correlatedExecutions = runtimeService.createExecutionQuery()
        .processVariableValueEquals("aKey", "aValue").count();
    assertEquals(1, correlatedExecutions);

    runtimeService.deleteProcessInstance(processInstance.getId(), null);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testExecutionCorrelationByBusinessKeyWithVariablesUsingFluentCorrelateAll() {
    String businessKey = "aBusinessKey";

    runtimeService.startProcessInstanceByKey("process", businessKey);
    runtimeService.startProcessInstanceByKey("process", businessKey);

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .processInstanceBusinessKey(businessKey)
      .setVariable("aKey", "aValue")
      .correlateAll();

    // the executions that has been correlated should have advanced
    long correlatedExecutions = runtimeService
        .createExecutionQuery()
        .processVariableValueEquals("aKey", "aValue")
        .count();
    assertEquals(2, correlatedExecutions);

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testExecutionCorrelationSetSerializedVariableValue() throws IOException, ClassNotFoundException {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

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

    // but it can be set as a variable:
    runtimeService
      .createMessageCorrelation("newInvoiceMessage")
      .setVariable("var",
          Variables
            .serializedObjectValue(serializedObject)
            .objectTypeName(FailingJavaSerializable.class.getName())
            .serializationDataFormat(SerializationDataFormats.JAVA)
            .create())
      .correlate();

    // then
    ObjectValue variableTyped = runtimeService.getVariableTyped(processInstance.getId(), "var", false);
    assertNotNull(variableTyped);
    assertFalse(variableTyped.isDeserialized());
    assertEquals(serializedObject, variableTyped.getValueSerialized());
    assertEquals(FailingJavaSerializable.class.getName(), variableTyped.getObjectTypeName());
    assertEquals(SerializationDataFormats.JAVA.getName(), variableTyped.getSerializationDataFormat());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testExecutionCorrelationSetSerializedVariableValues() throws IOException, ClassNotFoundException {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

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

    // but it can be set as a variable:
    runtimeService
      .createMessageCorrelation("newInvoiceMessage")
      .setVariables(
          Variables.createVariables().putValueTyped("var",
            Variables
              .serializedObjectValue(serializedObject)
              .objectTypeName(FailingJavaSerializable.class.getName())
              .serializationDataFormat(SerializationDataFormats.JAVA)
              .create()))
      .correlate();

    // then
    ObjectValue variableTyped = runtimeService.getVariableTyped(processInstance.getId(), "var", false);
    assertNotNull(variableTyped);
    assertFalse(variableTyped.isDeserialized());
    assertEquals(serializedObject, variableTyped.getValueSerialized());
    assertEquals(FailingJavaSerializable.class.getName(), variableTyped.getObjectTypeName());
    assertEquals(SerializationDataFormats.JAVA.getName(), variableTyped.getSerializationDataFormat());
  }

  @Deployment
  public void testMessageStartEventCorrelation() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    runtimeService.correlateMessage("newInvoiceMessage", new HashMap<String, Object>(), variables);

    long instances = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("aKey", "aValue").count();
    assertEquals(1, instances);

    // fluent builder ////////////////

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariable("aKey", "aValue")
      .correlate();

    instances = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("aKey", "aValue").count();
    assertEquals(2, instances);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  public void testMessageStartEventCorrelationUsingFluentCorrelateAll() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariable("aKey", "aValue")
      .correlateAll();

    long instances = runtimeService
        .createProcessInstanceQuery()
        .processDefinitionKey("messageStartEvent")
        .variableValueEquals("aKey", "aValue")
        .count();
    assertEquals(1, instances);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml"})
  public void testMessageStartEventCorrelationBusKey() {
    final String businessKey = "aBusinessKey";

    runtimeService.correlateMessage("newInvoiceMessage", businessKey);

    // assert that the business key is set correctly
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(businessKey, processInstance.getBusinessKey());

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // fluent builder ////////////////

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .processInstanceBusinessKey(businessKey)
      .correlate();

    // assert that the business key is set correctly
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(businessKey, processInstance.getBusinessKey());

    runtimeService.deleteProcessInstance(processInstance.getId(), null);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml"})
  public void testMessageStartEventCorrelationBusKeyUsingFluentCorrelateAll() {
    final String businessKey = "aBusinessKey";

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .processInstanceBusinessKey(businessKey)
      .correlateAll();

    // assert that the business key is set correctly
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(businessKey, processInstance.getBusinessKey());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  public void testMessageStartEventCorrelationSetSerializedVariableValue() throws IOException, ClassNotFoundException {

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

    // but it can be set as a variable:
    runtimeService
      .createMessageCorrelation("newInvoiceMessage")
      .setVariable("var",
          Variables
            .serializedObjectValue(serializedObject)
            .objectTypeName(FailingJavaSerializable.class.getName())
            .serializationDataFormat(SerializationDataFormats.JAVA)
            .create())
      .correlate();

    // then
    ProcessInstance startedInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(startedInstance);

    ObjectValue variableTyped = runtimeService.getVariableTyped(startedInstance.getId(), "var", false);
    assertNotNull(variableTyped);
    assertFalse(variableTyped.isDeserialized());
    assertEquals(serializedObject, variableTyped.getValueSerialized());
    assertEquals(FailingJavaSerializable.class.getName(), variableTyped.getObjectTypeName());
    assertEquals(SerializationDataFormats.JAVA.getName(), variableTyped.getSerializationDataFormat());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  public void testMessageStartEventCorrelationSetSerializedVariableValues() throws IOException, ClassNotFoundException {

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

    // but it can be set as a variable:
    runtimeService
      .createMessageCorrelation("newInvoiceMessage")
      .setVariables(
          Variables.createVariables().putValueTyped("var",
            Variables
              .serializedObjectValue(serializedObject)
              .objectTypeName(FailingJavaSerializable.class.getName())
              .serializationDataFormat(SerializationDataFormats.JAVA)
              .create()))
      .correlate();

    // then
    ProcessInstance startedInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(startedInstance);

    ObjectValue variableTyped = runtimeService.getVariableTyped(startedInstance.getId(), "var", false);
    assertNotNull(variableTyped);
    assertFalse(variableTyped.isDeserialized());
    assertEquals(serializedObject, variableTyped.getValueSerialized());
    assertEquals(FailingJavaSerializable.class.getName(), variableTyped.getObjectTypeName());
    assertEquals(SerializationDataFormats.JAVA.getName(), variableTyped.getSerializationDataFormat());
  }

  /**
   * this test assures the right start event is selected
   */
  @Deployment
  public void testMultipleMessageStartEventsCorrelation() {

    runtimeService.correlateMessage("someMessage");
    // verify the right start event was selected:
    Task task = taskService.createTaskQuery().taskDefinitionKey("task1").singleResult();
    assertNotNull(task);
    assertNull(taskService.createTaskQuery().taskDefinitionKey("task2").singleResult());
    taskService.complete(task.getId());

    runtimeService.correlateMessage("someOtherMessage");
    // verify the right start event was selected:
    task = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult();
    assertNotNull(task);
    assertNull(taskService.createTaskQuery().taskDefinitionKey("task1").singleResult());
    taskService.complete(task.getId());

    // fluent builder //////////////////////////

    runtimeService.createMessageCorrelation("someMessage").correlate();
    // verify the right start event was selected:
    task = taskService.createTaskQuery().taskDefinitionKey("task1").singleResult();
    assertNotNull(task);
    assertNull(taskService.createTaskQuery().taskDefinitionKey("task2").singleResult());
    taskService.complete(task.getId());

    runtimeService.createMessageCorrelation("someOtherMessage").correlate();
    // verify the right start event was selected:
    task = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult();
    assertNotNull(task);
    assertNull(taskService.createTaskQuery().taskDefinitionKey("task1").singleResult());
    taskService.complete(task.getId());
  }

  /**
   * this test assures the right start event is selected
   */
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMultipleMessageStartEventsCorrelation.bpmn20.xml"})
  public void testMultipleMessageStartEventsCorrelationUsingFluentCorrelateAll() {
    runtimeService.createMessageCorrelation("someMessage").correlateAll();
    // verify the right start event was selected:
    Task task = taskService.createTaskQuery().taskDefinitionKey("task1").singleResult();
    assertNotNull(task);
    assertNull(taskService.createTaskQuery().taskDefinitionKey("task2").singleResult());
    taskService.complete(task.getId());

    runtimeService.createMessageCorrelation("someOtherMessage").correlateAll();
    // verify the right start event was selected:
    task = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult();
    assertNotNull(task);
    assertNull(taskService.createTaskQuery().taskDefinitionKey("task1").singleResult());
    taskService.complete(task.getId());
  }

  @Deployment
  public void testMatchingStartEventAndExecution() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    assertNotNull(runtimeService.createExecutionQuery().messageEventSubscriptionName("newInvoiceMessage").singleResult());
    // correlate message -> this will trigger the execution
    runtimeService.correlateMessage("newInvoiceMessage");
    assertNull(runtimeService.createExecutionQuery().messageEventSubscriptionName("newInvoiceMessage").singleResult());

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // fluent builder //////////////////////

    processInstance = runtimeService.startProcessInstanceByKey("process");

    assertNotNull(runtimeService.createExecutionQuery().messageEventSubscriptionName("newInvoiceMessage").singleResult());
    // correlate message -> this will trigger the execution
    runtimeService.createMessageCorrelation("newInvoiceMessage").correlate();
    assertNull(runtimeService.createExecutionQuery().messageEventSubscriptionName("newInvoiceMessage").singleResult());

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMatchingStartEventAndExecution.bpmn20.xml"})
  public void testMatchingStartEventAndExecutionUsingFluentCorrelateAll() {
    runtimeService.startProcessInstanceByKey("process");
    runtimeService.startProcessInstanceByKey("process");

    assertEquals(2, runtimeService.createExecutionQuery().messageEventSubscriptionName("newInvoiceMessage").count());
    // correlate message -> this will trigger the executions AND start a new process instance
    runtimeService.createMessageCorrelation("newInvoiceMessage").correlateAll();
    assertNotNull(runtimeService.createExecutionQuery().messageEventSubscriptionName("newInvoiceMessage").singleResult());

    assertEquals(3, runtimeService.createProcessInstanceQuery().count());
  }

  public void testMessageStartEventCorrelationWithNonMatchingDefinition() {
    try {
      runtimeService.correlateMessage("aMessageName");
      fail("Expect an Exception");
    } catch (MismatchingMessageCorrelationException e) {
      assertTextPresent("Cannot correlate message", e.getMessage());
    }

    // fluent builder //////////////////

    try {
      runtimeService.createMessageCorrelation("aMessageName").correlate();
      fail("Expect an Exception");
    } catch (MismatchingMessageCorrelationException e) {
      assertTextPresent("Cannot correlate message", e.getMessage());
    }

    // fluent builder with multiple correlation //////////////////
    // This should not fail
    runtimeService.createMessageCorrelation("aMessageName").correlateAll();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationByBusinessKeyAndVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", "aBusinessKey", variables);

    variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", "anotherBusinessKey", variables);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    correlationKeys.put("aKey", "aValue");

    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("aProcessVariable", "aVariableValue");
    runtimeService.correlateMessage(messageName, "aBusinessKey", correlationKeys, processVariables);

    Execution correlatedExecution = runtimeService.createExecutionQuery()
        .activityId("task").processVariableValueEquals("aProcessVariable", "aVariableValue")
        .singleResult();

    assertNotNull(correlatedExecution);

    ProcessInstance correlatedProcessInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(correlatedExecution.getProcessInstanceId()).singleResult();

    assertEquals("aBusinessKey", correlatedProcessInstance.getBusinessKey());

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // fluent builder /////////////////////////////

    variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    processInstance = runtimeService.startProcessInstanceByKey("process", "aBusinessKey", variables);

    runtimeService.createMessageCorrelation(messageName)
      .processInstanceBusinessKey("aBusinessKey")
      .processInstanceVariableEquals("aKey", "aValue")
      .setVariable("aProcessVariable", "aVariableValue")
      .correlate();

    correlatedExecution = runtimeService.createExecutionQuery()
        .activityId("task").processVariableValueEquals("aProcessVariable", "aVariableValue")
        .singleResult();

    assertNotNull(correlatedExecution);

    correlatedProcessInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(correlatedExecution.getProcessInstanceId()).singleResult();

    assertEquals("aBusinessKey", correlatedProcessInstance.getBusinessKey());

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationByBusinessKeyAndVariablesUsingFluentCorrelateAll() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", "aBusinessKey", variables);
    runtimeService.startProcessInstanceByKey("process", "aBusinessKey", variables);

    String messageName = "newInvoiceMessage";
    runtimeService.createMessageCorrelation(messageName)
      .processInstanceBusinessKey("aBusinessKey")
      .processInstanceVariableEquals("aKey", "aValue")
      .setVariable("aProcessVariable", "aVariableValue")
      .correlateAll();

    List<Execution> correlatedExecutions = runtimeService
        .createExecutionQuery()
        .activityId("task")
        .processVariableValueEquals("aProcessVariable", "aVariableValue")
        .list();

    assertEquals(2, correlatedExecutions.size());

    // Instance 1
    Execution correlatedExecution = correlatedExecutions.get(0);
    ProcessInstance correlatedProcessInstance = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(correlatedExecution.getProcessInstanceId())
        .singleResult();

    assertEquals("aBusinessKey", correlatedProcessInstance.getBusinessKey());

    // Instance 2
    correlatedExecution = correlatedExecutions.get(1);
    correlatedProcessInstance = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(correlatedExecution.getProcessInstanceId())
        .singleResult();

    assertEquals("aBusinessKey", correlatedProcessInstance.getBusinessKey());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationByProcessInstanceId() {

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");

    // correlation with only the name is ambiguous:
    try {
      runtimeService.createMessageCorrelation("aMessageName").correlate();
      fail("Expect an Exception");
    } catch (MismatchingMessageCorrelationException e) {
      assertTextPresent("Cannot correlate message", e.getMessage());
    }

    // use process instance id as well
    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .processInstanceId(processInstance1.getId())
      .correlate();

    Execution correlatedExecution = runtimeService.createExecutionQuery()
        .activityId("task")
        .processInstanceId(processInstance1.getId())
        .singleResult();
    assertNotNull(correlatedExecution);

    Execution uncorrelatedExecution = runtimeService.createExecutionQuery()
        .activityId("task")
        .processInstanceId(processInstance2.getId())
        .singleResult();
    assertNull(uncorrelatedExecution);

    runtimeService.deleteProcessInstance(processInstance1.getId(), null);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationByProcessInstanceIdUsingFluentCorrelateAll() {
    // correlate by name
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");

    // correlation with only the name is ambiguous:
    runtimeService
      .createMessageCorrelation("aMessageName")
      .correlateAll();

    assertEquals(0, runtimeService.createExecutionQuery().activityId("task").count());

    // correlate process instance id
    processInstance1 = runtimeService.startProcessInstanceByKey("process");

    processInstance2 = runtimeService.startProcessInstanceByKey("process");

    // use process instance id as well
    runtimeService
      .createMessageCorrelation("newInvoiceMessage")
      .processInstanceId(processInstance1.getId())
      .correlateAll();

    Execution correlatedExecution = runtimeService
        .createExecutionQuery()
        .activityId("task")
        .processInstanceId(processInstance1.getId())
        .singleResult();
    assertNotNull(correlatedExecution);

    Execution uncorrelatedExecution = runtimeService
        .createExecutionQuery()
        .activityId("task")
        .processInstanceId(processInstance2.getId())
        .singleResult();
    assertNull(uncorrelatedExecution);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationByBusinessKeyAndNullVariableUsingFluentCorrelateAll() {
    runtimeService.startProcessInstanceByKey("process", "aBusinessKey");

    String messageName = "newInvoiceMessage";

    try {
      runtimeService.createMessageCorrelation(messageName)
        .processInstanceBusinessKey("aBusinessKey")
        .setVariable(null, "aVariableValue")
        .correlateAll();
      fail("Variable name is null");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessEngineException);
      assertTextPresent("null", e.getMessage());
    }

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationByBusinessKeyAndNullVariableEqualsUsingFluentCorrelateAll() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", "bar");
    runtimeService.startProcessInstanceByKey("process", "aBusinessKey", variables);

    String messageName = "newInvoiceMessage";

    try {
      runtimeService.createMessageCorrelation(messageName)
        .processInstanceBusinessKey("aBusinessKey")
        .processInstanceVariableEquals(null, "bar")
        .correlateAll();
      fail("Variable name is null");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessEngineException);
      assertTextPresent("null", e.getMessage());
    }

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationByBusinessKeyAndNullVariablesUsingFluentCorrelateAll() {
    runtimeService.startProcessInstanceByKey("process", "aBusinessKey");

    String messageName = "newInvoiceMessage";

    runtimeService.createMessageCorrelation(messageName)
      .processInstanceBusinessKey("aBusinessKey")
      .setVariables(null)
      .setVariable("foo", "bar")
      .correlateAll();

    List<Execution> correlatedExecutions = runtimeService
      .createExecutionQuery()
      .activityId("task")
      .processVariableValueEquals("foo", "bar")
      .list();

    assertFalse(correlatedExecutions.isEmpty());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationByVariablesOnly() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variable", "value1");
    runtimeService.startProcessInstanceByKey("process", variables);

    variables.put("variable", "value2");
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process", variables);

    runtimeService.correlateMessage(null, variables);

    List<Execution> correlatedExecutions = runtimeService
      .createExecutionQuery()
      .activityId("task")
      .list();

    assertEquals(1, correlatedExecutions.size());
    assertEquals(instance.getId(), correlatedExecutions.get(0).getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationByBusinessKey() {
    runtimeService.startProcessInstanceByKey("process", "businessKey1");
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process", "businessKey2");

    runtimeService.correlateMessage(null, "businessKey2");

    List<Execution> correlatedExecutions = runtimeService
      .createExecutionQuery()
      .activityId("task")
      .list();

    assertEquals(1, correlatedExecutions.size());
    assertEquals(instance.getId(), correlatedExecutions.get(0).getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationByProcessInstanceIdOnly() {
    runtimeService.startProcessInstanceByKey("process");
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");

    runtimeService
      .createMessageCorrelation(null)
      .processInstanceId(instance.getId())
      .correlate();

    List<Execution> correlatedExecutions = runtimeService
      .createExecutionQuery()
      .activityId("task")
      .list();

    assertEquals(1, correlatedExecutions.size());
    assertEquals(instance.getId(), correlatedExecutions.get(0).getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationWithoutMessageNameFluent() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variable", "value1");
    runtimeService.startProcessInstanceByKey("process", variables);

    variables.put("variable", "value2");
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process", variables);

    runtimeService.createMessageCorrelation(null)
      .processInstanceVariableEquals("variable", "value2")
      .correlate();

    List<Execution> correlatedExecutions = runtimeService
      .createExecutionQuery()
      .activityId("task")
      .list();

    assertEquals(1, correlatedExecutions.size());
    assertEquals(instance.getId(), correlatedExecutions.get(0).getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCorrelateAllWithoutMessage.bpmn20.xml"})
  public void testCorrelateAllWithoutMessage() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variable", "value1");
    runtimeService.startProcessInstanceByKey("process", variables);
    runtimeService.startProcessInstanceByKey("secondProcess", variables);

    variables.put("variable", "value2");
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("process", variables);
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("secondProcess", variables);

    runtimeService.createMessageCorrelation(null)
      .processInstanceVariableEquals("variable", "value2")
      .correlateAll();

    List<Execution> correlatedExecutions = runtimeService
      .createExecutionQuery()
      .activityId("task")
      .orderByProcessDefinitionKey()
      .asc()
      .list();

    assertEquals(2, correlatedExecutions.size());
    assertEquals(instance1.getId(), correlatedExecutions.get(0).getId());
    assertEquals(instance2.getId(), correlatedExecutions.get(1).getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  public void testCorrelationWithoutMessageDoesNotMatchStartEvent() {
    try {
      runtimeService.createMessageCorrelation(null)
        .processInstanceVariableEquals("variable", "value2")
        .correlate();
      fail("exception expected");
    } catch (MismatchingMessageCorrelationException e) {
      // expected
    }

    List<Execution> correlatedExecutions = runtimeService
      .createExecutionQuery()
      .activityId("task")
      .list();

    assertTrue(correlatedExecutions.isEmpty());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testCorrelationWithoutCorrelationPropertiesFails() {

    runtimeService.startProcessInstanceByKey("process");

    try {
      runtimeService.createMessageCorrelation(null)
        .correlate();
      fail("expected exception");
    } catch (NullValueException e) {
      // expected
    }

    try {
      runtimeService.correlateMessage(null);
      fail("expected exception");
    } catch (NullValueException e) {
      // expected
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/twoBoundaryEventSubscriptions.bpmn20.xml")
  public void testCorrelationToExecutionWithMultipleSubscriptionsFails() {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");

    try {
      runtimeService.createMessageCorrelation(null)
        .processInstanceId(instance.getId())
        .correlate();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // note: this does not expect a MismatchingCorrelationException since the exception
      // is only raised in the MessageEventReceivedCmd. Otherwise, this would require explicit checking in the
      // correlation handler that a matched execution without message name has exactly one message (now it checks for
      // at least one message)

      // expected
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testSuspendedProcessInstance() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    String processInstance = runtimeService.startProcessInstanceByKey("process", variables).getId();

    // suspend process instance
    runtimeService.suspendProcessInstanceById(processInstance);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    correlationKeys.put("aKey", "aValue");

    try {
      runtimeService.correlateMessage(messageName, correlationKeys);
      fail("It should not be possible to correlate a message to a suspended process instance.");
    } catch (MismatchingMessageCorrelationException e) {
      // expected
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  public void testOneMatchingAndOneSuspendedProcessInstance() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    String firstProcessInstance = runtimeService.startProcessInstanceByKey("process", variables).getId();

    variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    String secondProcessInstance = runtimeService.startProcessInstanceByKey("process", variables).getId();

    // suspend second process instance
    runtimeService.suspendProcessInstanceById(secondProcessInstance);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    correlationKeys.put("aKey", "aValue");

    Map<String, Object> messagePayload = new HashMap<String, Object>();
    messagePayload.put("aNewKey", "aNewVariable");

    runtimeService.correlateMessage(messageName, correlationKeys, messagePayload);

    // there exists an uncorrelated executions (the second process instance)
    long uncorrelatedExecutions = runtimeService
        .createExecutionQuery()
        .processInstanceId(secondProcessInstance)
        .processVariableValueEquals("aKey", "aValue")
        .messageEventSubscriptionName("newInvoiceMessage")
        .count();
    assertEquals(1, uncorrelatedExecutions);

    // the execution that has been correlated should have advanced
    long correlatedExecutions = runtimeService
        .createExecutionQuery()
        .processInstanceId(firstProcessInstance)
        .activityId("task")
        .processVariableValueEquals("aKey", "aValue")
        .processVariableValueEquals("aNewKey", "aNewVariable")
        .count();
    assertEquals(1, correlatedExecutions);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  public void testSuspendedProcessDefinition() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    repositoryService.suspendProcessDefinitionById(processDefinitionId);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");

    try {
      runtimeService.correlateMessage("newInvoiceMessage", new HashMap<String, Object>(), variables);
      fail("It should not be possible to correlate a message to a suspended process definition.");
    } catch (MismatchingMessageCorrelationException e) {
      // expected
    }
  }

}
