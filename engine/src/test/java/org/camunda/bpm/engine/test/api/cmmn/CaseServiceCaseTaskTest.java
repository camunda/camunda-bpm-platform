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
package org.camunda.bpm.engine.test.api.cmmn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class CaseServiceCaseTaskTest extends PluggableProcessEngineTestCase {

  protected final String DEFINITION_KEY = "oneCaseTaskCase";
  protected final String DEFINITION_KEY_2 = "oneTaskCase";
  protected final String CASE_TASK_KEY = "PI_CaseTask_1";


  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  public void testManualStart() {
    // given
    createCaseInstance(DEFINITION_KEY).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    CaseInstance subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNull(subCaseInstance);

    // when
    caseService
        .withCaseExecution(caseTaskId)
        .manualStart();

    // then
    subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isActive());

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertTrue(caseTask.isActive());
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  public void testManualStartWithVariable() {
    // given
    String superCaseInstanceId = createCaseInstance(DEFINITION_KEY).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    CaseInstance subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNull(subCaseInstance);

    // when
    caseService
        .withCaseExecution(caseTaskId)
        .setVariable("aVariableName", "abc")
        .setVariable("anotherVariableName", 999)
        .manualStart();

    // then
    subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isActive());

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertTrue(caseTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    verifyVariables(superCaseInstanceId, result);

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  public void testManualStartWithVariables() {
    // given
    String superCaseInstanceId = createCaseInstance(DEFINITION_KEY).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    CaseInstance subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNull(subCaseInstance);

    // variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    caseService
        .withCaseExecution(caseTaskId)
        .setVariables(variables)
        .manualStart();

    // then
    subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isActive());

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertTrue(caseTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    verifyVariables(superCaseInstanceId, result);

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testStart() {
    // given
    createCaseInstance(DEFINITION_KEY).getId();
    queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    CaseInstance subCaseInstance;

    // then
    subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isActive());

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertTrue(caseTask.isActive());
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testStartWithVariable() {
    // given
    String superCaseInstanceId = createCaseInstance(DEFINITION_KEY,
        Variables.createVariables()
            .putValue("aVariableName", "abc")
            .putValue("anotherVariableName", 999)).getId();

    CaseInstance subCaseInstance;

    // then
    subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isActive());

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertTrue(caseTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    verifyVariables(superCaseInstanceId, result);

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testStartWithVariables() {
    // given
    // variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    String superCaseInstanceId = createCaseInstance(DEFINITION_KEY, variables).getId();

    CaseInstance subCaseInstance;

    // then
    subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isActive());

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertTrue(caseTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    verifyVariables(superCaseInstanceId, result);

  }

  protected void verifyVariables(String superCaseInstanceId, List<VariableInstance> result) {
    for (VariableInstance variable : result) {

      assertEquals(superCaseInstanceId, variable.getCaseExecutionId());
      assertEquals(superCaseInstanceId, variable.getCaseInstanceId());

      if (variable.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());
      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testManualStartWithLocalVariable() {
    // given
    String superCaseInstanceId = createCaseInstance(DEFINITION_KEY).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    CaseInstance subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNull(subCaseInstance);

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .manualStart();

    // then
    subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isActive());

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertTrue(caseTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseTaskId, variable.getCaseExecutionId());
      assertEquals(superCaseInstanceId, variable.getCaseInstanceId());

      if (variable.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());
      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testManualStartWithLocalVariables() {
    // given
    String superCaseInstanceId = createCaseInstance(DEFINITION_KEY).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    CaseInstance subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNull(subCaseInstance);

    // variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseTaskId)
      .setVariablesLocal(variables)
      .manualStart();

    // then
    subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isActive());

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertTrue(caseTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseTaskId, variable.getCaseExecutionId());
      assertEquals(superCaseInstanceId, variable.getCaseInstanceId());

      if (variable.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());
      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn"
      })
  public void testReenableAnEnabledCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    CaseInstance subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNull(subCaseInstance);

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .reenable();
      fail("It should not be possible to re-enable an enabled case task.");
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskAndOneHumanTaskCaseWithManualActivation.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testReenableADisabledCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    CaseInstance subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNull(subCaseInstance);

    caseService
      .withCaseExecution(caseTaskId)
      .disable();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .reenable();

    // then
    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertTrue(caseTask.isEnabled());
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testReenableAnActiveCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .reenable();
      fail("It should not be possible to re-enable an active case task.");
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskAndOneHumanTaskCaseWithManualActivation.cmmn"})
  public void testDisableAnEnabledCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    CaseInstance subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertNull(subCaseInstance);

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .disable();

    // then
    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertTrue(caseTask.isDisabled());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskAndOneHumanTaskCaseWithManualActivation.cmmn"})
  public void testDisableADisabledCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .disable();
      fail("It should not be possible to disable a already disabled case task.");
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testDisableAnActiveCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    // when
    try {
      caseService
        .withCaseExecution(caseTaskId)
        .disable();
      fail("It should not be possible to disable an active case task.");
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskAndOneHumanTaskCaseWithManualActivation.cmmn"})
  public void testManualStartOfADisabledCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .manualStart();
      fail("It should not be possible to start a disabled case task manually.");
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testManualStartOfAnActiveCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .manualStart();
      fail("It should not be possible to start an already active case task manually.");
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testComplete() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .complete();
      fail("It should not be possible to complete a case task, while the process instance is still running.");
    } catch (NotAllowedException e) {}

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testCloseCaseInstanceShouldCompleteCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    String humanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    CaseInstance subCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY_2);
    assertTrue(subCaseInstance.isCompleted());

    // when
    caseService
      .withCaseExecution(subCaseInstance.getId())
      .close();

    // then
    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertNull(caseTask);

    CaseInstance superCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY);
    assertNotNull(superCaseInstance);
    assertTrue(superCaseInstance.isCompleted());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn"})
  public void testDisableShouldCompleteCaseInstance() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    // when

    caseService
      .withCaseExecution(caseTaskId)
      .disable();

    // then
    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK_KEY);

    // the case instance has been completed
    CaseInstance superCaseInstance = queryCaseInstanceByKey(DEFINITION_KEY);
    assertNotNull(superCaseInstance);
    assertTrue(superCaseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn"})
  public void testCompleteAnEnabledCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .complete();
      fail("Should not be able to complete an enabled case task.");
    } catch (NotAllowedException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskAndOneHumanTaskCaseWithManualActivation.cmmn"})
  public void testCompleteADisabledCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .complete();
      fail("Should not be able to complete a disabled case task.");
    } catch (NotAllowedException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn"})
  public void testClose() {
    // given
    createCaseInstance(DEFINITION_KEY);
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK_KEY).getId();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .close();
      fail("It should not be possible to close a case task.");
    } catch (NotAllowedException e) {

    }

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testTerminate() {
    // given
    createCaseInstance(DEFINITION_KEY);
    CaseExecution caseTaskExecution = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    // when
    caseService
      .withCaseExecution(caseTaskExecution.getId())
      .terminate();

    caseTaskExecution = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertNull(caseTaskExecution);
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testTerminateNonFluent() {
    // given
    createCaseInstance(DEFINITION_KEY);
    CaseExecution caseTaskExecution = queryCaseExecutionByActivityId(CASE_TASK_KEY);

    // when
    caseService
      .terminateCaseExecution(caseTaskExecution.getId());

    caseTaskExecution = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    assertNull(caseTaskExecution);
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  public void testTerminateNonActiveCaseTask() {
    // given
    createCaseInstance(DEFINITION_KEY);
    CaseExecution caseTaskExecution = queryCaseExecutionByActivityId(CASE_TASK_KEY);
    
    try {
      // when
      caseService 
        .terminateCaseExecution(caseTaskExecution.getId());
      fail("It should not be possible to terminate a case task.");
    } catch (NotAllowedException e) {
      boolean result = e.getMessage().contains("The case execution must be in state 'active' to terminate");
      assertTrue(result);   
    }
  }
  
  protected CaseInstance createCaseInstance(String caseDefinitionKey) {
    return caseService
        .withCaseDefinitionByKey(caseDefinitionKey)
        .create();
  }

  protected CaseInstance createCaseInstance(String caseDefinitionKey, Map<String, Object> variables) {
    return caseService
        .withCaseDefinitionByKey(caseDefinitionKey)
        .setVariables(variables)
        .create();
  }

  protected CaseExecution queryCaseExecutionByActivityId(String activityId) {
    return caseService
        .createCaseExecutionQuery()
        .activityId(activityId)
        .singleResult();
  }

  protected CaseInstance queryCaseInstanceByKey(String caseDefinitionKey) {
    return caseService
        .createCaseInstanceQuery()
        .caseDefinitionKey(caseDefinitionKey)
        .singleResult();
  }

  protected Task queryTask() {
    return taskService
        .createTaskQuery()
        .singleResult();
  }

}
