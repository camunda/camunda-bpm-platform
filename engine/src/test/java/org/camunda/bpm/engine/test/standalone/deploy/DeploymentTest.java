package org.camunda.bpm.engine.test.standalone.deploy;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DeploymentTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    @Override
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setJdbcUrl("jdbc:h2:mem:DeploymentTest-HistoryLevelNone;DB_CLOSE_DELAY=1000");
      configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
      configuration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_NONE);
      configuration.setDbHistoryUsed(false);
      return configuration;
    }
  };

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  @Test
  public void shouldDeleteDeployment() {
     BpmnModelInstance instance = Bpmn.createExecutableProcess("process").startEvent().endEvent().done();

     DeploymentWithDefinitions deployment = engineRule.getRepositoryService()
         .createDeployment()
         .addModelInstance("foo.bpmn", instance)
         .deployWithResult();

     engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);

     long count = engineRule.getRepositoryService().createDeploymentQuery().count();
     assertEquals(0, count);
  }


}
