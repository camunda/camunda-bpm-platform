package org.camunda.bpm.engine.test.api.cfg;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Svetlana Dorokhova.
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class PersistenceExceptionTest {

  public ProcessEngineRule engineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  private RuntimeService runtimeService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  public void testPersistenceExceptionContainsRealCause() {
    Assume.assumeFalse(engineRule.getProcessEngineConfiguration().getDatabaseType().equals(DbSqlSessionFactory.MARIADB));
    StringBuffer longString = new StringBuffer();
    for (int i = 0; i < 100; i++) {
      longString.append("tensymbols");
    }
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process1").startEvent().userTask(longString.toString()).endEvent().done();
    testRule.deploy(modelInstance);
    try {
      runtimeService.startProcessInstanceByKey("process1").getId();
      fail("persistence exception is expected");
    } catch (ProcessEngineException ex) {
      assertTrue(ex.getMessage().contains("insertHistoricTaskInstanceEvent"));
    }
  }

}
