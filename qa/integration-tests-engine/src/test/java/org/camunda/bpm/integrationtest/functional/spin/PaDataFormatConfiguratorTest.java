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
package org.camunda.bpm.integrationtest.functional.spin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.application.ProcessApplicationContext;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.ImplicitObjectValueUpdateHandler;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.JsonDataFormatConfigurator;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.JsonSerializable;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.camunda.spin.spi.DataFormatConfigurator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Arquillian.class)
public class PaDataFormatConfiguratorTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "PaDataFormatTest.war")
        .addAsResource("META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(ReferenceStoringProcessApplication.class)
        .addAsResource("org/camunda/bpm/integrationtest/oneTaskProcess.bpmn")
        .addAsResource("org/camunda/bpm/integrationtest/functional/spin/implicitProcessVariableUpdate.bpmn")
        .addAsResource("org/camunda/bpm/integrationtest/functional/spin/implicitTaskVariableUpdate.bpmn")
        .addClass(JsonSerializable.class)
        .addClass(ImplicitObjectValueUpdateHandler.class)
        .addClass(JsonDataFormatConfigurator.class)
        .addAsServiceProvider(DataFormatConfigurator.class, JsonDataFormatConfigurator.class);

    TestContainer.addSpinJacksonJsonDataFormat(webArchive);

    return webArchive;

  }

  /**
   * Tests that the PA-local data format applies when a variable is set in
   * the context of it
   */
  @Test
  public void testPaLocalFormatApplies() throws JsonProcessingException, IOException {

    // given a process instance
    final ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // when setting a variable in the context of a process application
    Date date = new Date(JsonSerializable.ONE_DAY_IN_MILLIS * 10); // 10th of January 1970
    JsonSerializable jsonSerializable = new JsonSerializable(date);

    try {
      ProcessApplicationContext.setCurrentProcessApplication(ReferenceStoringProcessApplication.INSTANCE);
      runtimeService.setVariable(pi.getId(),
        "jsonSerializable",
        Variables.objectValue(jsonSerializable).serializationDataFormat(SerializationDataFormats.JSON).create());
    } finally {
      ProcessApplicationContext.clear();
    }

    // then the process-application-local data format has been used to serialize the value
    ObjectValue objectValue = runtimeService.getVariableTyped(pi.getId(), "jsonSerializable", false);

    String serializedValue = objectValue.getValueSerialized();
    String expectedSerializedValue = jsonSerializable.toExpectedJsonString(JsonDataFormatConfigurator.DATE_FORMAT);

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode actualJsonTree = objectMapper.readTree(serializedValue);
    JsonNode expectedJsonTree = objectMapper.readTree(expectedSerializedValue);
    // JsonNode#equals makes a deep comparison
    Assert.assertEquals(expectedJsonTree, actualJsonTree);
  }

  /**
   * Tests that the PA-local format does not apply if the value is set outside of the context
   * of the process application
   */
  @Test
  public void testPaLocalFormatDoesNotApply() throws JsonProcessingException, IOException {

    // given a process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // when setting a variable without a process-application cotnext
    Date date = new Date(JsonSerializable.ONE_DAY_IN_MILLIS * 10); // 10th of January 1970
    JsonSerializable jsonSerializable = new JsonSerializable(date);

    runtimeService.setVariable(pi.getId(),
      "jsonSerializable",
      Variables.objectValue(jsonSerializable).serializationDataFormat(SerializationDataFormats.JSON).create());

    // then the global data format is applied
    ObjectValue objectValue = runtimeService.getVariableTyped(pi.getId(), "jsonSerializable", false);

    String serializedValue = objectValue.getValueSerialized();
    String expectedSerializedValue = jsonSerializable.toExpectedJsonString();

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode actualJsonTree = objectMapper.readTree(serializedValue);
    JsonNode expectedJsonTree = objectMapper.readTree(expectedSerializedValue);
    // JsonNode#equals makes a deep comparison
    Assert.assertEquals(expectedJsonTree, actualJsonTree);
  }

  /**
   * Tests that an implicit object value update happens in the context of the
   * process application.
   */
  @Test
  public void testExecutionVariableImplicitObjectValueUpdate() throws JsonProcessingException, IOException {

    // given a process instance and a task
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("implicitProcessVariableUpdate");

    // when setting a variable such that the process-application-local dataformat applies
    Date date = new Date(JsonSerializable.ONE_DAY_IN_MILLIS * 10); // 10th of January 1970
    JsonSerializable jsonSerializable = new JsonSerializable(date);
    try {

      ProcessApplicationContext.setCurrentProcessApplication(ReferenceStoringProcessApplication.INSTANCE);
      runtimeService.setVariable(pi.getId(),
        ImplicitObjectValueUpdateHandler.VARIABLE_NAME,
        Variables.objectValue(jsonSerializable).serializationDataFormat(SerializationDataFormats.JSON).create());
    } finally {
      ProcessApplicationContext.clear();
    }

    // and triggering an implicit update of the object value variable
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    taskService.complete(task.getId());

    // then the process-application-local format was used for making the update
    ObjectValue objectValue = runtimeService.getVariableTyped(pi.getId(),
        ImplicitObjectValueUpdateHandler.VARIABLE_NAME,
        false);

    ImplicitObjectValueUpdateHandler.addADay(jsonSerializable);
    String serializedValue = objectValue.getValueSerialized();
    String expectedSerializedValue = jsonSerializable.toExpectedJsonString(JsonDataFormatConfigurator.DATE_FORMAT);

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode actualJsonTree = objectMapper.readTree(serializedValue);
    JsonNode expectedJsonTree = objectMapper.readTree(expectedSerializedValue);
    // JsonNode#equals makes a deep comparison
    Assert.assertEquals(expectedJsonTree, actualJsonTree);

    // and it is also correct in the history
    HistoricVariableInstance historicObjectValue = historyService
        .createHistoricVariableInstanceQuery()
        .processInstanceId(pi.getId())
        .variableName(ImplicitObjectValueUpdateHandler.VARIABLE_NAME)
        .disableCustomObjectDeserialization()
        .singleResult();

    serializedValue = ((ObjectValue) historicObjectValue.getTypedValue()).getValueSerialized();
    actualJsonTree = objectMapper.readTree(serializedValue);
    Assert.assertEquals(expectedJsonTree, actualJsonTree);
  }

  @Test
  public void testTaskVariableImplicitObjectValueUpdate() throws JsonProcessingException, IOException {

    // given a process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("implicitTaskVariableUpdate");
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();

    // when setting a variable such that the process-application-local dataformat applies
    Date date = new Date(JsonSerializable.ONE_DAY_IN_MILLIS * 10); // 10th of January 1970
    JsonSerializable jsonSerializable = new JsonSerializable(date);
    try {
      ProcessApplicationContext.setCurrentProcessApplication(ReferenceStoringProcessApplication.INSTANCE);
      taskService.setVariableLocal(task.getId(),
        ImplicitObjectValueUpdateHandler.VARIABLE_NAME,
        Variables.objectValue(jsonSerializable).serializationDataFormat(SerializationDataFormats.JSON).create());
    } finally {
      ProcessApplicationContext.clear();
    }

    // and triggering an implicit update of the object value variable
    taskService.setAssignee(task.getId(), "foo");

    // then the process-application-local format was used for making the update
    ObjectValue objectValue = taskService.getVariableTyped(task.getId(),
        ImplicitObjectValueUpdateHandler.VARIABLE_NAME,
        false);

    ImplicitObjectValueUpdateHandler.addADay(jsonSerializable);
    String serializedValue = objectValue.getValueSerialized();
    String expectedSerializedValue = jsonSerializable.toExpectedJsonString(JsonDataFormatConfigurator.DATE_FORMAT);

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode actualJsonTree = objectMapper.readTree(serializedValue);
    JsonNode expectedJsonTree = objectMapper.readTree(expectedSerializedValue);
    // JsonNode#equals makes a deep comparison
    Assert.assertEquals(expectedJsonTree, actualJsonTree);

    // and it is also correct in the history
    HistoricVariableInstance historicObjectValue = historyService
        .createHistoricVariableInstanceQuery()
        .processInstanceId(pi.getId())
        .variableName(ImplicitObjectValueUpdateHandler.VARIABLE_NAME)
        .disableCustomObjectDeserialization()
        .singleResult();

    serializedValue = ((ObjectValue) historicObjectValue.getTypedValue()).getValueSerialized();
    actualJsonTree = objectMapper.readTree(serializedValue);
    Assert.assertEquals(expectedJsonTree, actualJsonTree);
  }
}
