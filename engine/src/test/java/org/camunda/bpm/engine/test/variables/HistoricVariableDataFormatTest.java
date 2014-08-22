package org.camunda.bpm.engine.test.variables;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.SerializedVariableTypes;
import org.camunda.bpm.engine.delegate.SerializedVariableValue;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.spin.DataFormats;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

public class HistoricVariableDataFormatTest extends AbstractProcessEngineTestCase {

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
  public void testSelectHistoricVariableInstances() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    SimpleBean bean = new SimpleBean("a String", 42, false);
    runtimeService.setVariable(instance.getId(), "simpleBean", bean);

    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertNotNull(historicVariable.getValue());
    assertNull(historicVariable.getErrorMessage());

    SimpleBean historyValue = (SimpleBean) historicVariable.getValue();
    assertEquals(bean.getStringProperty(), historyValue.getStringProperty());
    assertEquals(bean.getIntProperty(), historyValue.getIntProperty());
    assertEquals(bean.getBooleanProperty(), historyValue.getBooleanProperty());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSelectHistoricSerializedValues() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    SimpleBean bean = new SimpleBean("a String", 42, false);
    runtimeService.setVariable(instance.getId(), "simpleBean", bean);

    SerializedVariableValue historicValue =
        historyService.createHistoricVariableInstanceQuery().singleResult().getSerializedValue();
    assertNotNull(historicValue);

    Map<String, Object> config = historicValue.getConfig();
    assertEquals(2, config.size());
    assertEquals(JSON_FORMAT_NAME, config.get(SerializedVariableTypes.SPIN_TYPE_DATA_FORMAT_ID));
    assertEquals(bean.getClass().getCanonicalName(), config.get(SerializedVariableTypes.SPIN_TYPE_CONFIG_ROOT_TYPE));

    String variableAsJson = (String) historicValue.getValue();
    JSONAssert.assertEquals(bean.toExpectedJsonString(), variableAsJson, true);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSelectHistoricSerializedValuesUpdate() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    SimpleBean bean = new SimpleBean("a String", 42, false);
    runtimeService.setVariable(instance.getId(), "simpleBean", bean);

    if (ProcessEngineConfiguration.HISTORY_FULL.equals(processEngineConfiguration.getHistory())) {

      HistoricVariableUpdate historicUpdate = (HistoricVariableUpdate)
          historyService.createHistoricDetailQuery().variableUpdates().singleResult();
      SerializedVariableValue serializedValue = historicUpdate.getSerializedValue();
      assertNotNull(serializedValue);

      Map<String, Object> config = serializedValue.getConfig();
      assertEquals(2, config.size());
      assertEquals(JSON_FORMAT_NAME, config.get(SerializedVariableTypes.SPIN_TYPE_DATA_FORMAT_ID));
      assertEquals(bean.getClass().getCanonicalName(), config.get(SerializedVariableTypes.SPIN_TYPE_CONFIG_ROOT_TYPE));

      String variableAsJson = (String) serializedValue.getValue();
      JSONAssert.assertEquals(bean.toExpectedJsonString(), variableAsJson, true);
    }


  }



}
