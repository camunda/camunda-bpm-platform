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

import static org.camunda.bpm.engine.variable.Variables.*;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.StringValue;

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
  public void testExecuteSetVariableTyped() {
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
      .setVariable("aVariableName", stringValue("abc"))
      .setVariable("anotherVariableName", integerValue(null))
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
        assertEquals(stringValue("abc"), variable.getTypedValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(integerValue(null), variable.getTypedValue());
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
  public void testExecuteSetVariablesTyped() {
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

    VariableMap variables = createVariables()
        .putValueTyped("aVariableName", stringValue("abc"))
        .putValueTyped("anotherVariableName", integerValue(null));

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
        assertEquals(stringValue("abc"), variable.getTypedValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(integerValue(null), variable.getTypedValue());
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
  public void testExecuteSetVariableAndVariablesTyped() {
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

    VariableMap variables = createVariables()
        .putValueTyped("aVariableName", stringValue("abc"))
        .putValueTyped("anotherVariableName", integerValue(null));

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariables(variables)
      .setVariable("aThirdVariable", booleanValue(null))
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
        assertEquals(stringValue("abc"), variable.getTypedValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(integerValue(null), variable.getTypedValue());
      } else if (variable.getName().equals("aThirdVariable")) {
        assertEquals("aThirdVariable", variable.getName());
        assertEquals(booleanValue(null), variable.getTypedValue());
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
  public void testExecuteSetVariablesLocalTyped() {
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

    VariableMap variables = createVariables()
        .putValueTyped("aVariableName", stringValue("abc"))
        .putValueTyped("anotherVariableName", integerValue(null));

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
        assertEquals(stringValue("abc"), variable.getTypedValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(integerValue(null), variable.getTypedValue());
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

     assertEquals(variables, caseService.getVariablesTyped(caseExecutionId, true));
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariablesTyped() {
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
     VariableMap variables = caseService.getVariablesTyped(caseExecutionId);

     // then
     assertNotNull(variables);
     assertFalse(variables.isEmpty());
     assertEquals(2, variables.size());

     assertEquals("abc", variables.get("aVariableName"));
     assertEquals(999, variables.get("anotherVariableName"));

     assertEquals(variables, caseService.getVariablesTyped(caseExecutionId, true));
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

     assertEquals(variables, caseService.getVariables(caseExecutionId, names));
  }


  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariablesWithVariableNamesTyped() {
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
     VariableMap variables = caseService.getVariablesTyped(caseExecutionId, names, true);

     // then
     assertNotNull(variables);
     assertFalse(variables.isEmpty());
     assertEquals(2, variables.size());

     assertEquals("abc", variables.get("aVariableName"));
     assertEquals(999, variables.get("anotherVariableName"));

     assertEquals(variables, caseService.getVariables(caseExecutionId, names));
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

     assertEquals(variables, caseService.getVariablesLocal(caseExecutionId));
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariablesLocalTyped() {
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
     VariableMap variables = caseService.getVariablesLocalTyped(caseExecutionId);

     // then
     assertNotNull(variables);
     assertFalse(variables.isEmpty());
     assertEquals(2, variables.size());

     assertEquals("abc", variables.get("aVariableName"));
     assertEquals(999, variables.get("anotherVariableName"));

     assertEquals(variables, caseService.getVariablesLocalTyped(caseExecutionId, true));
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

     assertEquals(variables, caseService.getVariablesLocal(caseExecutionId, names));
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariablesLocalWithVariableNamesTyped() {
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
     VariableMap variables = caseService.getVariablesLocalTyped(caseExecutionId, names, true);

     // then
     assertNotNull(variables);
     assertFalse(variables.isEmpty());
     assertEquals(2, variables.size());

     assertEquals("abc", variables.get("aVariableName"));
     assertEquals(999, variables.get("anotherVariableName"));

     assertEquals(variables, caseService.getVariablesLocal(caseExecutionId, names));
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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariableTyped() {
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
        .setVariable("aSerializedObject", Variables.objectValue(Arrays.asList("1", "2")).create())
        .create()
        .getId();

     String caseExecutionId = caseService
         .createCaseExecutionQuery()
         .activityId("PI_HumanTask_1")
         .singleResult()
         .getId();

     // when
     StringValue stringValue = caseService.getVariableTyped(caseExecutionId, "aVariableName");
     ObjectValue objectValue = caseService.getVariableTyped(caseExecutionId, "aSerializedObject");
     ObjectValue serializedObjectValue = caseService.getVariableTyped(caseExecutionId, "aSerializedObject", false);

     // then
     assertNotNull(stringValue.getValue());
     assertNotNull(objectValue.getValue());
     assertTrue(objectValue.isDeserialized());
     assertEquals(Arrays.asList("1", "2"), objectValue.getValue());
     assertFalse(serializedObjectValue.isDeserialized());
     assertNotNull(serializedObjectValue.getValueSerialized());
  }

  public void testGetVariableTypedInvalidCaseExecutionId() {
    try {
      caseService.getVariableTyped("invalid", "aVariableName");
      fail("The case execution should not be found.");
    } catch (NotFoundException e) {

    }

    try {
      caseService.getVariableTyped(null, "aVariableName");
      fail("The case execution should not be found.");
    } catch (NotValidException e) {

    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testSetVariable() {
    // given:
    // a deployed case definition
    // and an active case instance
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService.setVariable(caseExecutionId, "aVariableName", "abc");

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
        .caseExecutionIdIn(caseInstanceId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    VariableInstance variable = result.get(0);
    assertEquals("aVariableName", variable.getName());
    assertEquals("abc", variable.getValue());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testSetVariables() {
    // given:
    // a deployed case definition
    // and an active case instance
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 123);
    caseService.setVariables(caseExecutionId, variables);

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
        .caseExecutionIdIn(caseInstanceId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

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
        assertEquals(123, variable.getValue());

      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testSetVariableLocal() {
    // given:
    // a deployed case definition
    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService.setVariableLocal(caseExecutionId, "aVariableName", "abc");

    // then

    // query by case instance id
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseInstanceId)
        .list();

    assertTrue(result.isEmpty());

    // query by caseExecutionId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    VariableInstance variable = result.get(0);
    assertEquals("aVariableName", variable.getName());
    assertEquals("abc", variable.getValue());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testSetVariablesLocal() {
    // given:
    // a deployed case definition
    // and an active case instance
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 123);
    caseService.setVariablesLocal(caseExecutionId, variables);

    // then

    // query by case instance id
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseInstanceId)
        .list();

    assertTrue(result.isEmpty());

    // query by caseExecutionId
    result = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(caseExecutionId)
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {
      if (variable.getName().equals("aVariableName")) {
        assertEquals(caseExecutionId, variable.getCaseExecutionId());
        assertEquals(caseInstanceId, variable.getCaseInstanceId());

        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());

      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals(caseExecutionId, variable.getCaseExecutionId());
        assertEquals(caseInstanceId, variable.getCaseInstanceId());

        assertEquals("anotherVariableName", variable.getName());
        assertEquals(123, variable.getValue());

      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testGetVariableTypedLocal() {
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

     caseService.withCaseExecution(caseExecutionId)
        .setVariableLocal("aVariableName", "abc")
        .setVariableLocal("anotherVariableName", 999)
        .setVariableLocal("aSerializedObject", Variables.objectValue(Arrays.asList("1", "2")).create())
        .execute();

     // when
     StringValue stringValue = caseService.getVariableLocalTyped(caseExecutionId, "aVariableName");
     ObjectValue objectValue = caseService.getVariableLocalTyped(caseExecutionId, "aSerializedObject");
     ObjectValue serializedObjectValue = caseService.getVariableLocalTyped(caseExecutionId, "aSerializedObject", false);

     // then
     assertNotNull(stringValue.getValue());
     assertNotNull(objectValue.getValue());
     assertTrue(objectValue.isDeserialized());
     assertEquals(Arrays.asList("1", "2"), objectValue.getValue());
     assertFalse(serializedObjectValue.isDeserialized());
     assertNotNull(serializedObjectValue.getValueSerialized());
  }

  public void testGetVariableLocalTypedInvalidCaseExecutionId() {
    try {
      caseService.getVariableLocalTyped("invalid", "aVariableName");
      fail("The case execution should not be found.");
    } catch (NotFoundException e) {

    }

    try {
      caseService.getVariableLocalTyped(null, "aVariableName");
      fail("The case execution should not be found.");
    } catch (NotValidException e) {

    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testRemoveVariable() {
    // given:
    // a deployed case definition
    // and an active case instance
    caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .setVariable("aVariableName", "abc")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService.removeVariable(caseExecutionId, "aVariableName");

    // then the variable should be gone
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testRemoveVariables() {
    // given:
    // a deployed case definition
    // and an active case instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "abc");
    variables.put("anotherVariable", 123);

    caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .setVariables(variables)
        .setVariable("aThirdVariable", "def")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService.removeVariables(caseExecutionId, variables.keySet());

    // then there should be only one variable left
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variable);
    assertEquals("aThirdVariable", variable.getName());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testRemoveVariableLocal() {
    // given:
    // a deployed case definition
    // and an active case instance
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService.setVariableLocal(caseExecutionId, "aVariableName", "abc");

    // when
    caseService.removeVariableLocal(caseInstanceId, "aVariableName");

    // then the variable should still be there
    assertEquals(1, runtimeService.createVariableInstanceQuery().count());

    // when
    caseService.removeVariableLocal(caseExecutionId, "aVariableName");

    // then the variable should be gone
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testRemoveVariablesLocal() {
    // given:
    // a deployed case definition
    // and an active case instance
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();


    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "abc");
    variables.put("anotherVariable", 123);

    caseService.setVariablesLocal(caseExecutionId, variables);
    caseService.setVariableLocal(caseExecutionId, "aThirdVariable", "def");

    // when
    caseService.removeVariablesLocal(caseInstanceId, variables.keySet());

    // then no variables should have been removed
    assertEquals(3, runtimeService.createVariableInstanceQuery().count());

    // when
    caseService.removeVariablesLocal(caseExecutionId, variables.keySet());

    // then there should be only one variable left
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variable);
    assertEquals("aThirdVariable", variable.getName());
  }

}
