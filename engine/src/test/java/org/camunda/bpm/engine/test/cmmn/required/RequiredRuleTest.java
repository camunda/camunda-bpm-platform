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
package org.camunda.bpm.engine.test.cmmn.required;

import java.util.Collections;

import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Roman Smirnov
 *
 */
public class RequiredRuleTest extends CmmnProcessEngineTestCase {

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/required/RequiredRuleTest.testVariableBasedRule.cmmn")
  public void testRequiredRuleEvaluatesToTrue() {
    CaseInstance caseInstance =
        caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("required", true));

    CaseExecution taskExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();
    assertNotNull(taskExecution);
    assertTrue(taskExecution.isRequired());

    try {
      caseService.completeCaseExecution(caseInstance.getId());
      fail("completing the containing stage should not be allowed");
    } catch (NotAllowedException e) {
      // happy path
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/required/RequiredRuleTest.testVariableBasedRule.cmmn")
  public void testRequiredRuleEvaluatesToFalse() {
    CaseInstance caseInstance =
        caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("required", false));

    CaseExecution taskExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(taskExecution);
    assertFalse(taskExecution.isRequired());

    // completing manually should be allowed
    caseService.completeCaseExecution(caseInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/required/RequiredRuleTest.testDefaultVariableBasedRule.cmmn")
  public void testDefaultRequiredRuleEvaluatesToTrue() {
    CaseInstance caseInstance =
        caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("required", true));

    CaseExecution taskExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(taskExecution);
    assertTrue(taskExecution.isRequired());

    try {
      caseService.completeCaseExecution(caseInstance.getId());
      fail("completing the containing stage should not be allowed");
    } catch (NotAllowedException e) {
      // happy path
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/required/RequiredRuleTest.testDefaultVariableBasedRule.cmmn")
  public void testDefaultRequiredRuleEvaluatesToFalse() {
    CaseInstance caseInstance =
        caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("required", false));

    CaseExecution taskExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(taskExecution);
    assertFalse(taskExecution.isRequired());

    // completing manually should be allowed
    caseService.completeCaseExecution(caseInstance.getId());
  }

  @Deployment
  public void testDefaultRequiredRuleWithoutConditionEvaluatesToTrue() {
    caseService.createCaseInstanceByKey("case");

    CaseExecution taskExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertThat(taskExecution, is(notNullValue()));
    assertThat(taskExecution.isRequired(), is(true));
  }

  @Deployment
  public void testDefaultRequiredRuleWithEmptyConditionEvaluatesToTrue() {
    caseService.createCaseInstanceByKey("case");

    CaseExecution taskExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertThat(taskExecution, is(notNullValue()));
    assertThat(taskExecution.isRequired(), is(true));
  }
}
