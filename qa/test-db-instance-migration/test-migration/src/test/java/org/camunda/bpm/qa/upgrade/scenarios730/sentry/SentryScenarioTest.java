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
package org.camunda.bpm.qa.upgrade.scenarios730.sentry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
@ScenarioUnderTest("SentryScenario")
@Origin("7.3.0")
public class SentryScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  protected CaseService caseService;

  @Before
  public void setUp() {
    ProcessEngine processEngine = rule.getProcessEngine();
    caseService = processEngine.getCaseService();
  }

  @Test
  @ScenarioUnderTest("triggerTaskEntryCriterion.1")
  public void testTriggerTaskEntryCriterion() {
    // given
    // enabled human task inside a stage instance
    String firstHumanTaskId = rule.caseExecutionQuery().activityId("PI_HumanTask_1").singleResult().getId();

    // when
    // start and complete human task
    caseService.manuallyStartCaseExecution(firstHumanTaskId);
    caseService.completeCaseExecution(firstHumanTaskId);

    // then
    // entry criterion of the second human task inside the stage instance
    // will be triggered
    CaseExecution secondHumanTask = rule.caseExecutionQuery().activityId("PI_HumanTask_2").singleResult();
    // ... and the task is enabled
    assertTrue(secondHumanTask.isEnabled());

    CaseSentryPartEntity sentryPart = createCaseSentryPartQuery()
        .sourceCaseExecutionId(firstHumanTaskId)
        .singleResult();
    // the associated sentry part is not satisfied
    assertFalse(sentryPart.isSatisfied());
    // the source is null (because this sentry part
    // has been migrated into 7.4)
    assertNull(sentryPart.getSource());
  }

  @Test
  @ScenarioUnderTest("triggerStageEntryCriterion.1")
  public void testTriggerStageEntryCriterion() {
    // given
    String secondHumanTaskId = rule.caseExecutionQuery().activityId("PI_HumanTask_2").singleResult().getId();
    String firstStageId = rule.caseExecutionQuery().activityId("PI_Stage_1").singleResult().getId();

    // when
    // complete human task
    caseService.completeCaseExecution(secondHumanTaskId);

    // then
    // "PI_Stage_1" should be completed
    CaseExecution firstStage = rule.caseExecutionQuery().activityId("PI_Stage_1").singleResult();
    assertNull(firstStage);

    // "PI_Stage_2" should be enabled
    CaseExecution secondStage = rule.caseExecutionQuery().activityId("PI_Stage_2").singleResult();
    assertNotNull(secondStage);
    assertTrue(secondStage.isEnabled());

    CaseSentryPartEntity sentryPart = createCaseSentryPartQuery()
        .sourceCaseExecutionId(firstStageId)
        .singleResult();
    // the associated sentry part is not satisfied
    assertFalse(sentryPart.isSatisfied());
    // the source is null (since this sentry part
    // has been migrated into 7.4)
    assertNull(sentryPart.getSource());
  }

  @Test
  @ScenarioUnderTest("newSentryInstance.1")
  public void testNewSentryInstance() {
    // given
    String secondStageId = rule.caseExecutionQuery().activityId("PI_Stage_2").singleResult().getId();

    // when
    // start human task
    caseService.manuallyStartCaseExecution(secondStageId);

    // then
    // a new sentry instance should be created
    CaseSentryPartEntity sentryPart = createCaseSentryPartQuery()
      .caseExecutionId(secondStageId)
      .singleResult();
    assertNotNull(sentryPart);
    assertFalse(sentryPart.isSatisfied());
    assertNull(sentryPart.getSourceCaseExecutionId());
    assertEquals("PI_HumanTask_1", sentryPart.getSource());
  }

  @Test
  @ScenarioUnderTest("completeInstance.1")
  public void testCompleteInstance() {
    // given

    // when
    String firstHumanTaskId = rule.caseExecutionQuery().activityId("PI_HumanTask_1").singleResult().getId();
    caseService.manuallyStartCaseExecution(firstHumanTaskId);
    caseService.completeCaseExecution(firstHumanTaskId);

    String secondHumanTaskId = rule.caseExecutionQuery().activityId("PI_HumanTask_2").singleResult().getId();
    caseService.manuallyStartCaseExecution(secondHumanTaskId);
    caseService.completeCaseExecution(secondHumanTaskId);

    String secondStageId = rule.caseExecutionQuery().activityId("PI_Stage_2").singleResult().getId();
    caseService.manuallyStartCaseExecution(secondStageId);

    firstHumanTaskId = rule.caseExecutionQuery().activityId("PI_HumanTask_1").singleResult().getId();
    caseService.manuallyStartCaseExecution(firstHumanTaskId);
    caseService.completeCaseExecution(firstHumanTaskId);

    secondHumanTaskId = rule.caseExecutionQuery().activityId("PI_HumanTask_2").singleResult().getId();
    caseService.manuallyStartCaseExecution(secondHumanTaskId);
    caseService.completeCaseExecution(secondHumanTaskId);

    // then
    CaseInstance caseInstance = rule.caseInstance();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());

    caseService.closeCaseInstance(caseInstance.getId());
    assertNull(rule.processInstanceQuery().singleResult());
  }

  // queries /////////////////////////////////

  protected CaseSentryPartQueryImpl createCaseSentryPartQuery() {
    ProcessEngine processEngine = rule.getProcessEngine();
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
    return new CaseSentryPartQueryImpl(commandExecutor);
  }

}
