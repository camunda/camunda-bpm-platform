package org.camunda.bpm.engine.test.api.authorization.externaltask;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SetExternalTasksRetriesBatchAuthorizationTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule);

  @Parameter
  public AuthorizationScenario scenario;

  @Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE)),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
          grant(Resources.PROCESS_INSTANCE, "processInstanceId2", "userId", Permissions.UPDATE))
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.PROCESS_INSTANCE, "processInstanceId1", "userId", Permissions.UPDATE),
          grant(Resources.PROCESS_DEFINITION, "oneExternalTaskProcess", "userId", Permissions.UPDATE_INSTANCE)),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
          grant(Resources.PROCESS_INSTANCE, "processInstanceId1", "userId", Permissions.UPDATE),
          grant(Resources.PROCESS_INSTANCE, "processInstanceId2", "userId", Permissions.UPDATE))
        .succeeds(),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_INSTANCE, "processInstanceId1", "userId", Permissions.UPDATE),
          grant(Resources.PROCESS_INSTANCE, "processInstanceId2", "userId", Permissions.UPDATE))
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE)),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
          grant(Resources.PROCESS_INSTANCE, "*", "userId", Permissions.UPDATE))
        .succeeds(),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_INSTANCE, "*", "userId", Permissions.UPDATE))
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE)),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
          grant(Resources.PROCESS_DEFINITION, "processDefinitionKey", "userId", Permissions.UPDATE_INSTANCE))
        .succeeds(),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_DEFINITION, "processDefinitionKey", "userId", Permissions.UPDATE_INSTANCE))
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE)),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
          grant(Resources.PROCESS_DEFINITION, "*", "userId", Permissions.UPDATE_INSTANCE))
        .succeeds(),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_DEFINITION, "*", "userId", Permissions.UPDATE_INSTANCE))
        .withoutAuthorizations()
        .failsDueToRequired(
         grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE))
      );
  }

  @Before
  public void setUp() {
    authRule.createUserAndGroup("userId", "groupId");
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
  
  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  public void testSetRetriesAsync() {

    // given
    ProcessInstance processInstance1 = engineRule.getRuntimeService().startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = engineRule.getRuntimeService().startProcessInstanceByKey("oneExternalTaskProcess");
    List<ExternalTask> externalTasks = engineRule.getExternalTaskService().createExternalTaskQuery().list();
    
    ArrayList<String> externalTaskIds = new ArrayList<String>();
    
    for (ExternalTask task : externalTasks) {
      externalTaskIds.add(task.getId());
    }

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("batchId", "*")
      .bindResource("processInstanceId1", processInstance1.getId())
      .bindResource("processInstanceId2", processInstance2.getId())
      .bindResource("processDefinitionKey", "oneExternalTaskProcess")
      .start();

    Batch batch = engineRule.getExternalTaskService().setRetriesAsync(externalTaskIds, null, 5);
    if (batch != null) {
      executeSeedAndBatchJobs(batch);
    }
    
    // then
    if (authRule.assertScenario(scenario)) {
      externalTasks = engineRule.getExternalTaskService().createExternalTaskQuery().list();
      for ( ExternalTask task : externalTasks) {
      Assert.assertEquals(5, (int) task.getRetries());
      }  
    }
  }
  
  public void executeSeedAndBatchJobs(Batch batch) {
    Job job = engineRule.getManagementService().createJobQuery().jobDefinitionId(batch.getSeedJobDefinitionId()).singleResult();
    // seed job
    engineRule.getManagementService().executeJob(job.getId());

    for (Job pending : engineRule.getManagementService().createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list()) {
      engineRule.getManagementService().executeJob(pending.getId());
    }
  }
}
