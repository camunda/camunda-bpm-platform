package org.camunda.bpm.engine.test.api.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class ModificationUserOperationLogTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);
  protected BatchModificationHelper helper = new BatchModificationHelper(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  protected RuntimeService runtimeService;
  protected BpmnModelInstance instance;
  protected static final Date START_DATE = new Date(1457326800000L);

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
  }

  @Before
  public void setClock() {
    ClockUtil.setCurrentTime(START_DATE);
  }

  @Before
  public void createBpmnModelInstance() {
    this.instance = Bpmn.createExecutableProcess("process1")
        .startEvent("start")
        .userTask("user1")
        .sequenceFlowId("seq")
        .userTask("user2")
        .endEvent("end")
        .done();
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @After
  public void removeInstanceIds() {
    helper.currentProcessInstances = new ArrayList<String>();
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }
  @Test
  public void testLogCreation() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    rule.getIdentityService().setAuthenticatedUserId("userId");

    // when
    helper.startBeforeAsync("process1", 10, "user2", processDefinition.getId());
    rule.getIdentityService().clearAuthentication();

    // then
    List<UserOperationLogEntry> opLogEntries = rule.getHistoryService().createUserOperationLogQuery().list();
    Assert.assertEquals(2, opLogEntries.size());

    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);


    UserOperationLogEntry asyncEntry = entries.get("async");
    Assert.assertNotNull(asyncEntry);
    Assert.assertEquals("ProcessInstance", asyncEntry.getEntityType());
    Assert.assertEquals("ModifyProcessInstance", asyncEntry.getOperationType());
    Assert.assertEquals(processDefinition.getId(), asyncEntry.getProcessDefinitionId());
    Assert.assertEquals(processDefinition.getKey(), asyncEntry.getProcessDefinitionKey());
    Assert.assertNull(asyncEntry.getProcessInstanceId());
    Assert.assertNull(asyncEntry.getOrgValue());
    Assert.assertEquals("true", asyncEntry.getNewValue());

    UserOperationLogEntry numInstancesEntry = entries.get("nrOfInstances");
    Assert.assertNotNull(numInstancesEntry);
    Assert.assertEquals("ProcessInstance", numInstancesEntry.getEntityType());
    Assert.assertEquals("ModifyProcessInstance", numInstancesEntry.getOperationType());
    Assert.assertEquals(processDefinition.getId(), numInstancesEntry.getProcessDefinitionId());
    Assert.assertEquals(processDefinition.getKey(), numInstancesEntry.getProcessDefinitionKey());
    Assert.assertNull(numInstancesEntry.getProcessInstanceId());
    Assert.assertNull(numInstancesEntry.getOrgValue());
    Assert.assertEquals("10", numInstancesEntry.getNewValue());

    Assert.assertEquals(asyncEntry.getOperationId(), numInstancesEntry.getOperationId());
  }

  @Test
  public void testNoCreationOnSyncBatchJobExecution() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    Batch batch = runtimeService.createModification(processDefinition.getId())
      .startAfterActivity("user2")
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    helper.executeSeedJob(batch);

    // when
    rule.getIdentityService().setAuthenticatedUserId("userId");
    helper.executeJobs(batch);
    rule.getIdentityService().clearAuthentication();

    // then
    Assert.assertEquals(0, rule.getHistoryService().createUserOperationLogQuery().count());
  }

  @Test
  public void testNoCreationOnJobExecutorBatchJobExecution() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    runtimeService.createModification(processDefinition.getId())
      .cancelAllForActivity("user1")
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    // when
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    Assert.assertEquals(0, rule.getHistoryService().createUserOperationLogQuery().count());
  }

  protected Map<String, UserOperationLogEntry> asMap(List<UserOperationLogEntry> logEntries) {
    Map<String, UserOperationLogEntry> map = new HashMap<String, UserOperationLogEntry>();

    for (UserOperationLogEntry entry : logEntries) {

      UserOperationLogEntry previousValue = map.put(entry.getProperty(), entry);
      if (previousValue != null) {
        Assert.fail("expected only entry for every property");
      }
    }

    return map;
  }
}
