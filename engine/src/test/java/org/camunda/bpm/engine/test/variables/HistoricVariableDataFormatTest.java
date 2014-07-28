package org.camunda.bpm.engine.test.variables;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
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
    assertEquals(JSON_FORMAT_NAME, historicVariable.getDataFormatId());

    SimpleBean historyValue = (SimpleBean) historicVariable.getValue();
    assertEquals(bean.getStringProperty(), historyValue.getStringProperty());
    assertEquals(bean.getIntProperty(), historyValue.getIntProperty());
    assertEquals(bean.getBooleanProperty(), historyValue.getBooleanProperty());

    JSONAssert.assertEquals(bean.toExpectedJsonString(), (String) historicVariable.getRawValue(), true);
  }

}
