/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.upgrade.scenarios.sentry;

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
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
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
  @ScenarioUnderTest("SentryScenario.triggerTaskEntryCriterion.1")
  public void testTriggerTaskEntryCriterion() {
    // given
    // enabled human task inside a stage instance
    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();
    
    // when
    // start and complete human task
    caseService.manuallyStartCaseExecution(firstHumanTaskId);
    caseService.completeCaseExecution(firstHumanTaskId);
    
    // then
    // entry criterion of the second human task inside the stage instance
    // will be triggered
    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("PI_HumanTask_2");
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
  @ScenarioUnderTest("SentryScenario.triggerStageEntryCriterion.1")
  public void testTriggerStageEntryCriterion() {
    // given
    String secondHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_2").getId();
    String firstStageId = queryCaseExecutionByActivityId("PI_Stage_1").getId();
    
    // when
    // complete human task
    caseService.completeCaseExecution(secondHumanTaskId);
    
    // then
    // "PI_Stage_1" should be completed
    CaseExecution firstStage = queryCaseExecutionByActivityId("PI_Stage_1");
    assertNull(firstStage);
    
    // "PI_Stage_2" should be enabled
    CaseExecution secondStage = queryCaseExecutionByActivityId("PI_Stage_2");
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
  @ScenarioUnderTest("SentryScenario.newSentryInstance.1")
  public void testNewSentryInstance() {
    // given
    String secondStageId = queryCaseExecutionByActivityId("PI_Stage_2").getId();
    
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
  @ScenarioUnderTest("SentryScenario.completeInstance.1")
  public void testCompleteInstance() {
    // given
    
    // when
    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();
    caseService.manuallyStartCaseExecution(firstHumanTaskId);
    caseService.completeCaseExecution(firstHumanTaskId);

    String secondHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_2").getId();
    caseService.manuallyStartCaseExecution(secondHumanTaskId);
    caseService.completeCaseExecution(secondHumanTaskId);
    
    String secondStageId = queryCaseExecutionByActivityId("PI_Stage_2").getId();
    caseService.manuallyStartCaseExecution(secondStageId);

    firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();
    caseService.manuallyStartCaseExecution(firstHumanTaskId);
    caseService.completeCaseExecution(firstHumanTaskId);

    secondHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_2").getId();
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
  
  protected CaseExecution queryCaseExecutionByActivityId(String activityId) {
    String caseInstanceId = rule.caseInstance().getId();
    return caseService
      .createCaseExecutionQuery()
      .activityId(activityId)
      .caseInstanceId(caseInstanceId)
      .singleResult();
  }

  protected CaseSentryPartQueryImpl createCaseSentryPartQuery() {
    ProcessEngine processEngine = rule.getProcessEngine();
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
    return new CaseSentryPartQueryImpl(commandExecutor);
  }

}
