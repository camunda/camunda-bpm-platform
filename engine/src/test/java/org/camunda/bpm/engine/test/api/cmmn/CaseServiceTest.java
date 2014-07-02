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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionCommandBuilder;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class CaseServiceTest extends PluggableProcessEngineTestCase {

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCreateCaseInstanceByKey() {
    // given a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // when
    CaseInstance caseInstance = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .create();

    // then
    assertNotNull(caseInstance);

    // check properties
    assertNull(caseInstance.getBusinessKey());
    assertEquals(caseDefinitionId, caseInstance.getCaseDefinitionId());
    assertEquals(caseInstance.getId(), caseInstance.getCaseInstanceId());
    assertTrue(caseInstance.isActive());
    assertFalse(caseInstance.isEnabled());

    // get persisted case instance
    CaseInstance instance = caseService
      .createCaseInstanceQuery()
      .singleResult();

    // should have the same properties
    assertEquals(caseInstance.getId(), instance.getId());
    assertEquals(caseInstance.getBusinessKey(), instance.getBusinessKey());
    assertEquals(caseInstance.getCaseDefinitionId(), instance.getCaseDefinitionId());
    assertEquals(caseInstance.getCaseInstanceId(), instance.getCaseInstanceId());
    assertEquals(caseInstance.isActive(), instance.isActive());
    assertEquals(caseInstance.isEnabled(), instance.isEnabled());
  }

  public void testCreateCaseInstanceByInvalidKey() {
    try {
      caseService
          .withCaseDefinitionByKey("invalid")
          .create();
      fail();
    } catch (ProcessEngineException e) { }

    try {
      caseService
          .withCaseDefinitionByKey(null)
          .create();
      fail();
    } catch (ProcessEngineException e) { }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCreateCaseInstanceById() {
    // given a deployed case definition
    String caseDefinitionId = repositoryService
      .createCaseDefinitionQuery()
      .singleResult()
      .getId();

    // when
    CaseInstance caseInstance = caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    // then
    assertNotNull(caseInstance);

    // check properties
    assertNull(caseInstance.getBusinessKey());
    assertEquals(caseDefinitionId, caseInstance.getCaseDefinitionId());
    assertEquals(caseInstance.getId(), caseInstance.getCaseInstanceId());
    assertTrue(caseInstance.isActive());
    assertFalse(caseInstance.isEnabled());

    // get persistent case instance
    CaseInstance instance = caseService
      .createCaseInstanceQuery()
      .singleResult();

    // should have the same properties
    assertEquals(caseInstance.getId(), instance.getId());
    assertEquals(caseInstance.getBusinessKey(), instance.getBusinessKey());
    assertEquals(caseInstance.getCaseDefinitionId(), instance.getCaseDefinitionId());
    assertEquals(caseInstance.getCaseInstanceId(), instance.getCaseInstanceId());
    assertEquals(caseInstance.isActive(), instance.isActive());
    assertEquals(caseInstance.isEnabled(), instance.isEnabled());

  }

  public void testCreateCaseInstanceByInvalidId() {
    try {
      caseService
          .withCaseDefinition("invalid")
          .create();
      fail();
    } catch (ProcessEngineException e) { }

    try {
      caseService
          .withCaseDefinition(null)
          .create();
      fail();
    } catch (ProcessEngineException e) { }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCreateCaseInstanceByKeyWithBusinessKey() {
    // given a deployed case definition
    String caseDefinitionId = repositoryService
      .createCaseDefinitionQuery()
      .singleResult()
      .getId();

    // when
    CaseInstance caseInstance = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .businessKey("aBusinessKey")
        .create();

    // then
    assertNotNull(caseInstance);

    // check properties
    assertEquals("aBusinessKey", caseInstance.getBusinessKey());
    assertEquals(caseDefinitionId, caseInstance.getCaseDefinitionId());
    assertEquals(caseInstance.getId(), caseInstance.getCaseInstanceId());
    assertTrue(caseInstance.isActive());
    assertFalse(caseInstance.isEnabled());

    // get persistend case instance
    CaseInstance instance = caseService
      .createCaseInstanceQuery()
      .singleResult();

    // should have the same properties
    assertEquals(caseInstance.getId(), instance.getId());
    assertEquals(caseInstance.getBusinessKey(), instance.getBusinessKey());
    assertEquals(caseInstance.getCaseDefinitionId(), instance.getCaseDefinitionId());
    assertEquals(caseInstance.getCaseInstanceId(), instance.getCaseInstanceId());
    assertEquals(caseInstance.isActive(), instance.isActive());
    assertEquals(caseInstance.isEnabled(), instance.isEnabled());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCreateCaseInstanceByIdWithBusinessKey() {
    // given a deployed case definition
    String caseDefinitionId = repositoryService
      .createCaseDefinitionQuery()
      .singleResult()
      .getId();

    // when
    CaseInstance caseInstance = caseService
        .withCaseDefinition(caseDefinitionId)
        .businessKey("aBusinessKey")
        .create();

    // then
    assertNotNull(caseInstance);

    // check properties
    assertEquals("aBusinessKey", caseInstance.getBusinessKey());
    assertEquals(caseDefinitionId, caseInstance.getCaseDefinitionId());
    assertEquals(caseInstance.getId(), caseInstance.getCaseInstanceId());
    assertTrue(caseInstance.isActive());
    assertFalse(caseInstance.isEnabled());

    // get persistend case instance
    CaseInstance instance = caseService
      .createCaseInstanceQuery()
      .singleResult();

    // should have the same properties
    assertEquals(caseInstance.getId(), instance.getId());
    assertEquals(caseInstance.getBusinessKey(), instance.getBusinessKey());
    assertEquals(caseInstance.getCaseDefinitionId(), instance.getCaseDefinitionId());
    assertEquals(caseInstance.getCaseInstanceId(), instance.getCaseInstanceId());
    assertEquals(caseInstance.isActive(), instance.isActive());
    assertEquals(caseInstance.isEnabled(), instance.isEnabled());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCreateCaseInstanceByKeyWithVariable() {
    // given a deployed case definition

    // when
    CaseInstance caseInstance = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .setVariable("aVariableName", "aVariableValue")
        .setVariable("anotherVariableName", 999)
        .create();

    // then
    assertNotNull(caseInstance);

    // there should exist two variables
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    List<VariableInstance> result = query
      .caseInstanceIdIn(caseInstance.getId())
      .orderByVariableName()
      .asc()
      .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variableInstance : result) {
      if (variableInstance.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variableInstance.getName());
        assertEquals("aVariableValue", variableInstance.getValue());
      } else if (variableInstance.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variableInstance.getName());
        assertEquals(999, variableInstance.getValue());
      } else {
        fail("Unexpected variable: " + variableInstance.getName());
      }

    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCreateCaseInstanceByKeyWithVariables() {
    // given a deployed case definition
    Map<String, Object> variables = new HashMap<String, Object>();

    variables.put("aVariableName", "aVariableValue");
    variables.put("anotherVariableName", 999);

    // when
    CaseInstance caseInstance = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .setVariables(variables)
        .create();

    // then
    assertNotNull(caseInstance);

    // there should exist two variables
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    List<VariableInstance> result = query
      .caseInstanceIdIn(caseInstance.getId())
      .orderByVariableName()
      .asc()
      .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variableInstance : result) {
      if (variableInstance.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variableInstance.getName());
        assertEquals("aVariableValue", variableInstance.getValue());
      } else if (variableInstance.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variableInstance.getName());
        assertEquals(999, variableInstance.getValue());
      } else {
        fail("Unexpected variable: " + variableInstance.getName());
      }

    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCreateCaseInstanceByIdWithVariable() {
    // given a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // when
    CaseInstance caseInstance = caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("aVariableName", "aVariableValue")
        .setVariable("anotherVariableName", 999)
        .create();

    // then
    assertNotNull(caseInstance);

    // there should exist two variables
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    List<VariableInstance> result = query
      .caseInstanceIdIn(caseInstance.getId())
      .orderByVariableName()
      .asc()
      .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variableInstance : result) {
      if (variableInstance.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variableInstance.getName());
        assertEquals("aVariableValue", variableInstance.getValue());
      } else if (variableInstance.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variableInstance.getName());
        assertEquals(999, variableInstance.getValue());
      } else {
        fail("Unexpected variable: " + variableInstance.getName());
      }

    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCreateCaseInstanceByIdWithVariables() {
    // given a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    Map<String, Object> variables = new HashMap<String, Object>();

    variables.put("aVariableName", "aVariableValue");
    variables.put("anotherVariableName", 999);

    // when
    CaseInstance caseInstance = caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariables(variables)
        .create();

    // then
    assertNotNull(caseInstance);

    // there should exist two variables
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    List<VariableInstance> result = query
      .caseInstanceIdIn(caseInstance.getId())
      .orderByVariableName()
      .asc()
      .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variableInstance : result) {
      if (variableInstance.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variableInstance.getName());
        assertEquals("aVariableValue", variableInstance.getValue());
      } else if (variableInstance.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variableInstance.getName());
        assertEquals(999, variableInstance.getValue());
      } else {
        fail("Unexpected variable: " + variableInstance.getName());
      }

    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testManualStartCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    // when
    try {
      caseService
        .withCaseExecution(caseInstanceId)
        .manualStart();
      fail("It should not be possible to start a case instance manually.");
    } catch (Exception e) {
    }


  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testDisableCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    // when
    try {
      caseService
        .withCaseExecution(caseInstanceId)
        .disable();
      fail("It should not be possible to disable a case instance.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testReenableCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    // when
    try {
      caseService
        .withCaseExecution(caseInstanceId)
        .reenable();
      fail("It should not be possible to re-enable a case instance.");
    } catch (Exception e) {
    }
  }

  public void testCreateCaseInstanceQuery() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    assertNotNull(query);
  }

  public void testCreateCaseExecutionQuery() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    assertNotNull(query);
  }

  public void testWithCaseExecution() {
    CaseExecutionCommandBuilder builder = caseService.withCaseExecution("aCaseExecutionId");

    assertNotNull(builder);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testHumanTaskManualStart() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
      .withCaseDefinition(caseDefinitionId)
      .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
      .activityId("PI_HumanTask_1")
      .singleResult()
      .getId();

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // there exists a task
    Task task = taskService
      .createTaskQuery()
      .caseExecutionId(caseExecutionId)
      .singleResult();

    assertNotNull(task);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testHumanTaskManualStartWithVariable() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariable("aVariableName", "abc")
      .setVariable("anotherVariableName", 999)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // there exists a task
    Task task = taskService
        .createTaskQuery()
        .caseExecutionId(caseExecutionId)
        .singleResult();

    assertNotNull(task);

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseInstanceId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testHumanTaskManualStartWithVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariables(variables)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // there exists a task
    Task task = taskService
        .createTaskQuery()
        .caseExecutionId(caseExecutionId)
        .singleResult();

    assertNotNull(task);

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseInstanceId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testHumanTaskManualStartWithLocalVariable() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // there exists a task
    Task task = taskService
        .createTaskQuery()
        .caseExecutionId(caseExecutionId)
        .singleResult();

    assertNotNull(task);

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testHumanTaskManualStartWithLocalVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariablesLocal(variables)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // there exists a task
    Task task = taskService
        .createTaskQuery()
        .caseExecutionId(caseExecutionId)
        .singleResult();

    assertNotNull(task);

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testStageManualStartCaseExecution() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
      .withCaseDefinition(caseDefinitionId)
      .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
      .activityId("PI_Stage_1")
      .singleResult()
      .getId();

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // there exists two new case execution:

    // (1) one case case execution representing "PI_HumanTask_1"
    CaseExecution firstHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(firstHumanTask);
    assertTrue(firstHumanTask.isEnabled());
    assertFalse(firstHumanTask.isActive());

    // (2) one case case execution representing "PI_HumanTask_2"
    CaseExecution secondHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_2")
        .singleResult();

    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
    assertFalse(secondHumanTask.isActive());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testStageManualStartCaseExecutionWithVariable() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariable("aVariableName", "abc")
      .setVariable("anotherVariableName", 999)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // (1) one case case execution representing "PI_HumanTask_1"
    CaseExecution firstHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(firstHumanTask);
    assertTrue(firstHumanTask.isEnabled());
    assertFalse(firstHumanTask.isActive());

    // (2) one case case execution representing "PI_HumanTask_2"
    CaseExecution secondHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_2")
        .singleResult();

    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
    assertFalse(secondHumanTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseInstanceId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testStageManualStartCaseExecutionWithVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariables(variables)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // (1) one case case execution representing "PI_HumanTask_1"
    CaseExecution firstHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(firstHumanTask);
    assertTrue(firstHumanTask.isEnabled());
    assertFalse(firstHumanTask.isActive());

    // (2) one case case execution representing "PI_HumanTask_2"
    CaseExecution secondHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_2")
        .singleResult();

    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
    assertFalse(secondHumanTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseInstanceId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testStageManualStartCaseExecutionWithLocalVariable() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // (1) one case case execution representing "PI_HumanTask_1"
    CaseExecution firstHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(firstHumanTask);
    assertTrue(firstHumanTask.isEnabled());
    assertFalse(firstHumanTask.isActive());

    // (2) one case case execution representing "PI_HumanTask_2"
    CaseExecution secondHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_2")
        .singleResult();

    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
    assertFalse(secondHumanTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testStageManualStartCaseExecutionWithLocalVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariablesLocal(variables)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // (1) one case case execution representing "PI_HumanTask_1"
    CaseExecution firstHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(firstHumanTask);
    assertTrue(firstHumanTask.isEnabled());
    assertFalse(firstHumanTask.isActive());

    // (2) one case case execution representing "PI_HumanTask_2"
    CaseExecution secondHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_2")
        .singleResult();

    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
    assertFalse(secondHumanTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testReenableAnEnabledHumanTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance

    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .reenable();
      fail("It should not be possible to re-enable an enabled human task.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"})
  public void testReenableAnDisabledHumanTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    String caseExecutionId = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // the human task is disabled
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .reenable();

    // then
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    // the human task is disabled
    assertFalse(caseExecution.isDisabled());
    assertFalse(caseExecution.isActive());
    assertTrue(caseExecution.isEnabled());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testReenableAnActiveHumanTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when
    try {
      caseService
        .withCaseExecution(caseExecutionId)
        .reenable();
      fail("It should not be possible to re-enable an active human task.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"})
  public void testDisableAnEnabledHumanTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance and the containing
    // human task is enabled
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    String caseExecutionId = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    // then
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    // the human task is disabled
    assertTrue(caseExecution.isDisabled());
    assertFalse(caseExecution.isActive());
    assertFalse(caseExecution.isEnabled());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"})
  public void testDisableADisabledHumanTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    String caseExecutionId = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // the human task is disabled
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .disable();
      fail("It should not be possible to disable a already disabled human task.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testDisableAnActiveHumanTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when
    try {
      caseService
        .withCaseExecution(caseExecutionId)
        .disable();
      fail("It should not be possible to disable an active human task.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"})
  public void testManualStartOfADisabledHumanTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .manualStart();
      fail("It should not be possible to start a disabled human task manually.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testManualStartOfAnActiveHumanTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .manualStart();
      fail("It should not be possible to start an already active human task manually.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testReenableAnEnabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .reenable();
      fail("It should not be possible to re-enable an enabled stage.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testReenableAnDisabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // the human task is disabled
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .reenable();

    // then
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    // the human task is disabled
    assertFalse(caseExecution.isDisabled());
    assertFalse(caseExecution.isActive());
    assertTrue(caseExecution.isEnabled());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testReenableAnActiveStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when
    try {
      caseService
        .withCaseExecution(caseExecutionId)
        .reenable();
      fail("It should not be possible to re-enable an active stage.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testDisableAnEnabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance and the containing
    // human task is enabled
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    // then
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    // the human task is disabled
    assertTrue(caseExecution.isDisabled());
    assertFalse(caseExecution.isActive());
    assertFalse(caseExecution.isEnabled());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testDisableADisabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // the human task is disabled
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .disable();
      fail("It should not be possible to disable a already disabled stage.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testDisableAnActiveStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when
    try {
      caseService
        .withCaseExecution(caseExecutionId)
        .disable();
      fail("It should not be possible to disable an active stage.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testManualStartOfADisabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .manualStart();
      fail("It should not be possible to start a disabled stage manually.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testManualStartOfAnActiveStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .manualStart();
      fail("It should not be possible to start an already active stage manually.");
    } catch (Exception e) {
    }
  }

  public void testManualStartInvalidCaseExecution() {
    try {
      caseService
          .withCaseExecution("invalid")
          .manualStart();
      fail();
    } catch (ProcessEngineException e) { }

    try {
      caseService
        .withCaseExecution(null)
        .manualStart();
      fail();
    } catch (ProcessEngineException e) { }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteSetVariable() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariable("aVariableName", "abc")
      .setVariable("anotherVariableName", 999)
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertTrue(result.isEmpty());

    // query by case instance id
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseInstanceId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteSetVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariables(variables)
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertTrue(result.isEmpty());

    // query by caseInstanceId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseInstanceId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteSetVariableAndVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariables(variables)
      .setVariable("aThirdVariable", 123)
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertTrue(result.isEmpty());

    // query by caseInstanceId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseInstanceId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

      if (variable.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());
      } else if (variable.getName().equals("aThirdVariable")) {
        assertEquals("aThirdVariable", variable.getName());
        assertEquals(123, variable.getValue());
      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteSetVariableLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

    // query by case instance id
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteSetVariablesLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariablesLocal(variables)
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

    // query by case instance id
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteSetVariableLocalAndVariablesLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariablesLocal(variables)
      .setVariableLocal("aThirdVariable", 123)
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

      if (variable.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());
      } else if (variable.getName().equals("aThirdVariable")) {
        assertEquals("aThirdVariable", variable.getName());
        assertEquals(123, variable.getValue());
      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }

    // query by caseInstanceId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

      if (variable.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());
      } else if (variable.getName().equals("aThirdVariable")) {
        assertEquals("aThirdVariable", variable.getName());
        assertEquals(123, variable.getValue());
      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteSetVariableAndVariablesLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariables(variables)
      .setVariableLocal("aThirdVariable", 123)
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    VariableInstance aThirdVariable = result.get(0);

    assertNotNull(aThirdVariable);
    assertEquals("aThirdVariable", aThirdVariable.getName());
    assertEquals(123, aThirdVariable.getValue());

    // query by caseInstanceId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    for (VariableInstance variable : result) {


      if (variable.getName().equals("aVariableName")) {
        assertEquals(caseInstanceId, variable.getCaseExecutionId());
        assertEquals(caseInstanceId, variable.getCaseInstanceId());

        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());

      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals(caseInstanceId, variable.getCaseExecutionId());
        assertEquals(caseInstanceId, variable.getCaseInstanceId());

        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());

      } else if (variable.getName().equals("aThirdVariable")) {
        assertEquals(caseExecutionId, variable.getCaseExecutionId());
        assertEquals(caseInstanceId, variable.getCaseInstanceId());

        assertEquals("aThirdVariable", variable.getName());
        assertEquals(123, variable.getValue());

      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteRemoveVariable() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("aVariableName", "abc")
        .setVariable("anotherVariableName", 999)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .removeVariable("aVariableName")
      .removeVariable("anotherVariableName")
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertTrue(result.isEmpty());

    // query by case instance id
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertTrue(result.isEmpty());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteRemoveVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("aVariableName", "abc")
        .setVariable("anotherVariableName", 999)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    List<String> variableNames = new ArrayList<String>();
    variableNames.add("aVariableName");
    variableNames.add("anotherVariableName");

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .removeVariables(variableNames)
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertTrue(result.isEmpty());

    // query by caseInstanceId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertTrue(result.isEmpty());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteRemoveVariableAndVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("aVariableName", "abc")
        .setVariable("anotherVariableName", 999)
        .setVariable("aThirdVariable", 123)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    List<String> variableNames = new ArrayList<String>();
    variableNames.add("aVariableName");
    variableNames.add("anotherVariableName");

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .removeVariables(variableNames)
      .removeVariable("aThirdVariable")
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertTrue(result.isEmpty());

    // query by caseInstanceId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertTrue(result.isEmpty());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteRemoveVariableLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .execute();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .removeVariableLocal("aVariableName")
      .removeVariableLocal("anotherVariableName")
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertTrue(result.isEmpty());

    // query by case instance id
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertTrue(result.isEmpty());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteRemoveVariablesLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .execute();

    List<String> variableNames = new ArrayList<String>();
    variableNames.add("aVariableName");
    variableNames.add("anotherVariableName");

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .removeVariablesLocal(variableNames)
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertTrue(result.isEmpty());

    // query by caseInstanceId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertTrue(result.isEmpty());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteRemoveVariableLocalAndVariablesLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .setVariableLocal("aThirdVariable", 123)
      .execute();

    List<String> variableNames = new ArrayList<String>();
    variableNames.add("aVariableName");
    variableNames.add("anotherVariableName");

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .removeVariablesLocal(variableNames)
      .removeVariableLocal("aThirdVariable")
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertTrue(result.isEmpty());

    // query by caseInstanceId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertTrue(result.isEmpty());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteRemoveVariableAndVariablesLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .setVariable("aVariableName", "abc")
      .setVariable("anotherVariableName", 999)
      .setVariableLocal("aThirdVariable", 123)
      .execute();

    List<String> variableNames = new ArrayList<String>();
    variableNames.add("aVariableName");
    variableNames.add("anotherVariableName");

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .removeVariables(variableNames)
      .removeVariableLocal("aThirdVariable")
      .execute();

    // then

    // query by caseExecutionId
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertTrue(result.isEmpty());

    // query by caseInstanceId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertTrue(result.isEmpty());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteRemoveAndSetSameVariable() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
     caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("aVariableName", "abc")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .removeVariable("aVariableName")
        .setVariable("aVariableName", "xyz")
        .execute();
    } catch (ProcessEngineException e) {
      // then
      assertTextPresent("Cannot set and remove a variable with the same variable name: 'aVariableName' within a command.", e.getMessage());
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testExecuteRemoveAndSetSameLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
     caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("aVariableName", "abc")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .setVariableLocal("aVariableName", "xyz")
        .removeVariableLocal("aVariableName")
        .execute();
    } catch (ProcessEngineException e) {
      // then
      assertTextPresent("Cannot set and remove a variable with the same variable name: 'aVariableName' within a command.", e.getMessage());
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
     caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("aVariableName", "abc")
        .setVariable("anotherVariableName", 999)
        .create()
        .getId();

     String caseExecutionId = caseService
         .createCaseExecutionQuery()
         .activityId("PI_HumanTask_1")
         .singleResult()
         .getId();

     // when
     Map<String, Object> variables = caseService.getVariables(caseExecutionId);

     // then
     assertNotNull(variables);
     assertFalse(variables.isEmpty());
     assertEquals(2, variables.size());

     assertEquals("abc", variables.get("aVariableName"));
     assertEquals(999, variables.get("anotherVariableName"));
  }

  public void testGetVariablesInvalidCaseExecutionId() {

    try {
      caseService.getVariables("invalid");
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }

    try {
      caseService.getVariables(null);
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariablesWithVariableNames() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
     caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("aVariableName", "abc")
        .setVariable("anotherVariableName", 999)
        .setVariable("thirVariable", "xyz")
        .create()
        .getId();

     String caseExecutionId = caseService
         .createCaseExecutionQuery()
         .activityId("PI_HumanTask_1")
         .singleResult()
         .getId();

     List<String> names = new ArrayList<String>();
     names.add("aVariableName");
     names.add("anotherVariableName");

     // when
     Map<String, Object> variables = caseService.getVariables(caseExecutionId, names);

     // then
     assertNotNull(variables);
     assertFalse(variables.isEmpty());
     assertEquals(2, variables.size());

     assertEquals("abc", variables.get("aVariableName"));
     assertEquals(999, variables.get("anotherVariableName"));
  }

  public void testGetVariablesWithVariablesNamesInvalidCaseExecutionId() {

    try {
      caseService.getVariables("invalid", null);
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }

    try {
      caseService.getVariables(null, null);
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariablesLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
     caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

     String caseExecutionId = caseService
         .createCaseExecutionQuery()
         .activityId("PI_HumanTask_1")
         .singleResult()
         .getId();

     caseService
       .withCaseExecution(caseExecutionId)
       .setVariableLocal("aVariableName", "abc")
       .setVariableLocal("anotherVariableName", 999)
       .execute();

     // when
     Map<String, Object> variables = caseService.getVariablesLocal(caseExecutionId);

     // then
     assertNotNull(variables);
     assertFalse(variables.isEmpty());
     assertEquals(2, variables.size());

     assertEquals("abc", variables.get("aVariableName"));
     assertEquals(999, variables.get("anotherVariableName"));
  }

  public void testGetVariablesLocalInvalidCaseExecutionId() {

    try {
      caseService.getVariablesLocal("invalid");
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }

    try {
      caseService.getVariablesLocal(null);
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariablesLocalWithVariableNames() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .execute();

     List<String> names = new ArrayList<String>();
     names.add("aVariableName");
     names.add("anotherVariableName");

     // when
     Map<String, Object> variables = caseService.getVariablesLocal(caseExecutionId, names);

     // then
     assertNotNull(variables);
     assertFalse(variables.isEmpty());
     assertEquals(2, variables.size());

     assertEquals("abc", variables.get("aVariableName"));
     assertEquals(999, variables.get("anotherVariableName"));
  }

  public void testGetVariablesLocalWithVariablesNamesInvalidCaseExecutionId() {

    try {
      caseService.getVariablesLocal("invalid", null);
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }

    try {
      caseService.getVariablesLocal(null, null);
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariable() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
     caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("aVariableName", "abc")
        .setVariable("anotherVariableName", 999)
        .setVariable("thirVariable", "xyz")
        .create()
        .getId();

     String caseExecutionId = caseService
         .createCaseExecutionQuery()
         .activityId("PI_HumanTask_1")
         .singleResult()
         .getId();

     // when
     Object value = caseService.getVariable(caseExecutionId, "aVariableName");

     // then
     assertNotNull(value);
     assertEquals("abc", value);
  }

  public void testGetVariableInvalidCaseExecutionId() {
    try {
      caseService.getVariable("invalid", "aVariableName");
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }

    try {
      caseService.getVariable(null, "aVariableName");
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariableLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
     caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

     String caseExecutionId = caseService
         .createCaseExecutionQuery()
         .activityId("PI_HumanTask_1")
         .singleResult()
         .getId();

     caseService
       .withCaseExecution(caseExecutionId)
       .setVariableLocal("aVariableName", "abc")
       .setVariableLocal("anotherVariableName", 999)
       .execute();

     // when
     Object value = caseService.getVariableLocal(caseExecutionId, "aVariableName");

     // then
     assertNotNull(value);
     assertEquals("abc", value);
  }

  public void testGetVariableLocalInvalidCaseExecutionId() {
    try {
      caseService.getVariableLocal("invalid", "aVariableName");
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }

    try {
      caseService.getVariableLocal(null, "aVariableName");
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testDisableTaskShouldCompleteCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    // then

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance has been completed
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .completed()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testDisableStageShouldCompleteCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    // then

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance has been completed
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .completed()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCompleteTaskShouldCompleteCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    Task task = taskService
        .createTaskQuery()
        .singleResult();

    assertNotNull(task);

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .complete();

    // then

    // the task has been completed and has been deleted
    task = taskService
        .createTaskQuery()
        .singleResult();

    assertNull(task);

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance has been completed
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .completed()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testCompleteStageShouldCompleteCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .complete();

    // then

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance has been completed
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .completed()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"})
  public void testCompleteTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    Task task = taskService
        .createTaskQuery()
        .singleResult();

    assertNotNull(task);

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .complete();

    // then

    // the task has been completed and has been deleted
    task = taskService
        .createTaskQuery()
        .singleResult();

    assertNull(task);

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance is still active
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .active()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testCompleteStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .complete();

    // then

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance is still active
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .active()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCompleteCaseInstanceWithEnabledTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    // when

    caseService
      .withCaseExecution(caseInstanceId)
      .complete();

    // then

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNull(caseExecution);

    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testCompleteCaseInstanceWithEnabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    // when

    caseService
      .withCaseExecution(caseInstanceId)
      .complete();

    // then

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult();

    assertNull(caseExecution);

    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCompleteCaseInstanceWithActiveTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when

    try {
      caseService
        .withCaseExecution(caseInstanceId)
        .complete();
      fail("It should not be possible to complete a case instance containing an active task.");
    } catch (ProcessEngineException e) {}

    // then

    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(caseExecution);
    assertTrue(caseExecution.isActive());

    // the case instance is still active
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testCompleteCaseInstanceWithActiveStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when

    try {
      caseService
        .withCaseExecution(caseInstanceId)
        .complete();
      fail("It should not be possible to complete a case instance containing an active stage.");
    } catch (ProcessEngineException e) {}

    // then

    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult();

    assertNotNull(caseExecution);
    assertTrue(caseExecution.isActive());

    // the case instance is still active
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCompleteEnabledTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .complete();
      fail("Should not be able to complete task.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testCompleteEnabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .complete();
      fail("Should not be able to complete stage.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"})
  public void testCompleteDisabledTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .complete();
      fail("Should not be able to complete task.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testCompleteDisabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .complete();
      fail("Should not be able to complete stage.");
    } catch (ProcessEngineException e) {}
  }


  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"})
  public void testCompleteTaskAndSetVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    Task task = taskService
        .createTaskQuery()
        .singleResult();

    assertNotNull(task);

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .setVariable("aVariableName", "abc")
      .setVariable("anotherVariableName", 999)
      .complete();

    // then

    // the task has been completed and has been deleted
    task = taskService
        .createTaskQuery()
        .singleResult();

    assertNull(task);

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance is still active
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .active()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseInstanceId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"})
  public void testCompleteTaskAndSetVariableLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    Task task = taskService
        .createTaskQuery()
        .singleResult();

    assertNotNull(task);

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .complete();

    // then

    // the task has been completed and has been deleted
    task = taskService
        .createTaskQuery()
        .singleResult();

    assertNull(task);

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance is still active
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .active()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());

    // the variables has been set and due to the completion
    // also removed in one command
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertTrue(result.isEmpty());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"})
  public void testCompleteTaskAndRemoveVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .setVariable("aVariableName", "abc")
      .setVariable("anotherVariableName", 999)
      .manualStart();

    Task task = taskService
        .createTaskQuery()
        .singleResult();

    assertNotNull(task);

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .removeVariable("aVariableName")
      .removeVariable("anotherVariableName")
      .complete();

    // then

    // the task has been completed and has been deleted
    task = taskService
        .createTaskQuery()
        .singleResult();

    assertNull(task);

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance is still active
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .active()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());

    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertTrue(result.isEmpty());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"})
  public void testCompleteTaskAndRemoveVariableLocal() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .manualStart();

    Task task = taskService
        .createTaskQuery()
        .singleResult();

    assertNotNull(task);

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .removeVariableLocal("aVariableName")
      .removeVariableLocal("anotherVariableName")
      .complete();

    // then

    // the task has been completed and has been deleted
    task = taskService
        .createTaskQuery()
        .singleResult();

    assertNull(task);

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance is still active
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .active()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());

    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertTrue(result.isEmpty());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/emptyCasePlanModelCase.cmmn"})
  public void testAutoCompletionOfEmptyCase() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // when
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    // then
    CaseInstance caseInstance = caseService
      .createCaseInstanceQuery()
      .completed()
      .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/emptyStageCase.cmmn"})
  public void testAutoCompletionOfEmptyStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // then

    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult();

    assertNull(caseExecution);

    CaseInstance caseInstance = caseService
      .createCaseInstanceQuery()
      .completed()
      .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  public void testCompleteInvalidCaseExeuction() {
    try {
      caseService
        .withCaseExecution("invalid")
        .complete();
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }

    try {
      caseService
        .withCaseExecution(null)
        .complete();
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCloseActiveCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    String caseInstanceId = caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseInstanceId)
        .close();
      fail("It should not be possible to close an active case instance.");
    } catch (ProcessEngineException e) {
    }

    // then
    CaseInstance caseInstance = caseService
      .createCaseInstanceQuery()
      .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCloseCompletedCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    String caseInstanceId = caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // disable human task -> case instance is completed
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    // when
    caseService
      .withCaseExecution(caseInstanceId)
      .close();

    // then
    CaseInstance caseInstance = caseService
      .createCaseInstanceQuery()
      .singleResult();

    assertNull(caseInstance);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCloseTask() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .close();
      fail("It should not be possible to close a task.");
    } catch (ProcessEngineException e) {

    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testCloseStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .close();
      fail("It should not be possible to close a stage.");
    } catch (ProcessEngineException e) {

    }

  }

  public void testCloseInvalidCaseExeuction() {
    try {
      caseService
        .withCaseExecution("invalid")
        .close();
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }

    try {
      caseService
        .withCaseExecution(null)
        .close();
      fail("The case execution should not be found.");
    } catch (ProcessEngineException e) {

    }
  }
}
