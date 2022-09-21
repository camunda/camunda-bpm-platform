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
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import com.google.common.collect.Sets;

public class UpdateProcessInstancesSuspendStateAsyncTest {

  protected static final Date TEST_DATE = new Date(1457326800000L);

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected BatchSuspensionHelper helper = new BatchSuspensionHelper(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }



  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchSuspensionById() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId())).suspendAsync();
    helper.completeSeedJobs(suspendprocess);
    helper.executeJobs(suspendprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }

  @Test
  public void testBatchSuspensionByIdsInDifferentDeployments() {
    // given
    String deploymentId1 = testRule.deployAndGetDefinition("org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml").getDeploymentId();
    String deploymentId2 = testRule.deployAndGetDefinition("org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml").getDeploymentId();
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId())).suspendAsync();

    // then
    Job seedJob = helper.getSeedJob(suspendprocess);
    assertThat(seedJob.getDeploymentId()).isIn(deploymentId1, deploymentId2);

    // when
    helper.completeSeedJobs(suspendprocess);

    // then
    List<Job> batchJobs = helper.getExecutionJobs(suspendprocess);
    assertThat(batchJobs).hasSize(2);
    assertThat(batchJobs.get(0).getDeploymentId()).isIn(deploymentId1, deploymentId2);
    assertThat(batchJobs.get(1).getDeploymentId()).isIn(deploymentId1, deploymentId2);
    assertThat(batchJobs.get(0).getDeploymentId()).isNotEqualTo(batchJobs.get(1).getDeploymentId());

    // when
    helper.completeExecutionJobs(suspendprocess);

    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void shouldSetInvocationsPerBatchTypeOnSuspension() {
    // given
    engineRule.getProcessEngineConfiguration()
        .getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_PROCESS_INSTANCE_UPDATE_SUSPENSION_STATE, 42);

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch batch = runtimeService.updateProcessInstanceSuspensionState()
        .byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId()))
        .suspendAsync();

    // then
    assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(42);

    // clear
    engineRule.getProcessEngineConfiguration()
        .setInvocationsPerBatchJobByBatchType(new HashMap<>());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void shouldSetInvocationsPerBatchTypeOnActivation() {
    // given
    engineRule.getProcessEngineConfiguration()
        .getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_PROCESS_INSTANCE_UPDATE_SUSPENSION_STATE, 42);

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch batch = runtimeService.updateProcessInstanceSuspensionState()
        .byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId()))
        .activateAsync();

    // then
    assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(42);

    // clear
    engineRule.getProcessEngineConfiguration()
        .setInvocationsPerBatchJobByBatchType(new HashMap<>());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchActivationById() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId())).suspendAsync();
    helper.completeSeedJobs(suspendprocess);
    helper.executeJobs(suspendprocess);
    Batch activateprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId())).activateAsync();
    helper.completeSeedJobs(activateprocess);
    helper.executeJobs(activateprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(p2c.isSuspended());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchSuspensionByProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceQuery(runtimeService.createProcessInstanceQuery().active()).suspendAsync();
    helper.completeSeedJobs(suspendprocess);
    helper.executeJobs(suspendprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testBatchActivationByProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceQuery(runtimeService.createProcessInstanceQuery().active()).suspendAsync();
    helper.completeSeedJobs(suspendprocess);
    helper.executeJobs(suspendprocess);
    Batch activateprocess = runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceQuery(runtimeService.createProcessInstanceQuery().suspended()).activateAsync();
    helper.completeSeedJobs(activateprocess);
    helper.executeJobs(activateprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(p2c.isSuspended());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testBatchSuspensionByHistoricProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byHistoricProcessInstanceQuery(historyService.createHistoricProcessInstanceQuery().processInstanceIds(Sets.newHashSet(processInstance1.getId(), processInstance2.getId()))).suspendAsync();
    helper.completeSeedJobs(suspendprocess);
    helper.executeJobs(suspendprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertTrue(p2c.isSuspended());

  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testBatchActivationByHistoricProcessInstanceQuery() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState().byHistoricProcessInstanceQuery(historyService.createHistoricProcessInstanceQuery().processInstanceIds(Sets.newHashSet(processInstance1.getId(), processInstance2.getId()))).suspendAsync();
    helper.completeSeedJobs(suspendprocess);
    helper.executeJobs(suspendprocess);
    Batch activateprocess = runtimeService.updateProcessInstanceSuspensionState().byHistoricProcessInstanceQuery(historyService.createHistoricProcessInstanceQuery().processInstanceIds(Sets.newHashSet(processInstance1.getId(), processInstance2.getId()))).activateAsync();
    helper.completeSeedJobs(activateprocess);
    helper.executeJobs(activateprocess);


    // then
    ProcessInstance p1c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(p1c.isSuspended());
    ProcessInstance p2c = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(p2c.isSuspended());
  }

  @Test
  public void testEmptyProcessInstanceListSuspendAsync() {
    // given
    // nothing

    // when/then
    assertThatThrownBy(() -> runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds().suspendAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("No process instance ids given");
  }

  @Test
  public void testEmptyProcessInstanceListActivateAsync() {
    // given
    // nothing

    // when/then
    assertThatThrownBy(() -> runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds().activateAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("No process instance ids given");
  }


  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testNullProcessInstanceListActivateAsync() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when/then
    assertThatThrownBy(() -> runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId(), null)).activateAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("Cannot be null");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  public void testNullProcessInstanceListSuspendAsync() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when/then
    assertThatThrownBy(() -> runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId(), null)).suspendAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("Cannot be null");

  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldSetExecutionStartTimeInBatchAndHistory() {
    // given
    ClockUtil.setCurrentTime(TEST_DATE);
    testRule.deployAndGetDefinition("org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
        .getDeploymentId();
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    Batch batch = runtimeService.updateProcessInstanceSuspensionState()
        .byProcessInstanceIds(processInstance1.getId())
        .suspendAsync();
    helper.executeSeedJob(batch);
    List<Job> executionJobs = helper.getExecutionJobs(batch);

    // when
    helper.executeJob(executionJobs.get(0));

    // then
    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    batch = engineRule.getManagementService().createBatchQuery().singleResult();

    Assertions.assertThat(batch.getExecutionStartTime()).isEqualToIgnoringMillis(TEST_DATE);
    Assertions.assertThat(historicBatch.getExecutionStartTime()).isEqualToIgnoringMillis(TEST_DATE);
  }

}
