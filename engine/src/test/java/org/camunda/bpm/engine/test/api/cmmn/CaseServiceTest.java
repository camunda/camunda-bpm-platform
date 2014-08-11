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

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecutionCommandBuilder;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class CaseServiceTest extends PluggableProcessEngineTestCase {

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

  public void testManualStartInvalidCaseExecution() {
    try {
      caseService
          .withCaseExecution("invalid")
          .manualStart();
      fail();
    } catch (NotFoundException e) { }

    try {
      caseService
        .withCaseExecution(null)
        .manualStart();
      fail();
    } catch (NotValidException e) { }

  }

  public void testCompleteInvalidCaseExeuction() {
    try {
      caseService
        .withCaseExecution("invalid")
        .complete();
      fail("The case execution should not be found.");
    } catch (NotFoundException e) {

    }

    try {
      caseService
        .withCaseExecution(null)
        .complete();
      fail("The case execution should not be found.");
    } catch (NotValidException e) {

    }
  }

  public void testCloseInvalidCaseExeuction() {
    try {
      caseService
        .withCaseExecution("invalid")
        .close();
      fail("The case execution should not be found.");
    } catch (NotFoundException e) {

    }

    try {
      caseService
        .withCaseExecution(null)
        .close();
      fail("The case execution should not be found.");
    } catch (NotValidException e) {

    }
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
    } catch (NotValidException e) {
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
    } catch (NotValidException e) {
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
    } catch (NotFoundException e) {

    }

    try {
      caseService.getVariables(null);
      fail("The case execution should not be found.");
    } catch (NotValidException e) {

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
    } catch (NotFoundException e) {

    }

    try {
      caseService.getVariables(null, null);
      fail("The case execution should not be found.");
    } catch (NotValidException e) {

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
    } catch (NotFoundException e) {

    }

    try {
      caseService.getVariablesLocal(null);
      fail("The case execution should not be found.");
    } catch (NotValidException e) {

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
    } catch (NotFoundException e) {

    }

    try {
      caseService.getVariablesLocal(null, null);
      fail("The case execution should not be found.");
    } catch (NotValidException e) {

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
    } catch (NotFoundException e) {

    }

    try {
      caseService.getVariable(null, "aVariableName");
      fail("The case execution should not be found.");
    } catch (NotValidException e) {

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
    } catch (NotFoundException e) {

    }

    try {
      caseService.getVariableLocal(null, "aVariableName");
      fail("The case execution should not be found.");
    } catch (NotValidException e) {

    }
  }

}
