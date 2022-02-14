/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.api.authorization.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenarioWithCount;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author Askar Akhmerov
 */
@RunWith(Parameterized.class)
public class SetJobRetriesBatchAuthorizationTest extends AbstractBatchAuthorizationTest {

  protected static final String DEFINITION_XML = "org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml";
  protected static final long BATCH_OPERATIONS = 3;
  protected static final int RETRIES = 5;

  protected void assertRetries(List<String> allJobIds, int i) {
    for (String id : allJobIds) {
      assertThat(managementService.createJobQuery().jobId(id).singleResult().getRetries()).isEqualTo(i);
    }
  }

  protected List<String> getAllJobIds() {
    ArrayList<String> result = new ArrayList<String>();
    for (Job job : managementService.createJobQuery().processDefinitionId(sourceDefinition.getId()).list()) {
      if (job.getProcessInstanceId() != null) {
        result.add(job.getId());
      }
    }
    return result;
  }

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  @Parameterized.Parameter
  public AuthorizationScenarioWithCount scenario;

  @Override
  @Before
  public void deployProcesses() {
    Deployment deploy = testHelper.deploy(DEFINITION_XML);
    sourceDefinition = engineRule.getRepositoryService()
        .createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
    processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
    processInstance2 = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
  }


  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        AuthorizationScenarioWithCount.scenario()
            .withCount(3)
            .withAuthorizations(
                grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
                grant(Resources.PROCESS_INSTANCE, "processInstance1", "userId", Permissions.READ, Permissions.UPDATE),
                grant(Resources.PROCESS_INSTANCE, "processInstance2", "userId", Permissions.READ)
            )
            .failsDueToRequired(
                grant(Resources.PROCESS_INSTANCE, "processInstance2", "userId", Permissions.UPDATE),
                grant(Resources.PROCESS_DEFINITION, "exceptionInJobExecution", "userId", Permissions.UPDATE_INSTANCE),
                grant(Resources.PROCESS_INSTANCE, "processInstance2", "userId", ProcessInstancePermissions.RETRY_JOB),
                grant(Resources.PROCESS_DEFINITION, "exceptionInJobExecution", "userId", ProcessDefinitionPermissions.RETRY_JOB)
            ),
        AuthorizationScenarioWithCount.scenario()
            .withCount(5)
            .withAuthorizations(
                grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
                grant(Resources.PROCESS_INSTANCE, "processInstance1", "userId", Permissions.ALL),
                grant(Resources.PROCESS_INSTANCE, "processInstance2", "userId", Permissions.ALL)
            ).succeeds(),
        AuthorizationScenarioWithCount.scenario()
            .withCount(5)
            .withAuthorizations(
                grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
                grant(Resources.PROCESS_DEFINITION, "Process", "userId", Permissions.READ_INSTANCE, Permissions.UPDATE_INSTANCE)
            ).succeeds(),
        AuthorizationScenarioWithCount.scenario()
            .withCount(5)
            .withAuthorizations(
                grant(Resources.BATCH, "*", "userId", BatchPermissions.CREATE_BATCH_SET_JOB_RETRIES),
                grant(Resources.PROCESS_DEFINITION, "Process", "userId", Permissions.READ_INSTANCE, Permissions.UPDATE_INSTANCE)
            ).succeeds()
    );
  }

  @Test
  public void testWithTwoInvocationsJobsListBased() {
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);
    setupAndExecuteJobsListBasedTest();

    // then
    assertScenario();

    assertRetries(getAllJobIds(), Long.valueOf(getScenario().getCount()).intValue());
  }

  @Test
  public void testWithTwoInvocationsJobsQueryBased() {
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);
    setupAndExecuteJobsQueryBasedTest();

    // then
    assertScenario();

    assertRetries(getAllJobIds(), Long.valueOf(getScenario().getCount()).intValue());
  }

  @Test
  public void testJobsListBased() {
    setupAndExecuteJobsListBasedTest();
    // then
    assertScenario();
  }

  @Test
  public void testJobsListQueryBased() {
    setupAndExecuteJobsQueryBasedTest();
    // then
    assertScenario();
  }

  @Test
  public void testWithTwoInvocationsProcessListBased() {
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);
    setupAndExecuteProcessListBasedTest();

    // then
    assertScenario();

    assertRetries(getAllJobIds(), Long.valueOf(getScenario().getCount()).intValue());
  }

  @Test
  public void testWithTwoInvocationsProcessQueryBased() {
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);
    setupAndExecuteJobsQueryBasedTest();

    // then
    assertScenario();

    assertRetries(getAllJobIds(), Long.valueOf(getScenario().getCount()).intValue());
  }

  private void setupAndExecuteProcessListBasedTest() {
    //given
    List<String> processInstances = Arrays.asList(new String[]{processInstance.getId(), processInstance2.getId()});
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("Process", sourceDefinition.getKey())
        .bindResource("processInstance1", processInstance.getId())
        .bindResource("processInstance2", processInstance2.getId())
        .start();

    // when
    batch = managementService.setJobRetriesAsync(
        processInstances, (ProcessInstanceQuery) null, RETRIES);

    executeSeedAndBatchJobs();
  }

  @Test
  public void testProcessList() {
    setupAndExecuteProcessListBasedTest();
    // then
    assertScenario();
  }

  protected void setupAndExecuteJobsListBasedTest() {
    //given
    List<String> allJobIds = getAllJobIds();
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("Process", sourceDefinition.getKey())
        .bindResource("processInstance1", processInstance.getId())
        .bindResource("processInstance2", processInstance2.getId())
        .start();

    // when
    batch = managementService.setJobRetriesAsync(
        allJobIds, RETRIES);

    executeSeedAndBatchJobs();
  }

  protected void setupAndExecuteJobsQueryBasedTest() {
    //given
    JobQuery jobQuery = managementService.createJobQuery();
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("Process", sourceDefinition.getKey())
        .bindResource("processInstance1", processInstance.getId())
        .bindResource("processInstance2", processInstance2.getId())
        .start();

    // when

    batch = managementService.setJobRetriesAsync(
        jobQuery, RETRIES);

    executeSeedAndBatchJobs();
  }

  @Override
  public AuthorizationScenarioWithCount getScenario() {
    return scenario;
  }

  protected void assertScenario() {
    if (authRule.assertScenario(getScenario())) {
      Batch batch = engineRule.getManagementService().createBatchQuery().singleResult();
      assertEquals("userId", batch.getCreateUserId());

      if (testHelper.isHistoryLevelFull()) {
        assertThat(engineRule.getHistoryService().createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES).count())
          .isEqualTo(BATCH_OPERATIONS);
        HistoricBatch historicBatch = engineRule.getHistoryService().createHistoricBatchQuery().list().get(0);
        assertEquals("userId", historicBatch.getCreateUserId());
      }
      assertRetries(getAllJobIds(), 5);
    }
  }
}
