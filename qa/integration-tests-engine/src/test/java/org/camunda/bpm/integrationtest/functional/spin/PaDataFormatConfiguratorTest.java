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
package org.camunda.bpm.integrationtest.functional.spin;

import java.io.IOException;
import java.util.Date;

import org.camunda.bpm.application.ProcessApplicationContext;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.ImplicitObjectValueUpdateDelegate;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.JsonDataFormatConfigurator;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.JsonSerializable;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormatConfigurator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thorben Lindhauer
 *
 */
@Ignore
@RunWith(Arquillian.class)
public class PaDataFormatConfiguratorTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "PaDataFormatTest.war")
        .addAsResource("META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(TestContainer.class)
        .addClass(ReferenceStoringProcessApplication.class)
        .addAsResource("org/camunda/bpm/integrationtest/oneTaskProcess.bpmn")
        .addAsResource("org/camunda/bpm/integrationtest/functional/spin/implicitUpdate.bpmn")
        .addClass(JsonSerializable.class)
        .addClass(ImplicitObjectValueUpdateDelegate.class)
        .addClass(JsonDataFormatConfigurator.class)
        .addAsServiceProvider(DataFormatConfigurator.class, JsonDataFormatConfigurator.class);

    TestContainer.addSpinJacksonJsonDataFormat(webArchive);

    return webArchive;

  }

  @Test
  public void testBuiltinFormatDoesNotApply() throws JsonProcessingException, IOException {

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

  @Test
  public void testBuiltinFormatApplies() throws JsonProcessingException, IOException {

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

  @Test
  @Ignore
  public void testConfiguredNativeJsonFormat() {
    // given a process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // when
    JsonSerializable jsonSerializable = new JsonSerializable(new Date(JsonSerializable.ONE_DAY_IN_MILLIS * 10));
    String serializedJsonValue = jsonSerializable.toExpectedJsonString(JsonDataFormatConfigurator.DATE_FORMAT);

    // then I can set a serialized Spin value that adheres to the configured format
    try {
      // TODO: the creation of the variable is not correct; right now it is not possible to create
      //   a native json/xml value with a pa-specific data format
      ProcessApplicationContext.setCurrentProcessApplication(ReferenceStoringProcessApplication.INSTANCE);
      runtimeService.setVariable(pi.getId(),
        "jsonSerializable",
        Variables.serializedObjectValue(serializedJsonValue).serializationDataFormat("json").create());
    } finally {
      ProcessApplicationContext.clear();
    }

    // and I can access it as well
    SpinJsonNode spinNode = null;

    try {
      ProcessApplicationContext.setCurrentProcessApplication(ReferenceStoringProcessApplication.INSTANCE);
      spinNode = (SpinJsonNode) runtimeService.getVariable(pi.getId(), "jsonSerializable");
    } finally {
      ProcessApplicationContext.clear();
    }

    JsonSerializable mappedSerializable = spinNode.mapTo(JsonSerializable.class);
    Assert.assertEquals(jsonSerializable, mappedSerializable);
  }

  @Test
  public void testImplicitObjectValueUpdate() throws JsonProcessingException, IOException {

    // given a process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("implicitUpdate");

    // when setting a variable such that the process-application-local dataformat applies
    Date date = new Date(JsonSerializable.ONE_DAY_IN_MILLIS * 10); // 10th of January 1970
    JsonSerializable jsonSerializable = new JsonSerializable(date);
    try {
      ProcessApplicationContext.setCurrentProcessApplication(ReferenceStoringProcessApplication.INSTANCE);
      runtimeService.setVariable(pi.getId(),
        ImplicitObjectValueUpdateDelegate.VARIABLE_NAME,
        Variables.objectValue(jsonSerializable).serializationDataFormat(SerializationDataFormats.JSON).create());
    } finally {
      ProcessApplicationContext.clear();
    }

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    // and triggering an implicit update of the object value variable
    taskService.complete(task.getId());

    // then the process-application-local format was used for making the update
    ObjectValue objectValue = runtimeService.getVariableTyped(pi.getId(),
        ImplicitObjectValueUpdateDelegate.VARIABLE_NAME,
        false);

    ImplicitObjectValueUpdateDelegate.addADay(jsonSerializable);
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
        .variableName(ImplicitObjectValueUpdateDelegate.VARIABLE_NAME)
        .disableCustomObjectDeserialization()
        .singleResult();

    serializedValue = ((ObjectValue) historicObjectValue.getTypedValue()).getValueSerialized();
    actualJsonTree = objectMapper.readTree(serializedValue);
    Assert.assertEquals(expectedJsonTree, actualJsonTree);
  }

  // TODO: another test for implicit update of a task variable required?
}
