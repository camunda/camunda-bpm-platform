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
package org.camunda.bpm.engine.test.cmmn.sentry;

import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.test.Deployment;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public class SentryVariableOnPartEntryCriteriaTest extends CmmnProcessEngineTestCase {

  // Basic tests - create, update, delete variable
  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testSimpleVariableOnPart.cmmn"})
  public void testVariableCreate() {
    
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    assertFalse(firstHumanTask.isEnabled());

    caseService.setVariable(caseInstanceId, "variable_1", "aVariable");
    firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(firstHumanTask.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testSimpleVariableOnPart.cmmn"})
  public void testUnknownVariableCreate() {
    
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    caseService.setVariable(caseInstanceId, "unknown", "aVariable");
    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    assertFalse(firstHumanTask.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testVariableUpdate.cmmn"})
  public void testVariableUpdate() {
    
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    caseService.setVariable(caseInstanceId, "variable_1", "aVariable");
    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    // HumanTask not enabled on variable create
    assertFalse(firstHumanTask.isEnabled());
    
    caseService.setVariable(caseInstanceId, "variable_1", "bVariable");
    firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(firstHumanTask.isEnabled());
  }  

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testVariableDelete.cmmn"})
  public void testVariableDelete() {
    
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    caseService.removeVariable(caseInstanceId, "variable_1");
    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    // removing unknown variable would not enable human task
    assertFalse(firstHumanTask.isEnabled());

    caseService.setVariable(caseInstanceId, "variable_1", "aVariable");
    firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    assertFalse(firstHumanTask.isEnabled());
    
    caseService.removeVariable(caseInstanceId, "variable_1");
    firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(firstHumanTask.isEnabled());
  }

  // different variable name and variable event test
  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testDifferentVariableName.cmmn"})
  public void testDifferentVariableName() {
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();
    
    CaseExecution firstHumanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    CaseExecution firstHumanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    
    assertFalse(firstHumanTask1.isEnabled());
    assertFalse(firstHumanTask2.isEnabled());

    caseService.setVariable(caseInstanceId, "variable_1", "aVariable");
    firstHumanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(firstHumanTask1.isEnabled());
    
    firstHumanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    // variable_2 is not set 
    assertFalse(firstHumanTask2.isEnabled());

    caseService.setVariable(caseInstanceId, "variable_2", "aVariable");
    firstHumanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(firstHumanTask2.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testDifferentVariableEvents.cmmn"})
  public void testDifferentVariableEventsButSameName() {
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();
    
    CaseExecution firstHumanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    CaseExecution firstHumanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    
    assertFalse(firstHumanTask1.isEnabled());
    assertFalse(firstHumanTask2.isEnabled());

    caseService.setVariable(caseInstanceId, "variable_1", "aVariable");
    firstHumanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(firstHumanTask1.isEnabled());
    
    firstHumanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    // variable_1 is not updated 
    assertFalse(firstHumanTask2.isEnabled());

    caseService.setVariable(caseInstanceId, "variable_1", "bVariable");
    firstHumanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(firstHumanTask2.isEnabled());
  }

  // Multiple variableOnParts test
  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testMoreVariableOnPart.cmmn"})
  public void testMultipleVariableOnParts() {
    
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    caseService.setVariable(caseInstanceId, "variable_1", "aVariable");
    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    // sentry would not be satisfied as the variable has to updated and deleted as well
    assertFalse(firstHumanTask.isEnabled());

    caseService.setVariable(caseInstanceId, "variable_1", "bVariable");
    firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    // sentry would not be satisfied as the variable has to deleted
    assertFalse(firstHumanTask.isEnabled());

    caseService.removeVariable(caseInstanceId, "variable_1");
    firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(firstHumanTask.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testMultipleSentryMultipleVariableOnPart.cmmn"})
  public void testMultipleSentryMultipleVariableOnParts() {
    
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    caseService.setVariable(caseInstanceId, "value", 99);
    CaseExecution firstHumanTask = queryCaseExecutionByActivityId("HumanTask_1");
    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("HumanTask_2");
    // Sentry1 would not be satisfied as the value has to be > 100
    // Sentry2 would not be satisfied as the humanTask 1 has to completed
    assertFalse(secondHumanTask.isEnabled());

    manualStart(firstHumanTask.getId());
    complete(firstHumanTask.getId());
    
    secondHumanTask = queryCaseExecutionByActivityId("HumanTask_2");
    // Sentry1 would not be satisfied as the value has to be > 100
    // But, Sentry 2 would be satisfied and enables HumanTask2
    assertTrue(secondHumanTask.isEnabled());

  }

  // IfPart, OnPart and VariableOnPart combination test
  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testOnPartIfPartAndVariableOnPart.cmmn"})
  public void testOnPartIfPartAndVariableOnPart() {
    
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();
    
    String firstHumanTaskId = queryCaseExecutionByActivityId("HumanTask_1").getId();

    complete(firstHumanTaskId);

    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("HumanTask_2");
    // Sentry would not be satisfied as variable_1 is not created and IfPart is not true
    assertFalse(secondHumanTask.isEnabled());

    caseService.setVariable(caseInstanceId, "value", 101);
    secondHumanTask = queryCaseExecutionByActivityId("HumanTask_2");
    // Sentry would not be satisfied as variable_1 is not created
    assertFalse(secondHumanTask.isEnabled());

    caseService.setVariable(caseInstanceId, "variable_1", "aVariable");
    secondHumanTask = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(secondHumanTask.isEnabled());
    
  }

  
  // Variable scope tests
  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testSimpleVariableScope.cmmn"})
  public void testVariableCreateScope() {
    
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();
    
    String firstHumanTaskId = queryCaseExecutionByActivityId("HumanTask_1").getId();
    
    manualStart(firstHumanTaskId);
    
    caseService.setVariableLocal(firstHumanTaskId, "variable_1", "aVariable");
    
    CaseExecution secondHumanTask = queryCaseExecutionByActivityId("HumanTask_2");
    // Sentry would not be triggered as the scope of the sentry and humanTask1 is different
    assertFalse(secondHumanTask.isEnabled());

    caseService.setVariableLocal(secondHumanTask.getId(), "variable_1", "aVariable");
    secondHumanTask = queryCaseExecutionByActivityId("HumanTask_2");
    // Still Sentry would not be triggered as the scope of sentry and the humantask2 is different
    assertFalse(secondHumanTask.isEnabled());

    caseService.setVariableLocal(caseInstanceId, "variable_1", "aVariable");
    secondHumanTask = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(secondHumanTask.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testStageScope.cmmn"})
  public void testStageScope() {
    
    caseService.createCaseInstanceByKey("Case_1");

    CaseExecution caseModelHumanTask = queryCaseExecutionByActivityId("CaseModel_HumanTask");
    assertFalse(caseModelHumanTask.isEnabled());

    String stageExecutionId = queryCaseExecutionByActivityId("Stage_1").getId();
    // set the variable in the scope of stage such that sentry in the scope of case model does not gets evaluated.
    caseService.setVariableLocal(stageExecutionId, "variable_1", "aVariable");
    
    CaseExecution stageHumanTask = queryCaseExecutionByActivityId("Stage_HumanTask");
    caseModelHumanTask = queryCaseExecutionByActivityId("CaseModel_HumanTask");
    assertFalse(caseModelHumanTask.isEnabled());
    assertTrue(stageHumanTask.isEnabled());

    caseService.removeVariable(stageExecutionId, "variable_1");
    // set the variable in the scope of case model that would trigger the sentry outside the scope of the stage
    caseService.setVariable(stageHumanTask.getId(), "variable_1", "aVariable");

    stageHumanTask = queryCaseExecutionByActivityId("Stage_HumanTask");
    caseModelHumanTask = queryCaseExecutionByActivityId("CaseModel_HumanTask");
    assertTrue(caseModelHumanTask.isEnabled());
    assertTrue(stageHumanTask.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testStagesScope.cmmn"})
  public void testStagesScope() {
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    caseService.setVariable(caseInstanceId, "variable_1", "aVariable");

    CaseExecution humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isEnabled());

    CaseExecution humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isEnabled());

    CaseExecution humanTask3 = queryCaseExecutionByActivityId("HumanTask_3");
    assertTrue(humanTask3.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testStagesScope.cmmn"})
  public void testStageLocalScope() {
    caseService.createCaseInstanceByKey("Case_1").getId();

    String stageExecution1_Id = queryCaseExecutionByActivityId("Stage_1").getId();
    
    String stageExecution2_Id = queryCaseExecutionByActivityId("Stage_2").getId();

    // variable set to stage 1 scope, so that sentries in stage 2 and in case model should not be triggered
    caseService.setVariableLocal(stageExecution1_Id, "variable_1", "aVariable");

    CaseExecution humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isEnabled());

    CaseExecution humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertFalse(humanTask2.isEnabled());

    CaseExecution humanTask3 = queryCaseExecutionByActivityId("HumanTask_3");
    assertFalse(humanTask3.isEnabled());

    // variable set to stage 2 scope, so that sentries in the scope of case model should not be triggered
    caseService.setVariableLocal(stageExecution2_Id, "variable_1", "aVariable");
    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isEnabled());

    humanTask3 = queryCaseExecutionByActivityId("HumanTask_3");
    assertFalse(humanTask3.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testMultipleOnPartsInStage.cmmn"})
  public void testMultipleOnPartsInStages() {
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();
    
    caseService.setVariable(caseInstanceId, "variable_1", 101);

    CaseExecution humanTask3 = queryCaseExecutionByActivityId("HumanTask_3");
    assertTrue(humanTask3.isEnabled());

    CaseExecution humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    // Not enabled as the sentry waits for human task 1 to complete
    assertFalse(humanTask2.isEnabled());

    CaseExecution humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    manualStart(humanTask1.getId());
    complete(humanTask1.getId());
    
    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.sentryEvaluationBeforeCreation.cmmn"})
  public void testShouldnotEvaluateSentryBeforeSentryCreation() {
    caseService.createCaseInstanceByKey("Case_1").getId();

    CaseExecution stageExecution = queryCaseExecutionByActivityId("Stage_1");
    assertTrue(stageExecution.isEnabled());

    CaseExecution humanTask = queryCaseExecutionByActivityId("HumanTask_1");
    assertNull(humanTask);

    // set the variable in the scope of stage - should not trigger sentry inside the stage as the sentry is not yet created.
    caseService.setVariableLocal(stageExecution.getId(), "variable_1", "aVariable");

    manualStart(stageExecution.getId());
    
    humanTask = queryCaseExecutionByActivityId("HumanTask_1");
    // variable event occurred before sentry creation
    assertTrue(humanTask.isAvailable());

    caseService.removeVariable(stageExecution.getId(), "variable_1");
    // Sentry is active and would enable human task 1
    caseService.setVariableLocal(stageExecution.getId(), "variable_1", "aVariable");
    humanTask = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask.isEnabled());
  }

  // Evaluation of not affected sentries test
  // i.e: Evaluation of a sentry's ifPart condition even if there are no evaluation of variableOnParts defined in the sentry
  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testSentryShouldNotBeEvaluatedAfterStageComplete.cmmn"})
  public void testEvaluationOfNotAffectedSentries() {
    caseService.createCaseInstanceByKey("Case_1").getId();

    String stageExecutionId = queryCaseExecutionByActivityId("Stage_1").getId();

    CaseExecution humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isAvailable());

    caseService.setVariableLocal(stageExecutionId, "value", 99);
    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    // if part is not satisfied
    assertFalse(humanTask2.isEnabled());

    caseService.setVariableLocal(stageExecutionId, "value", 101);
    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isEnabled());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testNotAffectedSentriesInMultipleStageScopes.cmmn"})
  public void testNotAffectedSentriesInMultipleStageScopes() {
    caseService.createCaseInstanceByKey("Case_1").getId();

    String stageExecution1_Id = queryCaseExecutionByActivityId("Stage_1").getId();

    caseService.setVariable(stageExecution1_Id, "value", 99);

    CaseExecution humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    // if part is not satisfied
    assertFalse(humanTask1.isEnabled());

    CaseExecution humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    // if part is not satisfied
    assertFalse(humanTask2.isEnabled());

    // Evaluates the sentry's IfPart alone
    caseService.setVariable(stageExecution1_Id, "value", 101);
    humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isEnabled());

    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isEnabled());
    
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testSameVariableNameInDifferentScopes.cmmn"})
  public void testSameVariableNameInDifferentScopes() {
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    String stageExecution1_Id = queryCaseExecutionByActivityId("Stage_1").getId();

    // inner stage
    String stageExecution2_Id = queryCaseExecutionByActivityId("Stage_2").getId();
    
    // set the same variable 'value' in the scope of case model
    caseService.setVariable(caseInstanceId, "value", 102);

    // set the variable 'value' in the scope of stage 1
    caseService.setVariableLocal(stageExecution1_Id, "value", 99);
    
    CaseExecution humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isAvailable());
    CaseExecution humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isAvailable());

    // update the variable 'value' in the case model scope
    caseService.setVariable(caseInstanceId, "value", 102);

    // then sentry of HumanTask 1 gets evaluated and sentry of HumanTask2 is not evaluated.
    humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isEnabled());
    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertFalse(humanTask2.isEnabled());

    // update the variable 'value' in the stage 2/stage 1 scope to evaluate the sentry inside stage 2
    caseService.setVariable(stageExecution2_Id, "value", 103);
    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isEnabled());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testSameVariableNameInDifferentScopes.cmmn"})
  public void testNestedScopes() {
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    String stageExecution1_Id = queryCaseExecutionByActivityId("Stage_1").getId();

    // set the variable 'value' in the scope of the case model
    caseService.setVariable(stageExecution1_Id, "value", 99);
    
    CaseExecution humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isAvailable());
    CaseExecution humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isAvailable());

    // update the variable 'value' in the case model scope
    caseService.setVariable(caseInstanceId, "value", 102);

    // then sentry of HumanTask 1 and HumanTask 2 gets evaluated.
    humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isEnabled());
    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isEnabled());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testSameVariableNameInDifferentScopes.cmmn"})
  public void testNestedScopesWithNullVariableValue() {
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    String stageExecution1_Id = queryCaseExecutionByActivityId("Stage_1").getId();

    // set the variable 'value' in the scope of the case model
    caseService.setVariable(caseInstanceId, "value", 99);

    // set the variable 'value' in the scope of the stage 1 with null value
    caseService.setVariableLocal(stageExecution1_Id, "value", null);
    
    CaseExecution humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isAvailable());
    CaseExecution humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isAvailable());

    // update the variable 'value' in the case model scope
    caseService.setVariable(caseInstanceId, "value", 102);

    // then sentry of HumanTask 1 and HumanTask 2 gets evaluated.
    humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isEnabled());
    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    // Sentry attached to HumanTask 2 is not evaluated because a variable 'value' exists in stage 2 even if the value is null 
    assertFalse(humanTask2.isEnabled());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testDifferentVariableNameInDifferentScope.cmmn"})
  public void testNestedScopesOfDifferentVariableNames() {
    String caseInstanceId = caseService.createCaseInstanceByKey("Case_1").getId();

    String stageExecution1_Id = queryCaseExecutionByActivityId("Stage_1").getId();

    // inner stage
    String stageExecution2_Id = queryCaseExecutionByActivityId("Stage_2").getId();
    
    // set the variable 'value_1' in the scope of the case model
    caseService.setVariable(caseInstanceId, "value_1", 99);
    // set the variable 'value_1' in the scope of the stage 1
    caseService.setVariableLocal(stageExecution1_Id, "value_1", 99);
    // set the variable 'value_2' in the scope of the stage 1
    caseService.setVariableLocal(stageExecution1_Id, "value_2", 99);

    CaseExecution humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isAvailable());
    CaseExecution humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isAvailable());

    // update the variable 'value_1' in the case model scope and stage scope
    caseService.setVariable(caseInstanceId, "value_1", 102);
    caseService.setVariableLocal(stageExecution1_Id, "value_1", 102);
    
    // then sentry of HumanTask 1 gets evaluated and sentry of HumanTask 2 does not gets evaluated.
    humanTask1 = queryCaseExecutionByActivityId("HumanTask_1");
    assertTrue(humanTask1.isEnabled());
    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertFalse(humanTask2.isEnabled());

    caseService.setVariable(stageExecution2_Id, "value_2", 102);
    humanTask2 = queryCaseExecutionByActivityId("HumanTask_2");
    assertTrue(humanTask2.isEnabled());

  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/variableonpart/SentryVariableOnPartEntryCriteriaTest.testSameVariableOnPartAsEntryAndExitCriteria.cmmn"})
  public void testSameVariableOnPartAsEntryAndExitCriteria() {
    caseService.createCaseInstanceByKey("Case_1").getId();

    CaseExecution stageExecution = queryCaseExecutionByActivityId("Stage_1");

    caseService.setVariable(stageExecution.getId(), "value", 99);

    CaseExecution humanTask = queryCaseExecutionByActivityId("HumanTask_1");
    // exit criteria not satisfied due to the variable 'value' must be greater than 100
    assertTrue(humanTask.isEnabled());
    manualStart(humanTask.getId());
    
    caseService.setVariable(stageExecution.getId(), "value", 101);
    stageExecution = queryCaseExecutionByActivityId("Stage_1");
    assertNull(stageExecution);
  }
}
