package org.camunda.bpm.engine.test.api.authorization.batch;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestBaseRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
public class DeleteProcessInstancesBatchAuthorizationTest {
  protected static final String TEST_REASON = "test reason";
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestBaseRule authRule = new AuthorizationTestBaseRule(engineRule);
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  protected ProcessInstance processInstance;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  @Before
  public void setUp() {
    authRule.createUserAndGroup("user", "group");
  }

  @Before
  public void deployProcessesAndCreateMigrationPlan() {
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
  }

  @After
  public void cleanBatch() {
    Batch batch = engineRule.getManagementService().createBatchQuery().singleResult();
    if (batch != null) {
      engineRule.getManagementService().deleteBatch(
          batch.getId(),true);
    }

    HistoricBatch historicBatch = engineRule.getHistoryService().createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
      engineRule.getHistoryService().deleteHistoricBatch(
          historicBatch.getId());
    }
  }

  @After
  public void tearDown() {
    authRule.disableAuthorization();
    authRule.deleteUsersAndGroups();
  }

  @Test
  public void testBatchDeleteWithNoAuthorizations() {
    thrown.expect(AuthorizationException.class);
    thrown.expectMessage("The user with id 'user' does not have 'CREATE' permission on resource 'Batch'.");
    // when
    authRule.enableAuthorization("user");

    engineRule.getRuntimeService().deleteProcessInstancesAsync(Arrays.asList(processInstance.getId()),TEST_REASON);
  }

  @Test
  public void testBatchDeleteWithBatchCreateAuthorizations() {
    authRule.createGrantAuthorization(Resources.BATCH, "*", "user", Permissions.CREATE);
    thrown.expect(AuthorizationException.class);
    thrown.expectMessage(startsWith("The user with id 'user' does not have one of the following permissions: 'DELETE'"));
    // when
    authRule.enableAuthorization("user");

    engineRule.getRuntimeService().deleteProcessInstancesAsync(Arrays.asList(processInstance.getId()),TEST_REASON);
    engineRule.getManagementService().executeJob(engineRule.getManagementService().createJobQuery().singleResult().getId());
    //testHelper.executeAvailableJobs(); throws stack overflow exception due to job failure
    for (Job existing : engineRule.getManagementService().createJobQuery().list()) {
      engineRule.getManagementService().executeJob(existing.getId());
    }
  }

  @Test
  public void testBatchDeleteWithAllAuthorizations() {
    authRule.createGrantAuthorization(Resources.BATCH, "*", "user", Permissions.CREATE);
    authRule.createGrantAuthorization(Resources.PROCESS_INSTANCE, "*", "user", Permissions.ALL);
    // when
    authRule.enableAuthorization("user");

    engineRule.getRuntimeService().deleteProcessInstancesAsync(Arrays.asList(processInstance.getId()),TEST_REASON);
    engineRule.getManagementService().executeJob(
        engineRule.getManagementService().createJobQuery().singleResult().getId());
    testHelper.executeAvailableJobs();

    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(engineRule.getProcessEngineConfiguration().getHistory()) &&
        !ProcessEngineConfiguration.HISTORY_AUDIT.equals(engineRule.getProcessEngineConfiguration().getHistory()) ) {
      assertThat(engineRule.getHistoryService().createUserOperationLogQuery().count(), is(3l));
    }
  }

  @Test
  public void testBatchDeleteWithDeleteProcessAndBatchCreateAuthorizations() {
    authRule.createGrantAuthorization(Resources.BATCH, "*", "user", Permissions.CREATE);
    authRule.createGrantAuthorization(Resources.PROCESS_INSTANCE, "*", "user", Permissions.DELETE);
    // when
    authRule.enableAuthorization("user");

    engineRule.getRuntimeService().deleteProcessInstancesAsync(Arrays.asList(processInstance.getId()),TEST_REASON);
    engineRule.getManagementService().executeJob(
        engineRule.getManagementService().createJobQuery().singleResult().getId());
    testHelper.executeAvailableJobs();

    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(engineRule.getProcessEngineConfiguration().getHistory()) &&
        !ProcessEngineConfiguration.HISTORY_AUDIT.equals(engineRule.getProcessEngineConfiguration().getHistory()) ) {
      assertThat(engineRule.getHistoryService().createUserOperationLogQuery().count(), is(3l));
    }
  }

  @Test
  public void testBatchDeleteQueryWithBatchCreateAuthorizations() {
    authRule.createGrantAuthorization(Resources.BATCH, "*", "user", Permissions.CREATE);
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage(startsWith("processInstanceIds is empty"));
    // when
    authRule.enableAuthorization("user");

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId());
    engineRule.getRuntimeService().deleteProcessInstancesAsync(query,TEST_REASON);

    engineRule.getManagementService().executeJob(engineRule.getManagementService().createJobQuery().singleResult().getId());
    //testHelper.executeAvailableJobs(); throws stack overflow exception due to job failure
    for (Job existing : engineRule.getManagementService().createJobQuery().list()) {
      engineRule.getManagementService().executeJob(existing.getId());
    }
  }

  @Test
  public void testBatchDeleteQueryWithAllAuthorizations() {
    authRule.createGrantAuthorization(Resources.BATCH, "*", "user", Permissions.CREATE);
    authRule.createGrantAuthorization(Resources.PROCESS_INSTANCE, "*", "user", Permissions.ALL);
    // when
    authRule.enableAuthorization("user");

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId());
    engineRule.getRuntimeService().deleteProcessInstancesAsync(query,TEST_REASON);

    engineRule.getManagementService().executeJob(
        engineRule.getManagementService().createJobQuery().singleResult().getId());
    testHelper.executeAvailableJobs();

    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(engineRule.getProcessEngineConfiguration().getHistory()) &&
        !ProcessEngineConfiguration.HISTORY_AUDIT.equals(engineRule.getProcessEngineConfiguration().getHistory()) ) {
      assertThat(engineRule.getHistoryService().createUserOperationLogQuery().count(), is(3l));
    }
  }

  @Test
  public void testBatchDeleteQueryWithDeleteProcessAndBatchCreateAuthorizations() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");
    authRule.createGrantAuthorization(Resources.BATCH, "*", "user", Permissions.CREATE);
    authRule.createGrantAuthorization(Resources.PROCESS_INSTANCE, "*", "user", Permissions.DELETE);
    // when
    authRule.enableAuthorization("user");

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId());
    engineRule.getRuntimeService().deleteProcessInstancesAsync(query,TEST_REASON);

  }

  @Test
  public void testBatchDeleteQueryWithDeleteProcessAndBatchCreateAndProcessInstanceReadAuthorizations() {
    authRule.createGrantAuthorization(Resources.BATCH, "*", "user", Permissions.CREATE);
    authRule.createGrantAuthorization(Resources.PROCESS_INSTANCE, "*", "user", Permissions.DELETE,Permissions.READ);
    // when
    authRule.enableAuthorization("user");

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId());
    engineRule.getRuntimeService().deleteProcessInstancesAsync(query,TEST_REASON);

    engineRule.getManagementService().executeJob(
        engineRule.getManagementService().createJobQuery().singleResult().getId());
    testHelper.executeAvailableJobs();
    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(engineRule.getProcessEngineConfiguration().getHistory()) &&
        !ProcessEngineConfiguration.HISTORY_AUDIT.equals(engineRule.getProcessEngineConfiguration().getHistory()) ) {
      assertThat(engineRule.getHistoryService().createUserOperationLogQuery().count(), is(3l));
    }
  }

}
