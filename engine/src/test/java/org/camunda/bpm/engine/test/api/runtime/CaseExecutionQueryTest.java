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

import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.caseExecutionByDefinitionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.caseExecutionByDefinitionKey;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.caseExecutionById;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.NullTolerantComparator;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionQueryTest extends PluggableProcessEngineTestCase {

  private static String CASE_DEFINITION_KEY = "oneTaskCase";
  private static String CASE_DEFINITION_KEY_2 = "twoTaskCase";

  /**
   * Setup starts 4 case instances of oneTaskCase
   * and 1 instance of twoTaskCase
   */
  protected void setUp() throws Exception {
    super.setUp();
    repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
      .addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn")
      .deploy();

    for (int i = 0; i < 4; i++) {
      caseService
        .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
        .businessKey(i + "")
        .create();
    }
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY_2)
      .businessKey("1")
      .create();
  }

  protected void tearDown() throws Exception {
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }

  private void verifyQueryResults(CaseExecutionQuery query, int countExpected) {
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

  protected void verifyQueryWithOrdering(CaseExecutionQuery query, int countExpected, NullTolerantComparator<CaseExecution> expectedOrdering) {
    verifyQueryResults(query, countExpected);
    TestOrderingUtil.verifySorting(query.list(), expectedOrdering);
  }

  private void verifySingleResultFails(CaseExecutionQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryWithoutQueryParameter() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    verifyQueryResults(query, 11);
  }

  public void testQueryByCaseDefinitionKey() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseDefinitionKey(CASE_DEFINITION_KEY);

    verifyQueryResults(query, 8);

    query.caseDefinitionKey(CASE_DEFINITION_KEY_2);

    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidCaseDefinitionKey() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseDefinitionId(caseDefinitionId);

    verifyQueryResults(query, 8);

    caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY_2)
        .singleResult()
        .getId();

    query.caseDefinitionId(caseDefinitionId);

    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidCaseDefinitionId() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseDefinitionId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionId(null);
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByCaseInstaceId() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    List<CaseInstance> caseInstances = caseService
        .createCaseInstanceQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .list();

    for (CaseInstance caseInstance : caseInstances) {
      query.caseInstanceId(caseInstance.getId());

      verifyQueryResults(query, 2);
    }

    CaseInstance instance = caseService
        .createCaseInstanceQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY_2)
        .singleResult();

    query.caseInstanceId(instance.getId());

    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidCaseInstanceId() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseInstanceId(null);
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByCaseInstanceBusinessKey() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceBusinessKey("0");

    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidCaseInstanceBusinessKey() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceBusinessKey("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseInstanceBusinessKey(null);
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByCaseInstanceBusinessKeyAndCaseDefinitionKey() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query
      .caseInstanceBusinessKey("0")
      .caseDefinitionKey(CASE_DEFINITION_KEY);

    verifyQueryResults(query, 2);

    query
      .caseInstanceBusinessKey("1")
      .caseDefinitionKey(CASE_DEFINITION_KEY);

    verifyQueryResults(query, 2);

    query
      .caseInstanceBusinessKey("2")
      .caseDefinitionKey(CASE_DEFINITION_KEY);

    verifyQueryResults(query, 2);

    query
      .caseInstanceBusinessKey("3")
      .caseDefinitionKey(CASE_DEFINITION_KEY);

    verifyQueryResults(query, 2);

    query
      .caseInstanceBusinessKey("1")
      .caseDefinitionKey(CASE_DEFINITION_KEY_2);

    verifyQueryResults(query, 3);

  }

  public void testQueryByCaseExecutionId() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    List<CaseExecution> executions = caseService
        .createCaseExecutionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY_2)
        .list();

    for (CaseExecution execution : executions) {
      query.caseExecutionId(execution.getId());

      verifyQueryResults(query, 1);
    }

  }

  public void testQueryByInvalidCaseExecutionId() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseExecutionId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseExecutionId(null);
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByActivityId() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.activityId("PI_HumanTask_1");

    verifyQueryResults(query, 5);

    query.activityId("PI_HumanTask_2");

    verifyQueryResults(query, 1);

  }

  public void testQueryByInvalidActivityId() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.activityId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.activityId(null);
      fail();
    } catch (NotValidException e) {}

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneMilestoneCase.cmmn"})
  public void testQueryByAvailable() {
    caseService
      .withCaseDefinitionByKey("oneMilestoneCase")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.available();

    verifyQueryResults(query, 1);

  }

  public void testQueryByEnabled() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.enabled();

    verifyQueryResults(query, 2);

  }

  public void testQueryByActive() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.active();

    verifyQueryResults(query, 9);

  }

  public void testQueryByDisabled() {
    List<CaseExecution> caseExecutions= caseService
        .createCaseExecutionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY_2)
        .activityId("PI_HumanTask_1")
        .list();

    for (CaseExecution caseExecution : caseExecutions) {
      caseService
        .withCaseExecution(caseExecution.getId())
        .disable();
    }

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.disabled();

    verifyQueryResults(query, 1);

  }

  public void testQueryByNullVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueEquals("aNullValue", null);

    verifyQueryResults(query, 1);
  }

  public void testQueryByStringVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueEquals("aStringValue", "abc");

    verifyQueryResults(query, 1);
  }

  public void testQueryByBooleanVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueEquals("aBooleanValue", true);

    verifyQueryResults(query, 1);
  }

  public void testQueryByShortVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueEquals("aShortValue", (short) 123);

    verifyQueryResults(query, 1);
  }

  public void testQueryByIntegerVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueEquals("anIntegerValue", 456);

    verifyQueryResults(query, 1);
  }

  public void testQueryByLongVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueEquals("aLongValue", (long) 789);

    verifyQueryResults(query, 1);
  }

  public void testQueryByDateVariableValueEquals() {
    Date now = new Date();
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueEquals("aDateValue", now);

    verifyQueryResults(query, 1);
  }

  public void testQueryByDoubleVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueEquals("aDoubleValue", 1.5);

    verifyQueryResults(query, 1);
  }

  public void testQueryByByteArrayVariableValueEquals() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueNotEquals("aStringValue", "abd");

    verifyQueryResults(query, 1);
  }

  public void testQueryByBooleanVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueNotEquals("aBooleanValue", false);

    verifyQueryResults(query, 1);
  }

  public void testQueryByShortVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueNotEquals("aShortValue", (short) 124);

    verifyQueryResults(query, 1);
  }

  public void testQueryByIntegerVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueNotEquals("anIntegerValue", 457);

    verifyQueryResults(query, 1);
  }

  public void testQueryByLongVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueNotEquals("aDateValue", before);

    verifyQueryResults(query, 1);
  }

  public void testQueryByDoubleVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueNotEquals("aDoubleValue", 1.6);

    verifyQueryResults(query, 1);
  }

  public void testQueryByByteArrayVariableValueNotEquals() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.variableValueGreaterThan("aNullValue", null).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByStringVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThan("aStringValue", "ab");

    verifyQueryResults(query, 1);

  }

  public void testQueryByBooleanVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.variableValueGreaterThan("aBooleanValue", false).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByShortVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThan("aShortValue", (short) 122);

    verifyQueryResults(query, 1);

  }

  public void testQueryByIntegerVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThan("anIntegerValue", 455);

    verifyQueryResults(query, 1);

  }

  public void testQueryByLongVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThan("aLongValue", (long) 788);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDateVariableValueGreaterThan() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    Date before = new Date(now.getTime() - 100000);

    query.variableValueGreaterThan("aDateValue", before);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDoubleVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThan("aDoubleValue", 1.4);

    verifyQueryResults(query, 1);

  }

  public void testQueryByByteArrayVariableValueGreaterThan() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.variableValueGreaterThanOrEqual("aNullValue", null).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByStringVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("aStringValue", "ab");

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("aStringValue", "abc");

    verifyQueryResults(query, 1);

  }

  public void testQueryByBooleanVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.variableValueGreaterThanOrEqual("aBooleanValue", false).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByShortVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("aShortValue", (short) 122);

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("aShortValue", (short) 123);

    verifyQueryResults(query, 1);

  }

  public void testQueryByIntegerVariableValueGreaterThanOrEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("anIntegerValue", 455);

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("anIntegerValue", 456);

    verifyQueryResults(query, 1);

  }

  public void testQueryByLongVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("aLongValue", (long) 788);

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("aLongValue", (long) 789);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDateVariableValueGreaterThanOrEqual() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    Date before = new Date(now.getTime() - 100000);

    query.variableValueGreaterThanOrEqual("aDateValue", before);

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("aDateValue", now);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDoubleVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("aDoubleValue", 1.4);

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueGreaterThanOrEqual("aDoubleValue", 1.5);

    verifyQueryResults(query, 1);

  }

  public void testQueryByByteArrayVariableValueGreaterThanOrEqual() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.variableValueLessThan("aNullValue", null).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByStringVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLessThan("aStringValue", "abd");

    verifyQueryResults(query, 1);

  }

  public void testQueryByBooleanVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.variableValueLessThan("aBooleanValue", false).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByShortVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLessThan("aShortValue", (short) 124);

    verifyQueryResults(query, 1);

  }

  public void testQueryByIntegerVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLessThan("anIntegerValue", 457);

    verifyQueryResults(query, 1);

  }

  public void testQueryByLongVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLessThan("aLongValue", (long) 790);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDateVariableValueLessThan() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    Date after = new Date(now.getTime() + 100000);

    query.variableValueLessThan("aDateValue", after);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDoubleVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLessThan("aDoubleValue", 1.6);

    verifyQueryResults(query, 1);

  }

  public void testQueryByByteArrayVariableValueLessThan() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.variableValueLessThanOrEqual("aNullValue", null).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByStringVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("aStringValue", "abd");

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("aStringValue", "abc");

    verifyQueryResults(query, 1);

  }

  public void testQueryByBooleanVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.variableValueLessThanOrEqual("aBooleanValue", false).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByShortVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("aShortValue", (short) 124);

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("aShortValue", (short) 123);

    verifyQueryResults(query, 1);

  }

  public void testQueryByIntegerVariableValueLessThanOrEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("anIntegerValue", 457);

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("anIntegerValue", 456);

    verifyQueryResults(query, 1);

  }

  public void testQueryByLongVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("aLongValue", (long) 790);

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("aLongValue", (long) 789);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDateVariableValueLessThanOrEqual() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    Date after = new Date(now.getTime() + 100000);

    query.variableValueLessThanOrEqual("aDateValue", after);

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("aDateValue", now);

    verifyQueryResults(query, 1);

  }

  public void testQueryByDoubleVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("aDoubleValue", 1.6);

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueLessThanOrEqual("aDoubleValue", 1.5);

    verifyQueryResults(query, 1);

  }

  public void testQueryByByteArrayVariableValueLessThanOrEqual() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

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

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.variableValueLike("aNullValue", null).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByStringVariableValueLike() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.variableValueLike("aStringValue", "ab%");

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueLike("aStringValue", "%bc");

    verifyQueryResults(query, 1);

    query = caseService.createCaseExecutionQuery();

    query.variableValueLike("aStringValue", "%b%");

    verifyQueryResults(query, 1);
  }

  public void testQueryByNullCaseInstanceVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueEquals("aNullValue", null);

    verifyQueryResults(query, 2);
  }

  public void testQueryByStringCaseInstanceVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueEquals("aStringValue", "abc");

    verifyQueryResults(query, 2);
  }

  public void testQueryByBooleanCaseInstanceVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueEquals("aBooleanValue", true);

    verifyQueryResults(query, 2);
  }

  public void testQueryByShortCaseInstanceVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueEquals("aShortValue", (short) 123);

    verifyQueryResults(query, 2);
  }

  public void testQueryByIntegerCaseInstanceVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueEquals("anIntegerValue", 456);

    verifyQueryResults(query, 2);
  }

  public void testQueryByLongCaseInstanceVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueEquals("aLongValue", (long) 789);

    verifyQueryResults(query, 2);
  }

  public void testQueryByDateCaseInstanceVariableValueEquals() {
    Date now = new Date();
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueEquals("aDateValue", now);

    verifyQueryResults(query, 2);
  }

  public void testQueryByDoubleCaseInstanceVariableValueEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueEquals("aDoubleValue", 1.5);

    verifyQueryResults(query, 2);
  }

  public void testQueryByByteArrayCaseInstanceVariableValueEquals() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueEquals("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableCaseInstanceVariableValueEquals() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueEquals("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByStringCaseInstanceVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueNotEquals("aStringValue", "abd");

    verifyQueryResults(query, 2);
  }

  public void testQueryByBooleanCaseInstanceVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueNotEquals("aBooleanValue", false);

    verifyQueryResults(query, 2);
  }

  public void testQueryByShortCaseInstanceVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueNotEquals("aShortValue", (short) 124);

    verifyQueryResults(query, 2);
  }

  public void testQueryByIntegerCaseInstanceVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueNotEquals("anIntegerValue", 457);

    verifyQueryResults(query, 2);
  }

  public void testQueryByLongCaseInstanceVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueNotEquals("aLongValue", (long) 790);

    verifyQueryResults(query, 2);
  }

  public void testQueryByDateCaseInstanceVariableValueNotEquals() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    Date before = new Date(now.getTime() - 100000);

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueNotEquals("aDateValue", before);

    verifyQueryResults(query, 2);
  }

  public void testQueryByDoubleCaseInstanceVariableValueNotEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueNotEquals("aDoubleValue", 1.6);

    verifyQueryResults(query, 2);
  }

  public void testQueryByByteArrayCaseInstanceVariableValueNotEquals() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueNotEquals("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableCaseInstanceVariableValueNotEquals() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueNotEquals("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNullCaseInstanceVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueGreaterThan("aNullValue", null).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByStringCaseInstanceVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThan("aStringValue", "ab");

    verifyQueryResults(query, 2);

  }

  public void testQueryByBooleanCaseInstanceVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueGreaterThan("aBooleanValue", false).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByShortCaseInstanceVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThan("aShortValue", (short) 122);

    verifyQueryResults(query, 2);

  }

  public void testQueryByIntegerCaseInstanceVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThan("anIntegerValue", 455);

    verifyQueryResults(query, 2);

  }

  public void testQueryByLongCaseInstanceVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThan("aLongValue", (long) 788);

    verifyQueryResults(query, 2);

  }

  public void testQueryByDateCaseInstanceVariableValueGreaterThan() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    Date before = new Date(now.getTime() - 100000);

    query.caseInstanceVariableValueGreaterThan("aDateValue", before);

    verifyQueryResults(query, 2);

  }

  public void testQueryByDoubleCaseInstanceVariableValueGreaterThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThan("aDoubleValue", 1.4);

    verifyQueryResults(query, 2);

  }

  public void testQueryByByteArrayCaseInstanceVariableValueGreaterThan() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueGreaterThan("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableCaseInstanceVariableGreaterThan() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueGreaterThan("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNullCaseInstanceVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueGreaterThanOrEqual("aNullValue", null).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByStringCaseInstanceVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("aStringValue", "ab");

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("aStringValue", "abc");

    verifyQueryResults(query, 2);

  }

  public void testQueryByBooleanCaseInstanceVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueGreaterThanOrEqual("aBooleanValue", false).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByShortCaseInstanceVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("aShortValue", (short) 122);

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("aShortValue", (short) 123);

    verifyQueryResults(query, 2);

  }

  public void testQueryByIntegerCaseInstanceVariableValueGreaterThanOrEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("anIntegerValue", 455);

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("anIntegerValue", 456);

    verifyQueryResults(query, 2);

  }

  public void testQueryByLongCaseInstanceVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("aLongValue", (long) 788);

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("aLongValue", (long) 789);

    verifyQueryResults(query, 2);

  }

  public void testQueryByDateCaseInstanceVariableValueGreaterThanOrEqual() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    Date before = new Date(now.getTime() - 100000);

    query.caseInstanceVariableValueGreaterThanOrEqual("aDateValue", before);

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("aDateValue", now);

    verifyQueryResults(query, 2);

  }

  public void testQueryByDoubleCaseInstanceVariableValueGreaterThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("aDoubleValue", 1.4);

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueGreaterThanOrEqual("aDoubleValue", 1.5);

    verifyQueryResults(query, 2);

  }

  public void testQueryByByteArrayCaseInstanceVariableValueGreaterThanOrEqual() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueGreaterThanOrEqual("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableCaseInstanceVariableGreaterThanOrEqual() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueGreaterThanOrEqual("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNullCaseInstanceVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueLessThan("aNullValue", null).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByStringCaseInstanceVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThan("aStringValue", "abd");

    verifyQueryResults(query, 2);

  }

  public void testQueryByBooleanCaseInstanceVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueLessThan("aBooleanValue", false).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByShortCaseInstanceVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThan("aShortValue", (short) 124);

    verifyQueryResults(query, 2);

  }

  public void testQueryByIntegerCaseInstanceVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThan("anIntegerValue", 457);

    verifyQueryResults(query, 2);

  }

  public void testQueryByLongCaseInstanceVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThan("aLongValue", (long) 790);

    verifyQueryResults(query, 2);

  }

  public void testQueryByDateCaseInstanceVariableValueLessThan() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    Date after = new Date(now.getTime() + 100000);

    query.caseInstanceVariableValueLessThan("aDateValue", after);

    verifyQueryResults(query, 2);

  }

  public void testQueryByDoubleCaseInstanceVariableValueLessThan() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThan("aDoubleValue", 1.6);

    verifyQueryResults(query, 2);

  }

  public void testQueryByByteArrayCaseInstanceVariableValueLessThan() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueLessThan("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableCaseInstanceVariableLessThan() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueLessThan("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNullCaseInstanceVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueLessThanOrEqual("aNullValue", null).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByStringCaseInstanceVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("aStringValue", "abd");

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("aStringValue", "abc");

    verifyQueryResults(query, 2);

  }

  public void testQueryByBooleanCaseInstanceVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aBooleanValue", true)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueLessThanOrEqual("aBooleanValue", false).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByShortCaseInstanceVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aShortValue", (short) 123)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("aShortValue", (short) 124);

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("aShortValue", (short) 123);

    verifyQueryResults(query, 2);

  }

  public void testQueryByIntegerCaseInstanceVariableValueLessThanOrEquals() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("anIntegerValue", 456)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("anIntegerValue", 457);

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("anIntegerValue", 456);

    verifyQueryResults(query, 2);

  }

  public void testQueryByLongCaseInstanceVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aLongValue", (long) 789)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("aLongValue", (long) 790);

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("aLongValue", (long) 789);

    verifyQueryResults(query, 2);

  }

  public void testQueryByDateCaseInstanceVariableValueLessThanOrEqual() {
    Date now = new Date();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDateValue", now)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    Date after = new Date(now.getTime() + 100000);

    query.caseInstanceVariableValueLessThanOrEqual("aDateValue", after);

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("aDateValue", now);

    verifyQueryResults(query, 2);

  }

  public void testQueryByDoubleCaseInstanceVariableValueLessThanOrEqual() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aDoubleValue", 1.5)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("aDoubleValue", 1.6);

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLessThanOrEqual("aDoubleValue", 1.5);

    verifyQueryResults(query, 2);

  }

  public void testQueryByByteArrayCaseInstanceVariableValueLessThanOrEqual() {
    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aByteArrayValue", bytes)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueLessThanOrEqual("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryBySerializableCaseInstanceVariableLessThanOrEqual() {
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aSerializableValue", serializable)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueLessThanOrEqual("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNullCaseInstanceVariableValueLike() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aNullValue", null)
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    try {
      query.caseInstanceVariableValueLike("aNullValue", null).list();
      fail();
    } catch (NotValidException e) {}

  }

  public void testQueryByStringCaseInstanceVariableValueLike() {
    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .setVariable("aStringValue", "abc")
      .create();

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLike("aStringValue", "ab%");

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLike("aStringValue", "%bc");

    verifyQueryResults(query, 2);

    query = caseService.createCaseExecutionQuery();

    query.caseInstanceVariableValueLike("aStringValue", "%b%");

    verifyQueryResults(query, 2);
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

    assertEquals(4, caseService.createCaseExecutionQuery().variableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, caseService.createCaseExecutionQuery().variableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, caseService.createCaseExecutionQuery().variableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, caseService.createCaseExecutionQuery().variableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, caseService.createCaseExecutionQuery().variableValueEquals("var", Variables.numberValue(null)).count());

    // other operators
    assertEquals(4, caseService.createCaseExecutionQuery().variableValueNotEquals("var", Variables.numberValue(123)).count());
    assertEquals(1, caseService.createCaseExecutionQuery().variableValueGreaterThan("var", Variables.numberValue(123L)).count());
    assertEquals(5, caseService.createCaseExecutionQuery().variableValueGreaterThanOrEqual("var", Variables.numberValue(123.0d)).count());
    assertEquals(0, caseService.createCaseExecutionQuery().variableValueLessThan("var", Variables.numberValue((short) 123)).count());
    assertEquals(4, caseService.createCaseExecutionQuery().variableValueLessThanOrEqual("var", Variables.numberValue((short) 123)).count());

    // two executions per case instance match the query
    assertEquals(8, caseService.createCaseExecutionQuery().caseInstanceVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(8, caseService.createCaseExecutionQuery().caseInstanceVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(8, caseService.createCaseExecutionQuery().caseInstanceVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(8, caseService.createCaseExecutionQuery().caseInstanceVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(2, caseService.createCaseExecutionQuery().caseInstanceVariableValueEquals("var", Variables.numberValue(null)).count());

    // other operators
    assertEquals(8, caseService.createCaseExecutionQuery().caseInstanceVariableValueNotEquals("var", Variables.numberValue(123)).count());
    assertEquals(2, caseService.createCaseExecutionQuery().caseInstanceVariableValueGreaterThan("var", Variables.numberValue(123L)).count());
    assertEquals(10, caseService.createCaseExecutionQuery().caseInstanceVariableValueGreaterThanOrEqual("var", Variables.numberValue(123.0d)).count());
    assertEquals(0, caseService.createCaseExecutionQuery().caseInstanceVariableValueLessThan("var", Variables.numberValue((short) 123)).count());
    assertEquals(8, caseService.createCaseExecutionQuery().caseInstanceVariableValueLessThanOrEqual("var", Variables.numberValue((short) 123)).count());

  }


  public void testQuerySorting() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    // asc
    query
      .orderByCaseDefinitionId()
      .asc();
    verifyQueryWithOrdering(query, 11, caseExecutionByDefinitionId());

    query = caseService.createCaseExecutionQuery();

    query
      .orderByCaseDefinitionKey()
      .asc();
    verifyQueryWithOrdering(query, 11, caseExecutionByDefinitionKey(processEngine));

    query = caseService.createCaseExecutionQuery();

    query
      .orderByCaseExecutionId()
      .asc();
    verifyQueryWithOrdering(query, 11, caseExecutionById());


    // desc

    query = caseService.createCaseExecutionQuery();

    query
      .orderByCaseDefinitionId()
      .desc();
    verifyQueryWithOrdering(query, 11, inverted(caseExecutionByDefinitionId()));

    query = caseService.createCaseExecutionQuery();

    query
      .orderByCaseDefinitionKey()
      .desc();
    verifyQueryWithOrdering(query, 11, inverted(caseExecutionByDefinitionKey(processEngine)));

    query = caseService.createCaseExecutionQuery();

    query
      .orderByCaseExecutionId()
      .desc();
    verifyQueryWithOrdering(query, 11, inverted(caseExecutionById()));

    query = caseService.createCaseExecutionQuery();

  }

  public void testCaseExecutionProperties() {
    // given
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .singleResult()
        .getId();

    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    // when
    CaseExecution task  = caseService
        .createCaseExecutionQuery()
        .caseInstanceId(caseInstanceId)
        .activityId("PI_HumanTask_1")
        .singleResult();

    // then
    assertEquals("PI_HumanTask_1", task.getActivityId());
    assertEquals("A HumanTask", task.getActivityName());
    assertEquals(caseDefinitionId, task.getCaseDefinitionId());
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    assertEquals(caseInstanceId, task.getParentId());
    assertEquals("humanTask", task.getActivityType());
    assertNotNull(task.getActivityDescription());
    assertNotNull(task.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/required/RequiredRuleTest.testVariableBasedRule.cmmn")
  public void testQueryByRequired() {
    caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("required", true));

    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .required();

    verifyQueryResults(query, 1);

    CaseExecution execution = query.singleResult();
    assertNotNull(execution);
    assertTrue(execution.isRequired());
  }

  public void testNullBusinessKeyForChildExecutions() {
    caseService.createCaseInstanceByKey(CASE_DEFINITION_KEY, "7890");
    List<CaseExecution> executions = caseService.createCaseExecutionQuery().caseInstanceBusinessKey("7890").list();
    for (CaseExecution e : executions) {
      if (((CaseExecutionEntity) e).isCaseInstanceExecution()) {
        assertEquals("7890", ((CaseExecutionEntity) e).getBusinessKeyWithoutCascade());
      } else {
        assertNull(((CaseExecutionEntity) e).getBusinessKeyWithoutCascade());
      }
    }
  }

}
