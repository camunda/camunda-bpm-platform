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
package org.camunda.bpm.engine.test.cmmn.activation;

import java.util.Collections;

import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class ManualActivationRuleTest extends CmmnProcessEngineTestCase {

  /**
   * CAM-3170
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/activation/ManualActivationRuleTest.testVariableBasedRule.cmmn")
  public void FAILING_testManualActivationRuleEvaluatesToTrue() {
    caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("manualActivation", true));

    CaseExecution taskExecution = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);
    assertTrue(taskExecution.isEnabled());
    assertFalse(taskExecution.isActive());
  }

  /**
   * CAM-3170
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/activation/ManualActivationRuleTest.testVariableBasedRule.cmmn")
  public void FAILING_testManualActivationRuleEvaluatesToFalse() {
    caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("manualActivation", false));

    CaseExecution taskExecution = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);
    assertFalse(taskExecution.isEnabled());
    assertTrue(taskExecution.isActive());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/activation/ManualActivationRuleTest.testDefaultVariableBasedRule.cmmn")
  public void testDefaultManualActivationRuleEvaluatesToTrue() {
    caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("manualActivation", true));

    CaseExecution taskExecution = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);
    assertTrue(taskExecution.isEnabled());
    assertFalse(taskExecution.isActive());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/activation/ManualActivationRuleTest.testDefaultVariableBasedRule.cmmn")
  public void testDefaultManualActivationRuleEvaluatesToFalse() {
    caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("manualActivation", false));

    CaseExecution taskExecution = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    assertNotNull(taskExecution);
    assertFalse(taskExecution.isEnabled());
    assertTrue(taskExecution.isActive());
  }

}
