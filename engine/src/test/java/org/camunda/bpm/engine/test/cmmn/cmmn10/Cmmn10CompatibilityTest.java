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
package org.camunda.bpm.engine.test.cmmn.cmmn10;

import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class Cmmn10CompatibilityTest extends CmmnProcessEngineTestCase {

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testRequiredRule.cmmn")
  public void testRequiredRule() {
    CaseInstance caseInstance =
        createCaseInstanceByKey("case", Variables.createVariables().putValue("required", true));

    CaseExecution taskExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");

    assertNotNull(taskExecution);
    assertTrue(taskExecution.isRequired());

    try {
      caseService.completeCaseExecution(caseInstance.getId());
      fail("completing the containing stage should not be allowed");
    } catch (NotAllowedException e) {
      // happy path
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testManualActivationRule.cmmn")
  public void testManualActivationRule() {
    createCaseInstanceByKey("case", Variables.createVariables().putValue("manual", false));

    CaseExecution taskExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");

    assertNotNull(taskExecution);
    assertTrue(taskExecution.isActive());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testManualActivationRuleWithoutCondition.cmmn")
  public void testManualActivationRuleWithoutCondition() {
    createCaseInstanceByKey("case", Variables.createVariables().putValue("manual", false));

    CaseExecution taskExecution = queryCaseExecutionByActivityId("PI_HumanTask_1");

    assertNotNull(taskExecution);
    assertTrue(taskExecution.isEnabled());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testRepetitionRule.cmmn")
  public void testRepetitionRule() {
    // given
    createCaseInstanceByKey("case", Variables.createVariables().putValue("repetition", true));

    String secondHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_2").getId();

    // when
    complete(secondHumanTaskId);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(2, query.count());
    assertEquals(1, query.available().count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testRepetitionRuleWithoutEntryCriteria.cmmn")
  public void testRepetitionRuleWithoutEntryCriteria() {
    // given
    createCaseInstanceByKey("case", Variables.createVariables().putValue("repetition", true));

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(firstHumanTaskId);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(1, query.count());
    assertEquals(1, query.active().count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testRepetitionRuleCustomStandardEvent.cmmn")
  public void testRepetitionRuleWithoutEntryCriteriaAndCustomStandardEvent() {
    // given
    createCaseInstanceByKey("case", Variables.createVariables().putValue("repetition", true));

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    disable(firstHumanTaskId);

    // then
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1");
    assertEquals(2, query.count());
    assertEquals(1, query.enabled().count());
    assertEquals(1, query.disabled().count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testPlanItemEntryCriterion.cmmn")
  public void testPlanItemEntryCriterion() {
    // given
    createCaseInstanceByKey("case");
    String humanTask = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask);

    // then
    assertTrue(queryCaseExecutionByActivityId("PI_HumanTask_2").isActive());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testPlanItemExitCriterion.cmmn")
  public void testPlanItemExitCriterion() {
    // given
    createCaseInstanceByKey("case");

    String humanTask = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask);

    // then
    assertNull(queryCaseExecutionByActivityId("PI_HumanTask_2"));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testCasePlanModelExitCriterion.cmmn")
  public void testCasePlanModelExitCriterion() {
    // given
    String caseInstanceId = createCaseInstanceByKey("case").getId();

    String humanTask = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask);

    // then
    assertTrue(queryCaseExecutionById(caseInstanceId).isTerminated());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testSentryIfPartCondition.cmmn")
  public void testSentryIfPartCondition() {
    // given
    createCaseInstanceByKey("case", Variables.createVariables().putValue("value", 99));

    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();
    String humanTask2 = queryCaseExecutionByActivityId("PI_HumanTask_2").getId();

    assertTrue(queryCaseExecutionById(humanTask2).isAvailable());

    // when
    caseService
      .withCaseExecution(humanTask1)
      .setVariable("value", 999)
      .manualStart();

    // then
    assertTrue(queryCaseExecutionById(humanTask2).isEnabled());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/cmm10/Cmmn10CompatibilityTest.testDescription.cmmn")
  public void testDescription() {
    // given
    createCaseInstanceByKey("case");

    // when
    String humanTask = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // then
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("This is a description!", task.getDescription());

  }

}
