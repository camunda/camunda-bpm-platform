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
package org.camunda.bpm.client.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.camunda.bpm.client.util.ProcessModels.TWO_EXTERNAL_TASK_PROCESS;
import static org.camunda.spin.DataFormats.XML_DATAFORMAT_NAME;
import static org.camunda.spin.plugin.variable.type.SpinValueType.XML;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.XmlValue;
import org.camunda.spin.xml.SpinXmlElement;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class XmlValueIT {

  protected static final String VARIABLE_NAME_XML = "xmlVariable";

  protected static final String VARIABLE_VALUE_XML_SERIALIZED = "<elementName attrName=\"attrValue\" />";
  protected static final String VARIABLE_VALUE_XML_SERIALIZED_BROKEN = "<elementName attrName=attrValue\" />";

  protected static final XmlValue VARIABLE_VALUE_XML_VALUE = SpinValues.xmlValue(VARIABLE_VALUE_XML_SERIALIZED)
      .serializationDataFormat(XML_DATAFORMAT_NAME)
      .create();

  protected static final XmlValue VARIABLE_VALUE_XML_VALUE_BROKEN = SpinValues.xmlValue(VARIABLE_VALUE_XML_SERIALIZED_BROKEN)
      .serializationDataFormat(XML_DATAFORMAT_NAME)
      .create();

  protected ClientRule clientRule = new ClientRule();
  protected EngineRule engineRule = new EngineRule();
  protected ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule).around(thrown);

  protected ExternalTaskClient client;

  protected ProcessDefinitionDto processDefinition;
  protected ProcessInstanceDto processInstance;

  protected RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();

  @Before
  public void setup() throws Exception {
    client = clientRule.client();
    handler.clear();
    processDefinition = engineRule.deploy(TWO_EXTERNAL_TASK_PROCESS).get(0);
  }

  @Test
  public void shouldGetDeserializedXml() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    SpinXmlElement variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isNotNull();
    assertThat("attrValue").isEqualTo(variableValue.attr("attrName").toString());
  }

  @Test
  public void shouldGetTypedDeserializedXml() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    XmlValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(typedValue.getType()).isEqualTo(XML);
    assertThat(typedValue.isDeserialized()).isTrue();

    SpinXmlElement variableValue = typedValue.getValue();
    assertThat(variableValue).isNotNull();
    assertThat("attrValue").isEqualTo(variableValue.attr("attrName").toString());
  }

  @Test
  public void shouldGetTypedSerializedXml() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    XmlValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML, false);
    assertThat(typedValue.getType()).isEqualTo(XML);
    assertThat(typedValue.isDeserialized()).isFalse();

    String xmlValueSerialized = typedValue.getValueSerialized();
    assertThat(xmlValueSerialized).isEqualTo(VARIABLE_VALUE_XML_SERIALIZED);
  }

  @Test
  public void shouldDeserializeNull() {
    // given
    XmlValue xmlValue = SpinValues.xmlValue((String) null)
        .serializationDataFormat(XML_DATAFORMAT_NAME)
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, xmlValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    SpinXmlElement returnedBean = task.getVariable(VARIABLE_NAME_XML);
    assertThat(returnedBean).isNull();
  }

  @Test
  public void shouldDeserializeNullTyped() {
    // given
    XmlValue xmlValue = SpinValues.xmlValue((String) null)
        .serializationDataFormat(XML_DATAFORMAT_NAME)
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, xmlValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    XmlValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(typedValue.getValue()).isNull();
    assertThat(typedValue.getType()).isEqualTo(XML);
    assertThat(typedValue.isDeserialized()).isTrue();
  }

  @Test
  public void shouldFailWithBrokenXmlWhileSerialization() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_VALUE_BROKEN);

    // then
    thrown.expect(ValueMapperException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariable(VARIABLE_NAME_XML);
  }

  @Test
  public void shouldFailWithBrokenXmlWhileSerializationTyped() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_VALUE_BROKEN);

    // then
    thrown.expect(ValueMapperException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariableTyped(VARIABLE_NAME_XML);
  }

  @Test
  public void shouldReturnBrokenSerializedXml() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_VALUE_BROKEN);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    XmlValue xmlValue = task.getVariableTyped(VARIABLE_NAME_XML, false);
    String xmlSerialized = xmlValue.getValueSerialized();
    assertThat(xmlSerialized).isEqualTo(VARIABLE_VALUE_XML_SERIALIZED_BROKEN);
  }

}
