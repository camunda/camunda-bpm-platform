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

package org.camunda.bpm.engine.test.variables;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;
import org.camunda.bpm.engine.impl.variable.DefaultSerializationFormatType;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.spin.DataFormats;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

public class VariableDataFormatTest extends AbstractProcessEngineTestCase {

  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/variables/oneTaskProcess.bpmn20.xml";

  protected static final String JSON_FORMAT_NAME = DataFormats.jsonTreeFormat().getName();

  @Override
  protected void initializeProcessEngine() {
    ProcessEngineConfigurationImpl engineConfig =
        (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("camunda.cfg.xml");

    engineConfig.setDefaultSerializationFormat(JSON_FORMAT_NAME);

    processEngine = engineConfig.buildProcessEngine();
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSerializationAsJson() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    SimpleBean bean = new SimpleBean("a String", 42, true);
    runtimeService.setVariable(instance.getId(), "simpleBean", bean);

    VariableInstance beanVariable = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(beanVariable);
    assertEquals(DefaultSerializationFormatType.TYPE_NAME, beanVariable.getTypeName());

    SimpleBean returnedBean = (SimpleBean) beanVariable.getValue();
    assertNotNull(returnedBean);
    assertEquals(returnedBean.getIntProperty(), bean.getIntProperty());
    assertEquals(returnedBean.getBooleanProperty(), bean.getBooleanProperty());
    assertEquals(returnedBean.getStringProperty(), bean.getStringProperty());

    assertEquals(JSON_FORMAT_NAME, beanVariable.getDataFormatId());

    String persistedValue = beanVariable.getRawValue();
    String expectedJson = bean.toExpectedJsonString();
    JSONAssert.assertEquals(expectedJson, persistedValue, true);

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testListSerializationAsJson() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<SimpleBean> beans = new ArrayList<SimpleBean>();
    for (int i = 0; i < 20; i++) {
      beans.add(new SimpleBean("a String" + i, 42 + i, true));
    }

    runtimeService.setVariable(instance.getId(), "simpleBeans", beans);

    VariableInstance beansVariable = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(beansVariable);
    assertEquals(DefaultSerializationFormatType.TYPE_NAME, beansVariable.getTypeName());

    List<SimpleBean> returnedBeans = (List<SimpleBean>) beansVariable.getValue();
    assertNotNull(returnedBeans);
    assertTrue(returnedBeans instanceof ArrayList);
    assertListsEqual(beans, returnedBeans);

    assertEquals(JSON_FORMAT_NAME, beansVariable.getDataFormatId());

    String persistedValue = beansVariable.getRawValue();
    String expectedJson = toExpectedJsonArray(beans);
    JSONAssert.assertEquals(expectedJson, persistedValue, true);

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testFailingSerialization() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    FailingSerializationBean failingBean = new FailingSerializationBean("a String", 42, true);

    try {
      runtimeService.setVariable(instance.getId(), "simpleBean", failingBean);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testFailingDeserialization() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    FailingDeserializationBean failingBean = new FailingDeserializationBean("a String", 42, true);

    runtimeService.setVariable(instance.getId(), "simpleBean", failingBean);

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();

    assertNull(variableInstance.getValue());
    assertNotNull(variableInstance.getErrorMessage());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSettingVariableExceedingTextFieldLength() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // field TEXT is varchar(4000), i.e. 4000 byte
    int textFieldLength = 4000;

    SimpleBean bean = new SimpleBean("a String", 42, true);
    String expectedJson = bean.toExpectedJsonString();

    int expectedBytesPerBean = expectedJson.getBytes().length;
    int beansToExceedFieldLength = (textFieldLength / expectedBytesPerBean) + 1;

    List<SimpleBean> lengthExceedingBeans = new ArrayList<SimpleBean>();
    for (int i = 0; i < beansToExceedFieldLength; i++) {
      lengthExceedingBeans.add(new SimpleBean("a String", 42, true));
    }

    runtimeService.setVariable(instance.getId(), "simpleBeans", lengthExceedingBeans);

    VariableInstance beansVariable = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(beansVariable);
    assertEquals(DefaultSerializationFormatType.TYPE_NAME, beansVariable.getTypeName());

    List<SimpleBean> returnedBeans = (List<SimpleBean>) beansVariable.getValue();
    assertNotNull(returnedBeans);
    assertTrue(returnedBeans instanceof ArrayList);
    assertListsEqual(lengthExceedingBeans, returnedBeans);
  }

  public void testFailForNonExistingSerializationFormat() {
    ProcessEngineConfigurationImpl engineConfig =
        (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("camunda.cfg.xml");

    engineConfig.setDefaultSerializationFormat("an unknown data format");

    try {
      engineConfig.buildProcessEngine();
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  protected void assertListsEqual(List<SimpleBean> expectedBeans, List<SimpleBean> actualBeans) {
    assertEquals(expectedBeans.size(), actualBeans.size());

    for (int i = 0; i < actualBeans.size(); i++) {
      SimpleBean actualBean = actualBeans.get(i);
      SimpleBean expectedBean = expectedBeans.get(i);

      assertEquals(expectedBean.getStringProperty(), actualBean.getStringProperty());
      assertEquals(expectedBean.getIntProperty(), actualBean.getIntProperty());
      assertEquals(expectedBean.getBooleanProperty(), actualBean.getBooleanProperty());
    }
  }

  protected String toExpectedJsonArray(List<SimpleBean> beans) {
    StringBuilder jsonBuilder = new StringBuilder();

    jsonBuilder.append("[");
    for (int i = 0; i < beans.size(); i++) {
      jsonBuilder.append(beans.get(i).toExpectedJsonString());

      if (i != beans.size() - 1)  {
        jsonBuilder.append(", ");
      }
    }
    jsonBuilder.append("]");

    return jsonBuilder.toString();
  }


  // TODO: think about VariableInstance#getRawValue: should this return String or Object?
  // must also be delegated to variable type to return the raw value; what should this be for other formats?
  // is it a good contract to return different things depending on the variable type?
  //
  // also apply to HistoricVariableInstance!

  // TODO: what about async history handlers => Are history events sufficient atm?

  // TODO: test default format configuration by java

  // TODO: test default format configuration by string => should string value config be replaced? probably not;
  // as a user, I can simply use spin to work with json string

  // TODO: test additional properties (data format id and configuration) are written to history (HistoricVariableInstance as well as HistoricDetail (when variables are updated))

  // TODO: upgrade scripts (RU_VARIABLE and history)

  // TODO: ensure that variable instance query works (getRawValue()) without having the class at hand

  // TODO: test something that involves VariableType#getTypeNameForValue(), i.e. ensure it is meaningful for
  // default serialization format type

  // TODO: test execution.getVariable(..) as raw value

  // TODO: current impl disables JPAEntityVariableType

  // TODO: expose getConfiguration() in interface VariableInstance?

  // TODO: add dataformatid and configuration to persistent state in
  // VariableInstanceEntity and HistoricVariableInstanceEntity
}
