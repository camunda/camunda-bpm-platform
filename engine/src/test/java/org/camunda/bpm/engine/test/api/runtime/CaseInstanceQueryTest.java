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
package org.camunda.bpm.engine.test.api.runtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class CaseInstanceQueryTest extends PluggableProcessEngineTestCase {

  private static String CASE_DEFINITION_KEY = "oneTaskCase";
  private static String CASE_DEFINITION_KEY_2 = "oneTaskCase2";

  private List<String> caseInstanceIds;

  /**
   * Setup starts 4 case instances of oneTaskCase
   * and 1 instance of oneTaskCase2
   */
  protected void setUp() throws Exception {
    super.setUp();
    repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
      .addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/oneTaskCase2.cmmn")
      .deploy();

    caseInstanceIds = new ArrayList<String>();
    for (int i = 0; i < 4; i++) {
      String id = caseService
          .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
          .businessKey(String.valueOf(i))
          .create()
          .getId();

      caseInstanceIds.add(id);
    }
    String id = caseService
        .withCaseDefinitionByKey(CASE_DEFINITION_KEY_2)
        .businessKey("1")
        .create()
        .getId();

    caseInstanceIds.add(id);
  }

  protected void tearDown() throws Exception {
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }

  private void verifyQueryResults(CaseInstanceQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  private void verifySingleResultFails(CaseInstanceQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testCaseInstanceProperties() {
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY_2)
        .singleResult()
        .getId();

    CaseInstance caseInstance = caseService
      .createCaseInstanceQuery()
      .caseDefinitionKey(CASE_DEFINITION_KEY_2)
      .singleResult();

    assertNotNull(caseInstance.getId());
    assertEquals(caseInstance.getId(), caseInstance.getCaseInstanceId());
    assertEquals("1", caseInstance.getBusinessKey());
    assertEquals(caseDefinitionId, caseInstance.getCaseDefinitionId());
    assertEquals("CasePlanModel_1", caseInstance.getActivityId());
    assertNull(caseInstance.getActivityName());
    assertNull(caseInstance.getParentId());
    assertTrue(caseInstance.isActive());
    assertFalse(caseInstance.isEnabled());

  }

  public void testQueryWithoutQueryParameter() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    verifyQueryResults(query, 5);
  }

  public void testQueryByCaseDefinitionKey() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.caseDefinitionKey(CASE_DEFINITION_KEY_2);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidCaseDefinitionKey() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.caseDefinitionKey("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionKey(null);
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByCaseDefinitionId() {
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .singleResult()
        .getId();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.caseDefinitionId(caseDefinitionId);

    verifyQueryResults(query, 4);
  }

  public void testQueryByInvalidCaseDefinitionId() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.caseDefinitionId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionId(null);
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByActive() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.active();

    verifyQueryResults(query, 5);
  }

  public void testQueryByCompleted() {

    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithManualActivation.cmmn")
        .deploy();

    for (int i = 0; i < 4; i++) {
      String id = caseService
          .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
          .businessKey(String.valueOf(i))
          .create()
          .getId();

      caseInstanceIds.add(id);
    }

    List<CaseExecution> executions = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_1")
      .list();

    for (CaseExecution caseExecution : executions) {
      caseService
        .withCaseExecution(caseExecution.getId())
        .disable();
    }

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.completed();

    verifyQueryResults(query, 4);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/CaseInstanceQueryTest.testQueryByTerminated.cmmn"})
  public void testQueryByTerminated() {
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("termination")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .caseInstanceId(caseInstanceId)
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .complete();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.terminated();

    verifyQueryResults(query, 1);
  }

  public void testQueryByCaseInstanceBusinessKey() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.caseInstanceBusinessKey("1");

    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidCaseInstanceBusinessKey() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.caseInstanceBusinessKey("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseInstanceBusinessKey(null);
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByCaseInstanceBusinessKeyAndCaseDefinitionKey() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query
      .caseInstanceBusinessKey("0")
      .caseDefinitionKey(CASE_DEFINITION_KEY);

    verifyQueryResults(query, 1);

    query
      .caseInstanceBusinessKey("1")
      .caseDefinitionKey(CASE_DEFINITION_KEY);

    verifyQueryResults(query, 1);

    query
      .caseInstanceBusinessKey("2")
      .caseDefinitionKey(CASE_DEFINITION_KEY);

    verifyQueryResults(query, 1);

    query
      .caseInstanceBusinessKey("3")
      .caseDefinitionKey(CASE_DEFINITION_KEY);

    verifyQueryResults(query, 1);

    query
      .caseInstanceBusinessKey("1")
      .caseDefinitionKey(CASE_DEFINITION_KEY_2);

    verifyQueryResults(query, 1);

  }

  public void testQueryByCaseInstanceId() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    for (String caseInstanceId : caseInstanceIds) {
      query.caseInstanceId(caseInstanceId);

      verifyQueryResults(query, 1);
    }

  }

  public void testQueryByInvalidCaseInstanceId() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.caseInstanceId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseInstanceId(null);
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByNullVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueEquals("aNullValue", null);

    verifyQueryResults(query, 1);
  }

  public void testQueryByStringVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueEquals("aStringValue", "abc");

    verifyQueryResults(query, 1);
  }

  public void testQueryByBooleanVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueEquals("aBooleanValue", true);

    verifyQueryResults(query, 1);
  }

  public void testQueryByShortVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueEquals("aShortValue", (short) 123);

    verifyQueryResults(query, 1);
  }

  public void testQueryByIntegerVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueEquals("anIntegerValue", 456);

    verifyQueryResults(query, 1);
  }

  public void testQueryByLongVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueEquals("aLongValue", (long) 789);

    verifyQueryResults(query, 1);
  }

  public void testQueryByDateVariableValueEquals() {
    Date now = new Date();
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueEquals("aDateValue", now);

    verifyQueryResults(query, 1);
  }

  public void testQueryByDoubleVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueEquals("aDoubleValue", 1.5);

    verifyQueryResults(query, 1);
  }

  public void testQueryByByteArrayVariableValueEquals() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueEquals("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableVariableValueEquals() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueEquals("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByStringVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueNotEquals("aStringValue", "abd");

    verifyQueryResults(query, 1);
  }

  public void testQueryByBooleanVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueNotEquals("aBooleanValue", false);

    verifyQueryResults(query, 1);
  }

  public void testQueryByShortVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueNotEquals("aShortValue", (short) 124);

    verifyQueryResults(query, 1);
  }

  public void testQueryByIntegerVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueNotEquals("anIntegerValue", 457);

    verifyQueryResults(query, 1);
  }

  public void testQueryByLongVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueNotEquals("aLongValue", (long) 790);

    verifyQueryResults(query, 1);
  }

  public void testQueryByDateVariableValueNotEquals() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    Date before = new Date(now.getTime() - 100000);

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueNotEquals("aDateValue", before);

    verifyQueryResults(query, 1);
  }

  public void testQueryByDoubleVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueNotEquals("aDoubleValue", 1.6);

    verifyQueryResults(query, 1);
  }

  public void testQueryByByteArrayVariableValueNotEquals() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueNotEquals("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableVariableValueNotEquals() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueNotEquals("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNullVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueGreaterThan("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  public void testQueryByStringVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThan("aStringValue", "ab");

    verifyQueryResults(query, 1);

  }

  public void testQueryByBooleanVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueGreaterThan("aBooleanValue", false).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  public void testQueryByShortVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThan("aShortValue", (short) 122);

    verifyQueryResults(query, 1);

  }

  public void testQueryByIntegerVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThan("anIntegerValue", 455);

    verifyQueryResults(query, 1);

  }

  public void testQueryByLongVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThan("aLongValue", (long) 788);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDateVariableValueGreaterThan() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    Date before = new Date(now.getTime() - 100000);

    query.variableValueGreaterThan("aDateValue", before);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDoubleVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThan("aDoubleValue", 1.4);

    verifyQueryResults(query, 1);

  }

  public void testQueryByByteArrayVariableValueGreaterThan() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueGreaterThan("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableVariableGreaterThan() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueGreaterThan("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNullVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueGreaterThanOrEqual("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  public void testQueryByStringVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("aStringValue", "ab");

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("aStringValue", "abc");

    verifyQueryResults(query, 1);

  }

  public void testQueryByBooleanVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueGreaterThanOrEqual("aBooleanValue", false).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  public void testQueryByShortVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("aShortValue", (short) 122);

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("aShortValue", (short) 123);

    verifyQueryResults(query, 1);

  }

  public void testQueryByIntegerVariableValueGreaterThanOrEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("anIntegerValue", 455);

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("anIntegerValue", 456);

    verifyQueryResults(query, 1);

  }

  public void testQueryByLongVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("aLongValue", (long) 788);

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("aLongValue", (long) 789);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDateVariableValueGreaterThanOrEqual() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    Date before = new Date(now.getTime() - 100000);

    query.variableValueGreaterThanOrEqual("aDateValue", before);

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("aDateValue", now);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDoubleVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("aDoubleValue", 1.4);

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueGreaterThanOrEqual("aDoubleValue", 1.5);

    verifyQueryResults(query, 1);

  }

  public void testQueryByByteArrayVariableValueGreaterThanOrEqual() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueGreaterThanOrEqual("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableVariableGreaterThanOrEqual() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueGreaterThanOrEqual("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNullVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueLessThan("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  public void testQueryByStringVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLessThan("aStringValue", "abd");

    verifyQueryResults(query, 1);

  }

  public void testQueryByBooleanVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueLessThan("aBooleanValue", false).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  public void testQueryByShortVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLessThan("aShortValue", (short) 124);

    verifyQueryResults(query, 1);

  }

  public void testQueryByIntegerVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLessThan("anIntegerValue", 457);

    verifyQueryResults(query, 1);

  }

  public void testQueryByLongVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLessThan("aLongValue", (long) 790);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDateVariableValueLessThan() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    Date after = new Date(now.getTime() + 100000);

    query.variableValueLessThan("aDateValue", after);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDoubleVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLessThan("aDoubleValue", 1.6);

    verifyQueryResults(query, 1);

  }

  public void testQueryByByteArrayVariableValueLessThan() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueLessThan("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableVariableLessThan() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueLessThan("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNullVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueLessThanOrEqual("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  public void testQueryByStringVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("aStringValue", "abd");

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("aStringValue", "abc");

    verifyQueryResults(query, 1);

  }

  public void testQueryByBooleanVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueLessThanOrEqual("aBooleanValue", false).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  public void testQueryByShortVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("aShortValue", (short) 124);

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("aShortValue", (short) 123);

    verifyQueryResults(query, 1);

  }

  public void testQueryByIntegerVariableValueLessThanOrEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("anIntegerValue", 457);

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("anIntegerValue", 456);

    verifyQueryResults(query, 1);

  }

  public void testQueryByLongVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("aLongValue", (long) 790);

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("aLongValue", (long) 789);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDateVariableValueLessThanOrEqual() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    Date after = new Date(now.getTime() + 100000);

    query.variableValueLessThanOrEqual("aDateValue", after);

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("aDateValue", now);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDoubleVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("aDoubleValue", 1.6);

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueLessThanOrEqual("aDoubleValue", 1.5);

    verifyQueryResults(query, 1);

  }

  public void testQueryByByteArrayVariableValueLessThanOrEqual() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueLessThanOrEqual("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableVariableLessThanOrEqual() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueLessThanOrEqual("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNullVariableValueLike() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    try {
      query.variableValueLike("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  public void testQueryByStringVariableValueLike() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    query.variableValueLike("aStringValue", "ab%");

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueLike("aStringValue", "%bc");

    verifyQueryResults(query, 1);

    query = caseService.createCaseInstanceQuery();

    query.variableValueLike("aStringValue", "%b%");

    verifyQueryResults(query, 1);
  }

  public void testQuerySorting() {
    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    // asc
    query
      .orderByCaseDefinitionId()
      .asc();
    verifyQueryResults(query, 5);

    query = caseService.createCaseInstanceQuery();

    query
      .orderByCaseDefinitionKey()
      .asc();
    verifyQueryResults(query, 5);

    query = caseService.createCaseInstanceQuery();

    query
      .orderByCaseInstanceId()
      .asc();
    verifyQueryResults(query, 5);

    // desc

    query = caseService.createCaseInstanceQuery();

    query
      .orderByCaseDefinitionId()
      .desc();
    verifyQueryResults(query, 5);

    query = caseService.createCaseInstanceQuery();

    query
      .orderByCaseDefinitionKey()
      .desc();
    verifyQueryResults(query, 5);

    query = caseService.createCaseInstanceQuery();

    query
      .orderByCaseInstanceId()
      .desc();
    verifyQueryResults(query, 5);

    query = caseService.createCaseInstanceQuery();

  }

  public void testCaseVariableValueEqualsNumber() throws Exception {
    // long
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("var", 123L)
      .create();

    // non-matching long
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("var", 12345L)
      .create();

    // short
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("var", (short) 123)
      .create();

    // double
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("var", 123.0d)
      .create();

    // integer
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("var", 123)
      .create();

    // untyped null (should not match)
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("var", null)
      .create();

    // typed null (should not match)
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("var", Variables.longValue(null))
      .create();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("var", "123")
      .create();

    assertEquals(4, caseService.createCaseInstanceQuery().variableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, caseService.createCaseInstanceQuery().variableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, caseService.createCaseInstanceQuery().variableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, caseService.createCaseInstanceQuery().variableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, caseService.createCaseInstanceQuery().variableValueEquals("var", Variables.numberValue(null)).count());

    // other operators
    assertEquals(4, caseService.createCaseInstanceQuery().variableValueNotEquals("var", Variables.numberValue(123)).count());
    assertEquals(1, caseService.createCaseInstanceQuery().variableValueGreaterThan("var", Variables.numberValue(123L)).count());
    assertEquals(5, caseService.createCaseInstanceQuery().variableValueGreaterThanOrEqual("var", Variables.numberValue(123.0d)).count());
    assertEquals(0, caseService.createCaseInstanceQuery().variableValueLessThan("var", Variables.numberValue((short) 123)).count());
    assertEquals(4, caseService.createCaseInstanceQuery().variableValueLessThanOrEqual("var", Variables.numberValue((short) 123)).count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/superProcessWithCaseCallActivity.bpmn20.xml"})
  public void testQueryBySuperProcessInstanceId() {
    String superProcessInstanceId = runtimeService.startProcessInstanceByKey("subProcessQueryTest").getId();

    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .superProcessInstanceId(superProcessInstanceId);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/superProcessWithCaseCallActivityInsideSubProcess.bpmn20.xml"})
  public void testQueryBySuperProcessInstanceIdNested() {
    String superProcessInstanceId = runtimeService.startProcessInstanceByKey("subProcessQueryTest").getId();

    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .superProcessInstanceId(superProcessInstanceId);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidSuperProcessInstanceId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .superProcessInstanceId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.superProcessInstanceId(null);
      fail();
    } catch (NotValidException e) {}

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryBySubProcessInstanceId() {
    String superCaseInstanceId = caseService.createCaseInstanceByKey("oneProcessTaskCase").getId();

    String subProcessInstanceId = runtimeService
        .createProcessInstanceQuery()
        .superCaseInstanceId(superCaseInstanceId)
        .singleResult()
        .getId();

    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .subProcessInstanceId(subProcessInstanceId);

    verifyQueryResults(query, 1);

    CaseInstance caseInstance = query.singleResult();
    assertEquals(superCaseInstanceId, caseInstance.getId());
  }

  public void testQueryByInvalidSubProcessInstanceId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .subProcessInstanceId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.subProcessInstanceId(null);
      fail();
    } catch (NotValidException e) {
      // expected
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn"})
  public void testQueryBySuperCaseInstanceId() {
    String superCaseInstanceId = caseService.createCaseInstanceByKey("oneCaseTaskCase").getId();

    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .superCaseInstanceId(superCaseInstanceId);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidSuperCaseInstanceId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .superCaseInstanceId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.superCaseInstanceId(null);
      fail();
    } catch (NotValidException e) {
      // expected
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn"})
  public void testQueryBySubCaseInstanceId() {
    String superCaseInstanceId = caseService.createCaseInstanceByKey("oneCaseTaskCase").getId();

    String subCaseInstanceId = caseService
        .createCaseInstanceQuery()
        .superCaseInstanceId(superCaseInstanceId)
        .singleResult()
        .getId();

    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .subCaseInstanceId(subCaseInstanceId);

    verifyQueryResults(query, 1);

    CaseInstance caseInstance = query.singleResult();
    assertEquals(superCaseInstanceId, caseInstance.getId());
  }

  public void testQueryByInvalidSubCaseInstanceId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .subCaseInstanceId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.subCaseInstanceId(null);
      fail();
    } catch (NotValidException e) {
      // expected
    }
  }

  public void testQueryByDeploymentId() {
    String deploymentId = repositoryService
        .createDeploymentQuery()
        .singleResult()
        .getId();

    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .deploymentId(deploymentId);

    verifyQueryResults(query, 5);
  }

  public void testQueryByInvalidDeploymentId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .deploymentId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.deploymentId(null);
      fail();
    } catch (NotValidException e) {
      // expected
    }
  }

}
