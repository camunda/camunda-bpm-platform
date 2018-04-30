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
package org.camunda.spin.plugin.variables;

import static org.camunda.spin.DataFormats.xml;
import static org.camunda.spin.plugin.variable.SpinValues.xmlValue;
import static org.camunda.spin.plugin.variable.type.SpinValueType.XML;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.spin.DataFormats;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.value.XmlValue;
import org.camunda.spin.plugin.variable.value.builder.XmlValueBuilder;
import org.camunda.spin.xml.SpinXmlElement;

/**
 * @author Roman Smirnov
 *
 */
public class XmlValueTest extends PluggableProcessEngineTestCase {

  protected static final String ONE_TASK_PROCESS = "org/camunda/spin/plugin/oneTaskProcess.bpmn20.xml";
  protected static final String XML_FORMAT_NAME = DataFormats.XML_DATAFORMAT_NAME;

  protected static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";

  protected String xmlString = "<elementName attrName=\"attrValue\" />";
  protected String brokenXmlString = "<elementName attrName=attrValue\" />";

  protected String variableName = "x";

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testGetUntypedXmlValue() {
    // given
    XmlValue xmlValue = xmlValue(xmlString).create();
    VariableMap variables = Variables.createVariables().putValueTyped(variableName, xmlValue);

    String processInstanceId = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY, variables).getId();

    // when
    SpinXmlElement value = (SpinXmlElement) runtimeService.getVariable(processInstanceId, variableName);

    // then
    assertTrue(value.hasAttr("attrName"));
    assertEquals("attrValue", value.attr("attrName").value());
    assertTrue(value.childElements().isEmpty());
    assertEquals(xml().getName(), value.getDataFormatName());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testGetTypedXmlValue() {
    // given
    XmlValue xmlValue = xmlValue(xmlString).create();
    VariableMap variables = Variables.createVariables().putValueTyped(variableName, xmlValue);

    String processInstanceId = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY, variables).getId();

    // when
    XmlValue typedValue = runtimeService.getVariableTyped(processInstanceId, variableName);

    // then
    SpinXmlElement value = typedValue.getValue();
    assertTrue(value.hasAttr("attrName"));
    assertEquals("attrValue", value.attr("attrName").value());
    assertTrue(value.childElements().isEmpty());

    assertTrue(typedValue.isDeserialized());
    assertEquals(XML, typedValue.getType());
    assertEquals(XML_FORMAT_NAME, typedValue.getSerializationDataFormat());
    assertEquals(xmlString, typedValue.getValueSerialized());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testBrokenXmlSerialization() {
    // given
    XmlValue value = xmlValue(brokenXmlString).create();

    String processInstanceId = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when
      runtimeService.setVariable(processInstanceId, variableName, value);
    } catch (Exception e) {
      fail("no exception expected");
    }
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testFailingDeserialization() {
    // given
    XmlValue value = xmlValue(brokenXmlString).create();

    String processInstanceId = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    runtimeService.setVariable(processInstanceId, variableName, value);

    try {
      // when
      runtimeService.getVariable(processInstanceId, variableName);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // happy path
    }

    try {
      runtimeService.getVariableTyped(processInstanceId, variableName);
      fail("exception expected");
    } catch(ProcessEngineException e) {
      // happy path
    }

    // However, I can access the serialized value
    XmlValue xmlValue = runtimeService.getVariableTyped(processInstanceId, variableName, false);
    assertFalse(xmlValue.isDeserialized());
    assertEquals(brokenXmlString, xmlValue.getValueSerialized());

    // but not the deserialized properties
    try {
      xmlValue.getValue();
      fail("exception expected");
    } catch(SpinRuntimeException e) {
    }
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testFailForNonExistingSerializationFormat() {
    // given
    XmlValueBuilder builder = xmlValue(xmlString).serializationDataFormat("non existing data format");
    String processInstanceId = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when (1)
      runtimeService.setVariable(processInstanceId, variableName, builder);
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      // then (1)
      assertTextPresent("Cannot find serializer for value", e.getMessage());
      // happy path
    }

    try {
      // when (2)
      runtimeService.setVariable(processInstanceId, variableName, builder.create());
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      // then (2)
      assertTextPresent("Cannot find serializer for value", e.getMessage());
      // happy path
    }
  }

  @Deployment(resources = "org/camunda/spin/plugin/xmlConditionProcess.bpmn20.xml")
  public void testXmlValueInCondition() {
    // given
    String xmlString = "<customer age=\"22\" />";
    XmlValue value = xmlValue(xmlString).create();
    VariableMap variables = Variables.createVariables().putValueTyped("customer", value);

    // when
    runtimeService.startProcessInstanceByKey("process", variables);

    // then
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task1", task.getTaskDefinitionKey());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testTransientXmlValueFluent() {
    // given
    XmlValue xmlValue = xmlValue(xmlString).setTransient(true).create();
    VariableMap variables = Variables.createVariables().putValueTyped(variableName, xmlValue);

    // when
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY, variables).getId();

    // then
    List<VariableInstance> variableInstances = runtimeService.createVariableInstanceQuery().list();
    assertEquals(0, variableInstances.size());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testTransientXmlValue() {
    // given
    XmlValue xmlValue = xmlValue(xmlString, true).create();
    VariableMap variables = Variables.createVariables().putValueTyped(variableName, xmlValue);

    // when
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY, variables).getId();

    // then
    List<VariableInstance> variableInstances = runtimeService.createVariableInstanceQuery().list();
    assertEquals(0, variableInstances.size());
  }

  public void testApplyValueInfoFromSerializedValue() {
    // given
    Map<String, Object> valueInfo = new HashMap<String, Object>();
    valueInfo.put(ValueType.VALUE_INFO_TRANSIENT, true);

    // when
    XmlValue xmlValue = (XmlValue) SpinValueType.XML.createValueFromSerialized(xmlString, valueInfo);

    // then
    assertEquals(true, xmlValue.isTransient());
    Map<String, Object> returnedValueInfo = SpinValueType.XML.getValueInfo(xmlValue);
    assertEquals(true, returnedValueInfo.get(ValueType.VALUE_INFO_TRANSIENT));
  }

  public void testDeserializeTransientXmlValue() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .exclusiveGateway("gtw")
          .sequenceFlowId("flow1")
          .condition("cond", "${XML(" + variableName + ").attr('attrName').value() == 'attrValue'}")
          .userTask("userTask1")
          .endEvent()
        .moveToLastGateway()
          .sequenceFlowId("flow2")
          .userTask("userTask2")
          .endEvent()
        .done();

    deployment(modelInstance);

    XmlValue xmlValue = xmlValue(xmlString, true).create();
    VariableMap variables = Variables.createVariables().putValueTyped(variableName, xmlValue);

    // when
    runtimeService.startProcessInstanceByKey("foo", variables);

    // then
    List<VariableInstance> variableInstances = runtimeService.createVariableInstanceQuery().list();
    assertEquals(0, variableInstances.size());

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("userTask1", task.getTaskDefinitionKey());
  }
}
