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
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.variables.FailingJavaSerializable;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 */
public class MessageCorrelationTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setJavaSerializationFormatEnabled(true));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;
  private TaskService taskService;
  private RepositoryService repositoryService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();
  }

  @Deployment
  @Test
  public void testCatchingMessageEventCorrelation() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("aKey", "aValue");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    variables = new HashMap<>();
    variables.put("aKey", "anotherValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<>();
    correlationKeys.put("aKey", "aValue");
    Map<String, Object> messagePayload = new HashMap<>();
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

    variables = new HashMap<>();
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
  @Test
  public void testOneMatchinProcessInstanceUsingFluentCorrelateAll() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    variables = new HashMap<>();
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
  @Test
  public void testTwoMatchingProcessInstancesCorrelation() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    variables = new HashMap<>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<>();
    correlationKeys.put("aKey", "aValue");

    try {
      runtimeService.correlateMessage(messageName, correlationKeys);
      fail("Expected an Exception");
    } catch (MismatchingMessageCorrelationException e) {
      testRule.assertTextPresent("2 executions match the correlation keys", e.getMessage());
    }

    // fluent builder fails as well
    try {
      runtimeService.createMessageCorrelation(messageName)
        .processInstanceVariableEquals("aKey", "aValue")
        .correlate();
      fail("Expected an Exception");
    } catch (MismatchingMessageCorrelationException e) {
      testRule.assertTextPresent("2 executions match the correlation keys", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  @Test
  public void testTwoMatchingProcessInstancesUsingFluentCorrelateAll() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    variables = new HashMap<>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", variables);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<>();
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
  @Test
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
  @Test
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
  @Test
  public void testMessageCorrelateAllResultListWithResultTypeExecution() {
    //given
    ProcessInstance procInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance procInstance2 = runtimeService.startProcessInstanceByKey("process");

    //when correlated all with result
    List<MessageCorrelationResult> resultList = runtimeService.createMessageCorrelation("newInvoiceMessage")
                                                              .correlateAllWithResult();

    assertEquals(2, resultList.size());
    //then result should contains executions on which messages was correlated
    for (MessageCorrelationResult result : resultList) {
      assertNotNull(result);
      assertEquals(MessageCorrelationResultType.Execution, result.getResultType());
      assertTrue(procInstance1.getId().equalsIgnoreCase(result.getExecution().getProcessInstanceId())
                || procInstance2.getId().equalsIgnoreCase(result.getExecution().getProcessInstanceId())
      );
      ExecutionEntity entity = (ExecutionEntity) result.getExecution();
      assertEquals("messageCatch", entity.getActivityId());
    }
  }


  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testMessageCorrelateAllResultListWithResultTypeProcessDefinition() {
    //when correlated all with result
    List<MessageCorrelationResult> resultList = runtimeService.createMessageCorrelation("newInvoiceMessage")
                                                              .correlateAllWithResult();

    assertEquals(1, resultList.size());
    //then result should contains process definitions and start event activity ids on which messages was correlated
    for (MessageCorrelationResult result : resultList) {
      checkProcessDefinitionMessageCorrelationResult(result, "theStart", "messageStartEvent");
    }
  }


  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  @Test
  public void testExecutionCorrelationByBusinessKeyWithVariables() {
    String businessKey = "aBusinessKey";
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", businessKey);

    Map<String, Object> variables = new HashMap<>();
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
  @Test
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
  @Test
  public void testExecutionCorrelationSetSerializedVariableValue() throws IOException, ClassNotFoundException {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    FailingJavaSerializable javaSerializable = new FailingJavaSerializable("foo");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(javaSerializable);
    String serializedObject = StringUtil.fromBytes(Base64.encodeBase64(baos.toByteArray()), engineRule.getProcessEngine());

    // then it is not possible to deserialize the object
    try {
      new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    } catch (RuntimeException e) {
      testRule.assertTextPresent("Exception while deserializing object.", e.getMessage());
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
  @Test
  public void testExecutionCorrelationSetSerializedVariableValues() throws IOException, ClassNotFoundException {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    FailingJavaSerializable javaSerializable = new FailingJavaSerializable("foo");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(javaSerializable);
    String serializedObject = StringUtil.fromBytes(Base64.encodeBase64(baos.toByteArray()), engineRule.getProcessEngine());

    // then it is not possible to deserialize the object
    try {
      new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    } catch (RuntimeException e) {
      testRule.assertTextPresent("Exception while deserializing object.", e.getMessage());
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
  @Test
  public void testMessageStartEventCorrelation() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("aKey", "aValue");

    runtimeService.correlateMessage("newInvoiceMessage", new HashMap<String, Object>(), variables);

    long instances = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("aKey", "aValue").count();
    assertEquals(1, instances);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testMessageStartEventCorrelationUsingFluentCorrelateStartMessage() {

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariable("aKey", "aValue")
      .correlateStartMessage();

    long instances = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("aKey", "aValue").count();
    assertEquals(1, instances);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testMessageStartEventCorrelationUsingFluentCorrelateSingle() {

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariable("aKey", "aValue")
      .correlate();

    long instances = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("aKey", "aValue").count();
    assertEquals(1, instances);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testMessageStartEventCorrelationUsingFluentCorrelateAll() {

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
  @Test
  public void testMessageStartEventCorrelationWithBusinessKey() {
    final String businessKey = "aBusinessKey";

    runtimeService.correlateMessage("newInvoiceMessage", businessKey);

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    assertEquals(businessKey, processInstance.getBusinessKey());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml"})
  @Test
  public void testMessageStartEventCorrelationWithBusinessKeyUsingFluentCorrelateStartMessage() {
    final String businessKey = "aBusinessKey";

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .processInstanceBusinessKey(businessKey)
      .correlateStartMessage();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    assertEquals(businessKey, processInstance.getBusinessKey());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml"})
  @Test
  public void testMessageStartEventCorrelationWithBusinessKeyUsingFluentCorrelateSingle() {
    final String businessKey = "aBusinessKey";

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .processInstanceBusinessKey(businessKey)
      .correlate();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    assertEquals(businessKey, processInstance.getBusinessKey());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml"})
  @Test
  public void testMessageStartEventCorrelationWithBusinessKeyUsingFluentCorrelateAll() {
    final String businessKey = "aBusinessKey";

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .processInstanceBusinessKey(businessKey)
      .correlateAll();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    assertEquals(businessKey, processInstance.getBusinessKey());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testMessageStartEventCorrelationSetSerializedVariableValue() throws IOException, ClassNotFoundException {

    // when
    FailingJavaSerializable javaSerializable = new FailingJavaSerializable("foo");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(javaSerializable);
    String serializedObject = StringUtil.fromBytes(Base64.encodeBase64(baos.toByteArray()), engineRule.getProcessEngine());

    // then it is not possible to deserialize the object
    try {
      new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    } catch (RuntimeException e) {
      testRule.assertTextPresent("Exception while deserializing object.", e.getMessage());
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
  @Test
  public void testMessageStartEventCorrelationSetSerializedVariableValues() throws IOException, ClassNotFoundException {

    // when
    FailingJavaSerializable javaSerializable = new FailingJavaSerializable("foo");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(javaSerializable);
    String serializedObject = StringUtil.fromBytes(Base64.encodeBase64(baos.toByteArray()), engineRule.getProcessEngine());

    // then it is not possible to deserialize the object
    try {
      new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    } catch (RuntimeException e) {
      testRule.assertTextPresent("Exception while deserializing object.", e.getMessage());
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

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testMessageStartEventCorrelationWithVariablesUsingFluentCorrelateStartMessage() {

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariables(Variables.createVariables()
          .putValue("var1", "a")
          .putValue("var2", "b"))
      .correlateStartMessage();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("var1", "a")
        .variableValueEquals("var2", "b");
    assertEquals(1, query.count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testMessageStartEventCorrelationWithVariablesUsingFluentCorrelateSingleMessage() {

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariables(Variables.createVariables()
          .putValue("var1", "a")
          .putValue("var2", "b"))
      .correlate();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("var1", "a")
        .variableValueEquals("var2", "b");
    assertEquals(1, query.count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testMessageStartEventCorrelationWithVariablesUsingFluentCorrelateAll() {

    runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariables(Variables.createVariables()
          .putValue("var1", "a")
          .putValue("var2", "b"))
      .correlateAll();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("var1", "a")
        .variableValueEquals("var2", "b");
    assertEquals(1, query.count());
  }

  /**
   * this test assures the right start event is selected
   */
  @Deployment
  @Test
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
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMultipleMessageStartEventsCorrelation.bpmn20.xml"})
  @Test
  public void testMultipleMessageStartEventsCorrelationUsingFluentCorrelateStartMessage() {

    runtimeService.createMessageCorrelation("someMessage").correlateStartMessage();
    // verify the right start event was selected:
    Task task = taskService.createTaskQuery().taskDefinitionKey("task1").singleResult();
    assertNotNull(task);
    assertNull(taskService.createTaskQuery().taskDefinitionKey("task2").singleResult());
    taskService.complete(task.getId());

    runtimeService.createMessageCorrelation("someOtherMessage").correlateStartMessage();
    // verify the right start event was selected:
    task = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult();
    assertNotNull(task);
    assertNull(taskService.createTaskQuery().taskDefinitionKey("task1").singleResult());
    taskService.complete(task.getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMultipleMessageStartEventsCorrelation.bpmn20.xml"})
  @Test
  public void testMultipleMessageStartEventsCorrelationUsingFluentCorrelateSingle() {

    runtimeService.createMessageCorrelation("someMessage").correlate();
    // verify the right start event was selected:
    Task task = taskService.createTaskQuery().taskDefinitionKey("task1").singleResult();
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
  @Test
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
  @Test
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
  @Test
  public void testMessageCorrelationResultWithResultTypeProcessDefinition() {
    //given
    String msgName = "newInvoiceMessage";

    //when
    //correlate message with result
    MessageCorrelationResult result = runtimeService.createMessageCorrelation(msgName).correlateWithResult();

    //then
    //message correlation result contains information from receiver
    checkProcessDefinitionMessageCorrelationResult(result, "theStart", "process");
  }

  protected void checkProcessDefinitionMessageCorrelationResult(MessageCorrelationResult result, String startActivityId, String processDefinitionId) {
    assertNotNull(result);
    assertNotNull(result.getProcessInstance().getId());
    assertEquals(MessageCorrelationResultType.ProcessDefinition, result.getResultType());
    assertTrue(result.getProcessInstance().getProcessDefinitionId().contains(processDefinitionId));
  }


  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMatchingStartEventAndExecution.bpmn20.xml"})
  @Test
  public void testMessageCorrelationResultWithResultTypeExecution() {
    //given
    String msgName = "newInvoiceMessage";
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    assertNotNull(runtimeService.createExecutionQuery().messageEventSubscriptionName(msgName).singleResult());

    //when
    //correlate message with result
    MessageCorrelationResult result = runtimeService.createMessageCorrelation(msgName).correlateWithResult();

    //then
    //message correlation result contains information from receiver
    checkExecutionMessageCorrelationResult(result, processInstance, "messageCatch");
  }

  protected void checkExecutionMessageCorrelationResult(MessageCorrelationResult result, ProcessInstance processInstance, String activityId) {
    assertNotNull(result);
    assertEquals(MessageCorrelationResultType.Execution, result.getResultType());
    assertEquals(processInstance.getId(), result.getExecution().getProcessInstanceId());
    ExecutionEntity entity = (ExecutionEntity) result.getExecution();
    assertEquals(activityId, entity.getActivityId());
  }


  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMatchingStartEventAndExecution.bpmn20.xml"})
  @Test
  public void testMatchingStartEventAndExecutionUsingFluentCorrelateAll() {
    runtimeService.startProcessInstanceByKey("process");
    runtimeService.startProcessInstanceByKey("process");

    assertEquals(2, runtimeService.createExecutionQuery().messageEventSubscriptionName("newInvoiceMessage").count());
    // correlate message -> this will trigger the executions AND start a new process instance
    runtimeService.createMessageCorrelation("newInvoiceMessage").correlateAll();
    assertNotNull(runtimeService.createExecutionQuery().messageEventSubscriptionName("newInvoiceMessage").singleResult());

    assertEquals(3, runtimeService.createProcessInstanceQuery().count());
  }


  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMatchingStartEventAndExecution.bpmn20.xml"})
  @Test
  public void testMatchingStartEventAndExecutionCorrelateAllWithResult() {
    //given
    ProcessInstance procInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance procInstance2 = runtimeService.startProcessInstanceByKey("process");

    //when correlated all with result
    List<MessageCorrelationResult> resultList = runtimeService.createMessageCorrelation("newInvoiceMessage")
            .correlateAllWithResult();

    //then result should contains three entries
    //two of type execution und one of type process definition
    assertEquals(3, resultList.size());
    int executionResultCount = 0;
    int procDefResultCount = 0;
    for (MessageCorrelationResult result : resultList) {
      if (result.getResultType().equals(MessageCorrelationResultType.Execution)) {
        assertNotNull(result);
        assertEquals(MessageCorrelationResultType.Execution, result.getResultType());
        assertTrue(procInstance1.getId().equalsIgnoreCase(result.getExecution().getProcessInstanceId())
                || procInstance2.getId().equalsIgnoreCase(result.getExecution().getProcessInstanceId())
        );
        ExecutionEntity entity = (ExecutionEntity) result.getExecution();
        assertEquals("messageCatch", entity.getActivityId());
        executionResultCount++;
      } else {
        checkProcessDefinitionMessageCorrelationResult(result, "theStart", "process");
        procDefResultCount++;
      }
    }
    assertEquals(2, executionResultCount);
    assertEquals(1, procDefResultCount);
  }

  @Test
  public void testMessageStartEventCorrelationWithNonMatchingDefinition() {
    try {
      runtimeService.correlateMessage("aMessageName");
      fail("Expect an Exception");
    } catch (MismatchingMessageCorrelationException e) {
      testRule.assertTextPresent("Cannot correlate message", e.getMessage());
    }

    // fluent builder //////////////////

    try {
      runtimeService.createMessageCorrelation("aMessageName").correlate();
      fail("Expect an Exception");
    } catch (MismatchingMessageCorrelationException e) {
      testRule.assertTextPresent("Cannot correlate message", e.getMessage());
    }

    // fluent builder with multiple correlation //////////////////
    // This should not fail
    runtimeService.createMessageCorrelation("aMessageName").correlateAll();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  @Test
  public void testCorrelationByBusinessKeyAndVariables() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("aKey", "aValue");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", "aBusinessKey", variables);

    variables = new HashMap<>();
    variables.put("aKey", "aValue");
    runtimeService.startProcessInstanceByKey("process", "anotherBusinessKey", variables);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<>();
    correlationKeys.put("aKey", "aValue");

    Map<String, Object> processVariables = new HashMap<>();
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

    variables = new HashMap<>();
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
  @Test
  public void testCorrelationByBusinessKeyAndVariablesUsingFluentCorrelateAll() {
    Map<String, Object> variables = new HashMap<>();
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
  @Test
  public void testCorrelationByProcessInstanceId() {

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");

    // correlation with only the name is ambiguous:
    try {
      runtimeService.createMessageCorrelation("aMessageName").correlate();
      fail("Expect an Exception");
    } catch (MismatchingMessageCorrelationException e) {
      testRule.assertTextPresent("Cannot correlate message", e.getMessage());
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
  @Test
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
  @Test
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
      testRule.assertTextPresent("null", e.getMessage());
    }

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  @Test
  public void testCorrelationByBusinessKeyAndNullVariableEqualsUsingFluentCorrelateAll() {
    Map<String, Object> variables = new HashMap<>();
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
      testRule.assertTextPresent("null", e.getMessage());
    }

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  @Test
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
  @Test
  public void testCorrelationByVariablesOnly() {
    Map<String, Object> variables = new HashMap<>();
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
  @Test
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
  @Test
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
  @Test
  public void testCorrelationWithoutMessageNameFluent() {
    Map<String, Object> variables = new HashMap<>();
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
  @Test
  public void testCorrelateAllWithoutMessage() {
    Map<String, Object> variables = new HashMap<>();
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
  @Test
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
  @Test
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
  @Test
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
  @Test
  public void testSuspendedProcessInstance() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("aKey", "aValue");
    String processInstance = runtimeService.startProcessInstanceByKey("process", variables).getId();

    // suspend process instance
    runtimeService.suspendProcessInstanceById(processInstance);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<>();
    correlationKeys.put("aKey", "aValue");

    try {
      runtimeService.correlateMessage(messageName, correlationKeys);
      fail("It should not be possible to correlate a message to a suspended process instance.");
    } catch (MismatchingMessageCorrelationException e) {
      // expected
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  @Test
  public void testOneMatchingAndOneSuspendedProcessInstance() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("aKey", "aValue");
    String firstProcessInstance = runtimeService.startProcessInstanceByKey("process", variables).getId();

    variables = new HashMap<>();
    variables.put("aKey", "aValue");
    String secondProcessInstance = runtimeService.startProcessInstanceByKey("process", variables).getId();

    // suspend second process instance
    runtimeService.suspendProcessInstanceById(secondProcessInstance);

    String messageName = "newInvoiceMessage";
    Map<String, Object> correlationKeys = new HashMap<>();
    correlationKeys.put("aKey", "aValue");

    Map<String, Object> messagePayload = new HashMap<>();
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
  @Test
  public void testSuspendedProcessDefinition() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    repositoryService.suspendProcessDefinitionById(processDefinitionId);

    Map<String, Object> variables = new HashMap<>();
    variables.put("aKey", "aValue");

    try {
      runtimeService.correlateMessage("newInvoiceMessage", new HashMap<String, Object>(), variables);
      fail("It should not be possible to correlate a message to a suspended process definition.");
    } catch (MismatchingMessageCorrelationException e) {
      // expected
    }
  }

  @Test
  public void testCorrelateMessageStartEventWithProcessDefinitionId() {
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
          .message("a")
        .userTask()
        .endEvent()
        .done());

    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
          .message("b")
        .userTask()
        .endEvent()
        .done());

    ProcessDefinition firstProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(1).singleResult();
    ProcessDefinition secondProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(2).singleResult();

    runtimeService.createMessageCorrelation("a")
      .processDefinitionId(firstProcessDefinition.getId())
      .processInstanceBusinessKey("first")
      .correlateStartMessage();

    runtimeService.createMessageCorrelation("b")
      .processDefinitionId(secondProcessDefinition.getId())
      .processInstanceBusinessKey("second")
      .correlateStartMessage();

    assertEquals(1, runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey("first").processDefinitionId(firstProcessDefinition.getId()).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey("second").processDefinitionId(secondProcessDefinition.getId()).count());
  }

  @Test
  public void testFailCorrelateMessageStartEventWithWrongProcessDefinitionId() {
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
          .message("a")
        .userTask()
        .endEvent()
        .done());

    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
          .message("b")
        .userTask()
        .endEvent()
        .done());

    ProcessDefinition latestProcessDefinition = repositoryService.createProcessDefinitionQuery().latestVersion().singleResult();

    try {
      runtimeService.createMessageCorrelation("a")
        .processDefinitionId(latestProcessDefinition.getId())
        .correlateStartMessage();

      fail("expected exception");
    } catch (MismatchingMessageCorrelationException e){
      testRule.assertTextPresent("Cannot correlate message 'a'", e.getMessage());
    }
  }

  @Test
  public void testFailCorrelateMessageStartEventWithNonExistingProcessDefinitionId() {
    try {
      runtimeService.createMessageCorrelation("a")
        .processDefinitionId("not existing")
        .correlateStartMessage();

      fail("expected exception");
    } catch (ProcessEngineException e){
      testRule.assertTextPresent("no deployed process definition found", e.getMessage());
    }
  }

  @Test
  public void testFailCorrelateMessageWithProcessDefinitionId() {
    try {
      runtimeService.createMessageCorrelation("a")
        .processDefinitionId("id")
        .correlate();

      fail("expected exception");
    } catch (BadUserRequestException e){
      testRule.assertTextPresent("Cannot specify a process definition id", e.getMessage());
    }
  }

  @Test
  public void testFailCorrelateMessagesWithProcessDefinitionId() {
    try {
      runtimeService.createMessageCorrelation("a")
        .processDefinitionId("id")
        .correlateAll();

      fail("expected exception");
    } catch (BadUserRequestException e){
      testRule.assertTextPresent("Cannot specify a process definition id", e.getMessage());
    }
  }

  @Test
  public void testFailCorrelateMessageStartEventWithCorrelationVariable() {
    try {
      runtimeService.createMessageCorrelation("a")
        .processInstanceVariableEquals("var", "value")
        .correlateStartMessage();

      fail("expected exception");
    } catch (BadUserRequestException e){
      testRule.assertTextPresent("Cannot specify correlation variables ", e.getMessage());
    }
  }

  @Test
  public void testFailCorrelateMessageStartEventWithCorrelationVariables() {
    try {
      runtimeService.createMessageCorrelation("a")
        .processInstanceVariablesEqual(Variables
              .createVariables()
              .putValue("var1", "b")
              .putValue("var2", "c"))
        .correlateStartMessage();

      fail("expected exception");
    } catch (BadUserRequestException e){
      testRule.assertTextPresent("Cannot specify correlation variables ", e.getMessage());
    }
  }

  @Test
  public void testCorrelationWithResultBySettingLocalVariables() {
    // given
    String outputVarName = "localVar";
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
          .intermediateCatchEvent("message_1")
            .message("1")
            .camundaOutputParameter(outputVarName, "${testLocalVar}")
          .userTask("UserTask_1")
        .endEvent()
        .done();

    testRule.deploy(model);

    Map<String, Object> variables = new HashMap<>();
    variables.put("processInstanceVar", "processInstanceVarValue");
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    Map<String, Object> messageLocalPayload = new HashMap<>();
    String outpuValue = "outputValue";
    String localVarName = "testLocalVar";
    messageLocalPayload.put(localVarName, outpuValue);

    // when
    MessageCorrelationResultWithVariables messageCorrelationResult = runtimeService
        .createMessageCorrelation("1")
        .setVariablesLocal(messageLocalPayload)
        .correlateWithResultAndVariables(true);

    // then
    checkExecutionMessageCorrelationResult(messageCorrelationResult, processInstance, "message_1");

    VariableInstance variable = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId())
        .variableName(outputVarName)
        .singleResult();
    assertNotNull(variable);
    assertEquals(outpuValue, variable.getValue());
    assertEquals(processInstance.getId(), variable.getExecutionId());

    VariableInstance variableNonExisting = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId())
        .variableName(localVarName)
        .singleResult();
    assertNull(variableNonExisting);

    VariableMap variablesInReturn = messageCorrelationResult.getVariables();
    assertEquals(variable.getTypedValue(), variablesInReturn.getValueTyped(outputVarName));
    assertEquals("processInstanceVarValue", variablesInReturn.getValue("processInstanceVar", String.class));
  }

  @Test
  public void testCorrelationBySettingLocalVariables() {
    // given
    String outputVarName = "localVar";
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
          .intermediateCatchEvent("message_1")
            .message("1")
            .camundaOutputParameter(outputVarName, "${testLocalVar}")
          .userTask("UserTask_1")
        .endEvent()
        .done();

    testRule.deploy(model);

    Map<String, Object> variables = new HashMap<>();
    variables.put("processInstanceVar", "processInstanceVarValue");
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    Map<String, Object> messageLocalPayload = new HashMap<>();
    String outpuValue = "outputValue";
    String localVarName = "testLocalVar";
    messageLocalPayload.put(localVarName, outpuValue);

    // when
    runtimeService
        .createMessageCorrelation("1")
        .setVariablesLocal(messageLocalPayload)
        .correlate();

    // then
    VariableInstance variable = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId())
        .variableName(outputVarName)
        .singleResult();
    assertNotNull(variable);
    assertEquals(outpuValue, variable.getValue());
    assertEquals(processInstance.getId(), variable.getExecutionId());

    VariableInstance variableNonExisting = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId())
        .variableName(localVarName)
        .singleResult();
    assertNull(variableNonExisting);
  }



  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testCorrelationWithTransientLocalVariables() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
        .startEvent()
        .intermediateCatchEvent("message_1").message("message")
        .userTask("UserTask_1")
        .endEvent()
        .done();

    testRule.deploy(model);

    runtimeService.startProcessInstanceByKey("process");

    StringValue transientValue = Variables.stringValue("value", true);

    Map<String, Object> messageLocalPayload = new HashMap<>();
    messageLocalPayload.put("var", transientValue);

    // when
    runtimeService
        .createMessageCorrelation("message")
        .setVariablesLocal(messageLocalPayload)
        .correlate();

    // then
    long numHistoricVariables =
        engineRule.getHistoryService()
          .createHistoricVariableInstanceQuery()
          .count();

    assertThat(numHistoricVariables).isEqualTo(0);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.waitForMessageProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.sendMessageProcess.bpmn20.xml" })
  @Test
  public void testCorrelateWithResultTwoTimesInSameTransaction() {
    // start process that waits for message
    Map<String, Object> variables = new HashMap<>();
    variables.put("correlationKey", "someCorrelationKey");
    ProcessInstance messageWaitProcess = runtimeService.startProcessInstanceByKey("waitForMessageProcess", variables);

    Execution waitingProcess = runtimeService.createExecutionQuery().executionId(messageWaitProcess.getProcessInstanceId()).singleResult();
    Assert.assertNotNull(waitingProcess);

    VariableMap switchScenarioFlag = Variables.createVariables().putValue("allFlag", false);

    // when/then
    assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("sendMessageProcess", switchScenarioFlag))
      .isInstanceOf(MismatchingMessageCorrelationException.class)
      .hasMessageContaining("Cannot correlate message 'waitForCorrelationKeyMessage'");

    // waiting process has not finished
    waitingProcess = runtimeService.createExecutionQuery().executionId(messageWaitProcess.getProcessInstanceId()).singleResult();
    Assert.assertNotNull(waitingProcess);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.waitForMessageProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.sendMessageProcess.bpmn20.xml" })
  @Test
  public void testCorrelateAllWithResultTwoTimesInSameTransaction() {
    // start process that waits for message
    Map<String, Object> variables = new HashMap<>();
    variables.put("correlationKey", "someCorrelationKey");
    ProcessInstance messageWaitProcess = runtimeService.startProcessInstanceByKey("waitForMessageProcess", variables);

    Execution waitingProcess = runtimeService.createExecutionQuery().executionId(messageWaitProcess.getProcessInstanceId()).singleResult();
    Assert.assertNotNull(waitingProcess);

    // start process that sends two messages with the same correlationKey
    VariableMap switchScenarioFlag = Variables.createVariables().putValue("allFlag", true);
    runtimeService.startProcessInstanceByKey("sendMessageProcess", switchScenarioFlag);

    // waiting process must be finished
    waitingProcess = runtimeService.createExecutionQuery().executionId(messageWaitProcess.getProcessInstanceId()).singleResult();
    Assert.assertNull(waitingProcess);
  }

  @Test
  @Ignore("CAM-10198")
  public void testMessageStartEventCorrelationWithLocalVariables() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
        .message("1")
        .userTask("userTask1")
        .endEvent()
        .done();

    testRule.deploy(model);

    Map<String, Object> messagePayload = new HashMap<>();
    String outpuValue = "outputValue";
    String localVarName = "testLocalVar";
    messagePayload.put(localVarName, outpuValue);

    // when
    MessageCorrelationResult result = runtimeService
        .createMessageCorrelation("1")
        .setVariablesLocal(messagePayload)
        .correlateWithResult();

    // then
    assertNotNull(result);
    assertEquals(MessageCorrelationResultType.ProcessDefinition, result.getResultType());
  }

  @Test
  public void testMessageStartEventCorrelationWithVariablesInResult() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
        .message("1")
        .userTask("UserTask_1")
        .endEvent()
        .done();

    testRule.deploy(model);

    // when
    MessageCorrelationResultWithVariables result = runtimeService
        .createMessageCorrelation("1")
        .setVariable("foo", "bar")
        .correlateWithResultAndVariables(true);

    // then
    assertNotNull(result);
    assertEquals(MessageCorrelationResultType.ProcessDefinition, result.getResultType());
    assertEquals("bar", result.getVariables().getValue("foo", String.class));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testCatchingMessageEventCorrelation.bpmn20.xml")
  @Test
  public void testCorrelateAllWithResultVariables() {
    //given
    ProcessInstance procInstance1 = runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("var1", "foo"));
    ProcessInstance procInstance2 = runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("var2", "bar"));

    //when correlated all with result and variables
    List<MessageCorrelationResultWithVariables> resultList = runtimeService
        .createMessageCorrelation("newInvoiceMessage")
        .correlateAllWithResultAndVariables(true);

    assertEquals(2, resultList.size());
    //then result should contains executions on which messages was correlated
    for (MessageCorrelationResultWithVariables result : resultList) {
      assertNotNull(result);
      assertEquals(MessageCorrelationResultType.Execution, result.getResultType());
      ExecutionEntity execution = (ExecutionEntity) result.getExecution();
      VariableMap variables = result.getVariables();
      assertEquals(1, variables.size());
      if (procInstance1.getId().equalsIgnoreCase(execution.getProcessInstanceId())) {
        assertEquals("foo", variables.getValue("var1", String.class));
      } else if (procInstance2.getId().equalsIgnoreCase(execution.getProcessInstanceId())) {
        assertEquals("bar", variables.getValue("var2", String.class));
      } else {
        fail("Only those process instances should exist");
      }
    }
  }

  @Test
  public void testCorrelationWithModifiedVariablesInResult() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
        .intermediateCatchEvent("Message_1")
        .message("1")
        .serviceTask()
        .camundaClass(ChangeVariableDelegate.class.getName())
        .userTask("UserTask_1")
        .endEvent()
        .done();

    testRule.deploy(model);

    runtimeService.startProcessInstanceByKey("Process_1",
        Variables.createVariables()
        .putValue("a", 40)
        .putValue("b", 2));

    // when
    MessageCorrelationResultWithVariables result = runtimeService
        .createMessageCorrelation("1")
        .correlateWithResultAndVariables(true);

    // then
    assertNotNull(result);
    assertEquals(MessageCorrelationResultType.Execution, result.getResultType());
    assertEquals(3, result.getVariables().size());
    assertEquals("foo", result.getVariables().get("a"));
    assertEquals(2, result.getVariables().get("b"));
    assertEquals(42, result.getVariables().get("sum"));
  }

  @Test
  public void testCorrelateWithVariablesInReturnShouldDeserializeObjectValue()
  {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
      .startEvent()
      .intermediateCatchEvent("Message_1")
      .message("1")
      .userTask("UserTask_1")
      .endEvent()
      .done();

    testRule.deploy(model);

    ObjectValue value = Variables.objectValue("value").create();
    VariableMap variables = Variables.createVariables().putValue("var", value);

    runtimeService.startProcessInstanceByKey("process", variables);

    // when
    MessageCorrelationResultWithVariables result = runtimeService.createMessageCorrelation("1")
        .correlateWithResultAndVariables(true);

    // then
    VariableMap resultVariables = result.getVariables();

    ObjectValue returnedValue = resultVariables.getValueTyped("var");
    assertThat(returnedValue.isDeserialized()).isTrue();
    assertThat(returnedValue.getValue()).isEqualTo("value");
  }

  @Test
  public void testCorrelateWithVariablesInReturnShouldNotDeserializeObjectValue()
  {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
      .startEvent()
      .intermediateCatchEvent("Message_1")
      .message("1")
      .userTask("UserTask_1")
      .endEvent()
      .done();

    testRule.deploy(model);

    ObjectValue value = Variables.objectValue("value").create();
    VariableMap variables = Variables.createVariables().putValue("var", value);

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process", variables);
    String serializedValue = ((ObjectValue) runtimeService.getVariableTyped(instance.getId(), "var")).getValueSerialized();

    // when
    MessageCorrelationResultWithVariables result = runtimeService.createMessageCorrelation("1")
        .correlateWithResultAndVariables(false);

    // then
    VariableMap resultVariables = result.getVariables();

    ObjectValue returnedValue = resultVariables.getValueTyped("var");
    assertThat(returnedValue.isDeserialized()).isFalse();
    assertThat(returnedValue.getValueSerialized()).isEqualTo(serializedValue);
  }

  @Test
  public void testCorrelateAllWithVariablesInReturnShouldDeserializeObjectValue()
  {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
      .startEvent()
      .intermediateCatchEvent("Message_1")
      .message("1")
      .userTask("UserTask_1")
      .endEvent()
      .done();

    testRule.deploy(model);

    ObjectValue value = Variables.objectValue("value").create();
    VariableMap variables = Variables.createVariables().putValue("var", value);

    runtimeService.startProcessInstanceByKey("process", variables);

    // when
    List<MessageCorrelationResultWithVariables> result = runtimeService.createMessageCorrelation("1")
        .correlateAllWithResultAndVariables(true);

    // then
    assertThat(result).hasSize(1);

    VariableMap resultVariables = result.get(0).getVariables();

    ObjectValue returnedValue = resultVariables.getValueTyped("var");
    assertThat(returnedValue.isDeserialized()).isTrue();
    assertThat(returnedValue.getValue()).isEqualTo("value");
  }

  @Test
  public void testCorrelateAllWithVariablesInReturnShouldNotDeserializeObjectValue()
  {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
      .startEvent()
      .intermediateCatchEvent("Message_1")
      .message("1")
      .userTask("UserTask_1")
      .endEvent()
      .done();

    testRule.deploy(model);

    ObjectValue value = Variables.objectValue("value").create();
    VariableMap variables = Variables.createVariables().putValue("var", value);

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process", variables);
    String serializedValue = ((ObjectValue) runtimeService.getVariableTyped(instance.getId(), "var")).getValueSerialized();

    // when
    List<MessageCorrelationResultWithVariables> result = runtimeService.createMessageCorrelation("1")
        .correlateAllWithResultAndVariables(false);

    // then
    assertThat(result).hasSize(1);

    VariableMap resultVariables = result.get(0).getVariables();

    ObjectValue returnedValue = resultVariables.getValueTyped("var");
    assertThat(returnedValue.isDeserialized()).isFalse();
    assertThat(returnedValue.getValueSerialized()).isEqualTo(serializedValue);
  }

  @Test
  public void testStartMessageOnlyFlag() {
    deployTwoVersionsWithStartMessageEvent();

    ProcessDefinition firstProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(1).singleResult();
    ProcessDefinition secondProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(2).singleResult();

    runtimeService.createMessageCorrelation("a")
      .startMessageOnly()
      .processDefinitionId(firstProcessDefinition.getId())
      .processInstanceBusinessKey("first")
      .correlate();

    runtimeService.createMessageCorrelation("a")
      .startMessageOnly()
      .processDefinitionId(secondProcessDefinition.getId())
      .processInstanceBusinessKey("second")
      .correlate();

    assertTwoInstancesAreStarted(firstProcessDefinition, secondProcessDefinition);
  }

  @Test
  public void testStartMessageOnlyFlagAll() {
    deployTwoVersionsWithStartMessageEvent();

    ProcessDefinition firstProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(1).singleResult();
    ProcessDefinition secondProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(2).singleResult();

    runtimeService.createMessageCorrelation("a")
      .startMessageOnly()
      .processDefinitionId(firstProcessDefinition.getId())
      .processInstanceBusinessKey("first")
      .correlateAll();

    runtimeService.createMessageCorrelation("a")
      .startMessageOnly()
      .processDefinitionId(secondProcessDefinition.getId())
      .processInstanceBusinessKey("second")
      .correlateAll();

    assertTwoInstancesAreStarted(firstProcessDefinition, secondProcessDefinition);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testStartMessageOnlyFlagWithResult() {
    MessageCorrelationResult result = runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariable("aKey", "aValue")
      .startMessageOnly()
      .correlateWithResult();

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("aKey", "aValue");
    assertThat(processInstanceQuery.count()).isEqualTo(1);
    assertThat(result.getProcessInstance().getId()).isEqualTo(processInstanceQuery.singleResult().getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testStartMessageOnlyFlagWithVariablesInResult() {

    MessageCorrelationResultWithVariables result = runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariable("aKey", "aValue")
      .startMessageOnly()
      .correlateWithResultAndVariables(false);

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("aKey", "aValue");
    assertThat(processInstanceQuery.count()).isEqualTo(1);
    assertThat(result.getVariables().size()).isEqualTo(1);
    assertThat(result.getVariables().getValueTyped("aKey").getValue()).isEqualTo("aValue");
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testStartMessageOnlyFlagAllWithResult() {
    List<MessageCorrelationResult> result = runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariable("aKey", "aValue")
      .startMessageOnly()
      .correlateAllWithResult();

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("aKey", "aValue");
    assertThat(processInstanceQuery.count()).isEqualTo(1);
    assertThat(result.get(0).getProcessInstance().getId()).isEqualTo(processInstanceQuery.singleResult().getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/MessageCorrelationTest.testMessageStartEventCorrelation.bpmn20.xml")
  @Test
  public void testStartMessageOnlyFlagAllWithVariablesInResult() {

    List<MessageCorrelationResultWithVariables> results = runtimeService.createMessageCorrelation("newInvoiceMessage")
      .setVariable("aKey", "aValue")
      .startMessageOnly()
      .correlateAllWithResultAndVariables(false);

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey("messageStartEvent")
        .variableValueEquals("aKey", "aValue");
    assertThat(processInstanceQuery.count()).isEqualTo(1);
    MessageCorrelationResultWithVariables result = results.get(0);
    assertThat(result.getVariables().size()).isEqualTo(1);
    assertThat(result.getVariables().getValueTyped("aKey").getValue()).isEqualTo("aValue");
  }

  @Test
  public void testFailStartMessageOnlyFlagWithCorrelationVariable() {
    try {
      runtimeService.createMessageCorrelation("a")
        .startMessageOnly()
        .processInstanceVariableEquals("var", "value")
        .correlate();

      fail("expected exception");
    } catch (BadUserRequestException e){
      testRule.assertTextPresent("Cannot specify correlation variables ", e.getMessage());
    }
  }

  @Test
  public void testFailStartMessageOnlyFlagWithCorrelationVariables() {
    try {
      runtimeService.createMessageCorrelation("a")
        .startMessageOnly()
        .processInstanceVariablesEqual(Variables
              .createVariables()
              .putValue("var1", "b")
              .putValue("var2", "c"))
        .correlateAll();

      fail("expected exception");
    } catch (BadUserRequestException e){
      testRule.assertTextPresent("Cannot specify correlation variables ", e.getMessage());
    }
  }

  protected void deployTwoVersionsWithStartMessageEvent() {
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
          .message("a")
        .userTask("ut1")
        .endEvent()
        .done());

    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
          .message("a")
        .userTask("ut2")
        .endEvent()
        .done());
  }

  protected void assertTwoInstancesAreStarted(ProcessDefinition firstProcessDefinition, ProcessDefinition secondProcessDefinition) {
    assertThat(runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey("first")
        .processDefinitionId(firstProcessDefinition.getId())
        .count())
        .isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey("second")
        .processDefinitionId(secondProcessDefinition.getId())
        .count())
        .isEqualTo(1);
  }

  public static class ChangeVariableDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
      Integer a = (Integer) execution.getVariable("a");
      Integer b = (Integer) execution.getVariable("b");
      execution.setVariable("sum", a + b);
      execution.setVariable("a", "foo");
    }
  }
}
