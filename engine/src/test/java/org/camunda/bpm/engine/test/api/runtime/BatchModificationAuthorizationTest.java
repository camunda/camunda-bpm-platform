package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BatchModificationAuthorizationTest {

  protected static final String TEST_REASON = "test reason";
  protected static final String JOB_EXCEPTION_DEFINITION_XML = "org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected BatchModificationHelper helper = new BatchModificationHelper(engineRule);
  
  protected ProcessInstance processInstance;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testRule);

  @Parameterized.Parameter
  public AuthorizationScenario scenario;

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
                grant(Resources.PROCESS_INSTANCE, "processInstance1", "userId", Permissions.READ, Permissions.UPDATE),
                grant(Resources.PROCESS_INSTANCE, "processInstance2", "userId", Permissions.READ, Permissions.UPDATE)
            ).succeeds(),
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
                grant(Resources.PROCESS_INSTANCE, "processInstance1", "userId", Permissions.READ, Permissions.UPDATE),
                grant(Resources.PROCESS_INSTANCE, "processInstance2", "userId", Permissions.READ)
            ).failsDueToRequired(
                grant(Resources.PROCESS_INSTANCE, "processInstance2", "userId", Permissions.UPDATE),
                grant(Resources.PROCESS_DEFINITION, "processDefinition", "userId", Permissions.UPDATE_INSTANCE))
            .succeeds()
    );
  }

  @Before
  public void deployProcess() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @After
  public void cleanBatch() {
    Batch batch = engineRule.getManagementService().createBatchQuery().singleResult();
    if (batch != null) {
      engineRule.getManagementService().deleteBatch(
          batch.getId(), true);
    }

    HistoricBatch historicBatch = engineRule.getHistoryService().createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
      engineRule.getHistoryService().deleteHistoricBatch(
          historicBatch.getId());
    }
  }
  
  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }
  

  @Test
  public void executeBatchModification() {
    //given
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process1").startEvent().userTask("user1").userTask("user2").endEvent().done();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    
    ProcessInstance processInstance1 = engineRule.getRuntimeService().startProcessInstanceByKey("process1");
    ProcessInstance processInstance2 = engineRule.getRuntimeService().startProcessInstanceByKey("process1");
    
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("processInstance1", processInstance1.getId())
        .bindResource("processInstance2", processInstance2.getId())
        .bindResource("processDefinition", "process1")
        .bindResource("batchId", "*")
        .start();
    
    Batch batch = engineRule.getRuntimeService()
        .createModification(processDefinition.getId())
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .startAfterActivity("user2")
        .executeAsync();

    Job job = engineRule.getManagementService().createJobQuery()
        .jobDefinitionId(batch.getSeedJobDefinitionId())
        .singleResult();
    
    //seed job
    engineRule.getManagementService().executeJob(job.getId());

    for (Job pending : engineRule.getManagementService().createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list()) {
      engineRule.getManagementService().executeJob(pending.getId());
    }
    
    // then
    authRule.assertScenario(scenario);
  }
}
