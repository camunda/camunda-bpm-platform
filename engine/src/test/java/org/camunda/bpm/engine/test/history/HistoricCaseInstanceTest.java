/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseInstanceEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.cmmn.CmmnTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricCaseInstanceTest extends CmmnTest {

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/emptyStageCase.cmmn"})
  @Test
  public void testCaseInstanceProperties() {
    CaseInstance caseInstance = createCaseInstance();

    HistoricCaseInstance historicInstance = queryHistoricCaseInstance(caseInstance.getId());

    // assert case instance properties are set correctly
    assertEquals(caseInstance.getId(), historicInstance.getId());
    assertEquals(caseInstance.getBusinessKey(), historicInstance.getBusinessKey());
    assertEquals(caseInstance.getCaseDefinitionId(), historicInstance.getCaseDefinitionId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/emptyStageWithManualActivationCase.cmmn"})
  @Test
  public void testCaseInstanceStates() {
    String caseInstanceId = createCaseInstance().getId();

    HistoricCaseInstance historicCaseInstance = queryHistoricCaseInstance(caseInstanceId);

    assertTrue(historicCaseInstance.isActive());
    assertCount(1, historicQuery().active());
    assertCount(1, historicQuery().notClosed());

    // start empty stage to complete case instance
    String stageExecutionId = queryCaseExecutionByActivityId("PI_Stage_1").getId();
    manualStart(stageExecutionId);

    historicCaseInstance = queryHistoricCaseInstance(caseInstanceId);
    assertTrue(historicCaseInstance.isCompleted());
    assertCount(1, historicQuery().completed());
    assertCount(1, historicQuery().notClosed());

    // reactive and terminate case instance
    reactivate(caseInstanceId);
    terminate(caseInstanceId);

    historicCaseInstance = queryHistoricCaseInstance(caseInstanceId);
    assertTrue(historicCaseInstance.isTerminated());
    assertCount(1, historicQuery().terminated());
    assertCount(1, historicQuery().notClosed());

    // reactive and suspend case instance
    reactivate(caseInstanceId);
    suspend(caseInstanceId);

    historicCaseInstance = queryHistoricCaseInstance(caseInstanceId);
    // not public API
    assertTrue(((HistoricCaseInstanceEntity) historicCaseInstance).isSuspended());
//    assertCount(1, historicQuery().suspended());
    assertCount(1, historicQuery().notClosed());

    // close case instance
    close(caseInstanceId);

    historicCaseInstance = queryHistoricCaseInstance(caseInstanceId);
    assertTrue(historicCaseInstance.isClosed());
    assertCount(1, historicQuery().closed());
    assertCount(0, historicQuery().notClosed());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/emptyStageWithManualActivationCase.cmmn"})
  @Test
  public void testHistoricCaseInstanceDates() {
    // create test dates
    long duration = 72 * 3600 * 1000;
    Date created = ClockUtil.getCurrentTime();
    Date closed = new Date(created.getTime() + duration);

    // create instance
    ClockUtil.setCurrentTime(created);
    String caseInstanceId = createCaseInstance().getId();

    terminate(caseInstanceId);

    // close instance
    ClockUtil.setCurrentTime(closed);
    close(caseInstanceId);

    HistoricCaseInstance historicCaseInstance = queryHistoricCaseInstance(caseInstanceId);

    // read historic dates ignoring milliseconds
    Date createTime = historicCaseInstance.getCreateTime();
    Date closeTime = historicCaseInstance.getCloseTime();
    Long durationInMillis = historicCaseInstance.getDurationInMillis();

    assertDateSimilar(created, createTime);
    assertDateSimilar(closed, closeTime);

    // test that duration is as expected with a maximal difference of one second
    assertTrue(durationInMillis >= duration);
    assertTrue(durationInMillis < duration + 1000);

    // test queries
    Date beforeCreate = new Date(created.getTime() - 3600 * 1000);
    Date afterClose = new Date(closed.getTime() + 3600 * 1000);

    assertCount(1, historicQuery().createdAfter(beforeCreate));
    assertCount(0, historicQuery().createdAfter(closed));

    assertCount(0, historicQuery().createdBefore(beforeCreate));
    assertCount(1, historicQuery().createdBefore(closed));

    assertCount(0, historicQuery().createdBefore(beforeCreate).createdAfter(closed));

    assertCount(1, historicQuery().closedAfter(created));
    assertCount(0, historicQuery().closedAfter(afterClose));

    assertCount(0, historicQuery().closedBefore(created));
    assertCount(1, historicQuery().closedBefore(afterClose));

    assertCount(0, historicQuery().closedBefore(created).closedAfter(afterClose));

    assertCount(1, historicQuery().closedBefore(afterClose).closedAfter(created));
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/emptyStageCase.cmmn"})
  @Test
  public void testCreateUser() {
    String userId = "test";
    identityService.setAuthenticatedUserId(userId);

    String caseInstanceId = createCaseInstance().getId();

    HistoricCaseInstance historicCaseInstance = queryHistoricCaseInstance(caseInstanceId);
    assertEquals(userId, historicCaseInstance.getCreateUserId());
    assertCount(1, historicQuery().createdBy(userId));

    identityService.setAuthenticatedUserId(null);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  @Test
  public void testSuperCaseInstance() {
    String caseInstanceId  = createCaseInstanceByKey("oneCaseTaskCase").getId();
    queryCaseExecutionByActivityId("PI_CaseTask_1").getId();

    HistoricCaseInstance historicCaseInstance = historicQuery()
      .superCaseInstanceId(caseInstanceId)
      .singleResult();

    assertNotNull(historicCaseInstance);
    assertEquals(caseInstanceId, historicCaseInstance.getSuperCaseInstanceId());

    String superCaseInstanceId = historicQuery()
      .subCaseInstanceId(historicCaseInstance.getId())
      .singleResult()
      .getId();

    assertEquals(caseInstanceId, superCaseInstanceId);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn",
    "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn",
    "org/camunda/bpm/engine/test/api/repository/three_.cmmn"
  })
  @Test
  public void testHistoricCaseInstanceQuery() {
    CaseInstance oneTaskCase = createCaseInstanceByKey("oneTaskCase", "oneBusiness");
    CaseInstance twoTaskCase = createCaseInstanceByKey("twoTaskCase", "twoBusiness");
    createCaseInstanceByKey("xyz_", "xyz_");

    assertCount(1, historicQuery().caseInstanceId(oneTaskCase.getId()));
    assertCount(1, historicQuery().caseInstanceId(twoTaskCase.getId()));

    Set<String> caseInstanceIds = new HashSet<String>();
    caseInstanceIds.add(oneTaskCase.getId());
    caseInstanceIds.add("unknown1");
    caseInstanceIds.add(twoTaskCase.getId());
    caseInstanceIds.add("unknown2");

    assertCount(2, historicQuery().caseInstanceIds(caseInstanceIds));
    assertCount(0, historicQuery().caseInstanceIds(caseInstanceIds).caseInstanceId("someOtherId"));

    assertCount(1, historicQuery().caseDefinitionId(oneTaskCase.getCaseDefinitionId()));

    assertCount(1, historicQuery().caseDefinitionKey("oneTaskCase"));

    assertCount(3, historicQuery().caseDefinitionKeyNotIn(Arrays.asList("unknown")));
    assertCount(2, historicQuery().caseDefinitionKeyNotIn(Arrays.asList("oneTaskCase")));
    assertCount(1, historicQuery().caseDefinitionKeyNotIn(Arrays.asList("oneTaskCase", "twoTaskCase")));
    assertCount(0, historicQuery().caseDefinitionKeyNotIn(Arrays.asList("oneTaskCase")).caseDefinitionKey("oneTaskCase"));


    try {
      // oracle handles empty string like null which seems to lead to undefined behavior of the LIKE comparison
      historicQuery().caseDefinitionKeyNotIn(Arrays.asList(""));
      fail("Exception expected");
    }
    catch (NotValidException e) {
      // expected
    }


    assertCount(1, historicQuery().caseDefinitionName("One Task Case"));

    assertCount(2, historicQuery().caseDefinitionNameLike("%T%"));
    assertCount(1, historicQuery().caseDefinitionNameLike("One%"));
    assertCount(0, historicQuery().caseDefinitionNameLike("%Process%"));
    assertCount(1, historicQuery().caseDefinitionNameLike("%z\\_"));

    assertCount(1, historicQuery().caseInstanceBusinessKey("oneBusiness"));

    assertCount(2, historicQuery().caseInstanceBusinessKeyLike("%Business"));
    assertCount(1, historicQuery().caseInstanceBusinessKeyLike("one%"));
    assertCount(0, historicQuery().caseInstanceBusinessKeyLike("%unknown%"));
    assertCount(1, historicQuery().caseInstanceBusinessKeyLike("%z\\_"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByVariable() {
    String caseInstanceId = createCaseInstance().getId();
    caseService.setVariable(caseInstanceId, "foo", "bar");
    caseService.setVariable(caseInstanceId, "number", 10);

    assertCount(1, historicQuery().variableValueEquals("foo", "bar"));
    assertCount(0, historicQuery().variableValueNotEquals("foo", "bar"));
    assertCount(1, historicQuery().variableValueNotEquals("foo", "lol"));
    assertCount(0, historicQuery().variableValueEquals("foo", "lol"));
    assertCount(1, historicQuery().variableValueLike("foo", "%a%"));
    assertCount(0, historicQuery().variableValueLike("foo", "%lol%"));
    assertCount(0, historicQuery().variableValueNotLike("foo", "%a%"));
    assertCount(1, historicQuery().variableValueNotLike("foo", "%lol%"));

    assertCount(1, historicQuery().variableValueEquals("number", 10));
    assertCount(0, historicQuery().variableValueNotEquals("number", 10));
    assertCount(1, historicQuery().variableValueNotEquals("number", 1));
    assertCount(1, historicQuery().variableValueGreaterThan("number", 1));
    assertCount(0, historicQuery().variableValueLessThan("number", 1));
    assertCount(1, historicQuery().variableValueGreaterThanOrEqual("number", 10));
    assertCount(0, historicQuery().variableValueLessThan("number", 10));
    assertCount(1, historicQuery().variableValueLessThan("number", 20));
    assertCount(0, historicQuery().variableValueGreaterThan("number", 20));
    assertCount(1, historicQuery().variableValueLessThanOrEqual("number", 10));
    assertCount(0, historicQuery().variableValueGreaterThan("number", 10));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
  @Test
  public void testCaseVariableValueEqualsNumber() throws Exception {
    // long
    caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("var", 123L)
      .create();

    // non-matching long
    caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("var", 12345L)
      .create();

    // short
    caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("var", (short) 123)
      .create();

    // double
    caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("var", 123.0d)
      .create();

    // integer
    caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("var", 123)
      .create();

    // untyped null (should not match)
    caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("var", null)
      .create();

    // typed null (should not match)
    caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("var", Variables.longValue(null))
      .create();

    caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("var", "123")
      .create();

    assertEquals(4, historicQuery().variableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, historicQuery().variableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, historicQuery().variableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, historicQuery().variableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, historicQuery().variableValueEquals("var", Variables.numberValue(null)).count());
  }


  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryPaging() {
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();

    assertEquals(3, historicQuery().listPage(0, 3).size());
    assertEquals(2, historicQuery().listPage(2, 2).size());
    assertEquals(1, historicQuery().listPage(3, 2).size());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn",
    "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"
  })
  @SuppressWarnings("unchecked")
  @Test
  public void testQuerySorting() {
    String oneCaseInstanceId = createCaseInstanceByKey("oneTaskCase", "oneBusinessKey").getId();
    String twoCaseInstanceId = createCaseInstanceByKey("twoTaskCase", "twoBusinessKey").getId();

    // terminate and close case instances => close time and duration is set
    terminate(oneCaseInstanceId);
    close(oneCaseInstanceId);
    // set time ahead to get different durations
    ClockUtil.setCurrentTime(DateTimeUtil.now().plusHours(1).toDate());
    terminate(twoCaseInstanceId);
    close(twoCaseInstanceId);

    HistoricCaseInstance oneCaseInstance = queryHistoricCaseInstance(oneCaseInstanceId);
    HistoricCaseInstance twoCaseInstance = queryHistoricCaseInstance(twoCaseInstanceId);

    // sort by case instance ids
    List<? extends Comparable> sortedList = Arrays.asList(oneCaseInstance.getId(), twoCaseInstance.getId());
    Collections.sort(sortedList);

    List<HistoricCaseInstance> instances = historicQuery().orderByCaseInstanceId().asc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("id").containsExactly(sortedList.get(0), sortedList.get(1));

    instances = historicQuery().orderByCaseInstanceId().desc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("id").containsExactly(sortedList.get(1), sortedList.get(0));

    // sort by case definition ids
    sortedList = Arrays.asList(oneCaseInstance.getCaseDefinitionId(), twoCaseInstance.getCaseDefinitionId());
    Collections.sort(sortedList);

    instances = historicQuery().orderByCaseDefinitionId().asc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("caseDefinitionId").containsExactly(sortedList.get(0), sortedList.get(1));

    instances = historicQuery().orderByCaseDefinitionId().desc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("caseDefinitionId").containsExactly(sortedList.get(1), sortedList.get(0));

    // sort by business keys
    sortedList = Arrays.asList(oneCaseInstance.getBusinessKey(), twoCaseInstance.getBusinessKey());
    Collections.sort(sortedList);

    instances = historicQuery().orderByCaseInstanceBusinessKey().asc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("businessKey").containsExactly(sortedList.get(0), sortedList.get(1));

    instances = historicQuery().orderByCaseInstanceBusinessKey().desc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("businessKey").containsExactly(sortedList.get(1), sortedList.get(0));

    // sort by create time
    sortedList = Arrays.asList(oneCaseInstance.getCreateTime(), twoCaseInstance.getCreateTime());
    Collections.sort(sortedList);

    instances = historicQuery().orderByCaseInstanceCreateTime().asc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("createTime").containsExactly(sortedList.get(0), sortedList.get(1));

    instances = historicQuery().orderByCaseInstanceCreateTime().desc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("createTime").containsExactly(sortedList.get(1), sortedList.get(0));

    // sort by close time
    sortedList = Arrays.asList(oneCaseInstance.getCloseTime(), twoCaseInstance.getCloseTime());
    Collections.sort(sortedList);

    instances = historicQuery().orderByCaseInstanceCloseTime().asc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("closeTime").containsExactly(sortedList.get(0), sortedList.get(1));

    instances = historicQuery().orderByCaseInstanceCloseTime().desc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("closeTime").containsExactly(sortedList.get(1), sortedList.get(0));

    // sort by duration
    sortedList = Arrays.asList(oneCaseInstance.getDurationInMillis(), twoCaseInstance.getDurationInMillis());
    Collections.sort(sortedList);

    instances = historicQuery().orderByCaseInstanceDuration().asc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("durationInMillis").containsExactly(sortedList.get(0), sortedList.get(1));

    instances = historicQuery().orderByCaseInstanceDuration().desc().list();
    assertEquals(2, instances.size());
    assertThat(instances).extracting("durationInMillis").containsExactly(sortedList.get(1), sortedList.get(0));

  }

  @Test
  public void testInvalidSorting() {
    try {
      historicQuery().asc();
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }

    try {
      historicQuery().desc();
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }

    try {
      historicQuery().orderByCaseInstanceId().count();
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testNativeQuery() {
    String id = createCaseInstance().getId();
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();

    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    String tableName = managementService.getTableName(HistoricCaseInstance.class);

    assertEquals(tablePrefix + "ACT_HI_CASEINST", tableName);
    assertEquals(tableName, managementService.getTableName(HistoricCaseInstanceEntity.class));

    assertEquals(4, historyService.createNativeHistoricCaseInstanceQuery().sql("SELECT * FROM " + tableName).list().size());
    assertEquals(4, historyService.createNativeHistoricCaseInstanceQuery().sql("SELECT count(*) FROM " + tableName).count());

    assertEquals(16, historyService.createNativeHistoricCaseInstanceQuery().sql("SELECT count(*) FROM " + tableName + " H1, " + tableName + " H2").count());

    // select with distinct
    assertEquals(4, historyService.createNativeHistoricCaseInstanceQuery().sql("SELECT DISTINCT * FROM " + tableName).list().size());

    assertEquals(1, historyService.createNativeHistoricCaseInstanceQuery().sql("SELECT count(*) FROM " + tableName + " H WHERE H.ID_ = '" + id + "'").count());
    assertEquals(1, historyService.createNativeHistoricCaseInstanceQuery().sql("SELECT * FROM " + tableName + " H WHERE H.ID_ = '" + id + "'").list().size());

    // use parameters
    assertEquals(1, historyService.createNativeHistoricCaseInstanceQuery().sql("SELECT count(*) FROM " + tableName + " H WHERE H.ID_ = #{caseInstanceId}").parameter("caseInstanceId", id).count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testNativeQueryPaging() {
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();

    String tableName = managementService.getTableName(HistoricCaseInstance.class);
    assertEquals(3, historyService.createNativeHistoricCaseInstanceQuery().sql("SELECT * FROM " + tableName).listPage(0, 3).size());
    assertEquals(2, historyService.createNativeHistoricCaseInstanceQuery().sql("SELECT * FROM " + tableName).listPage(2, 2).size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/emptyStageWithManualActivationCase.cmmn"})
  @Test
  public void testDeleteHistoricCaseInstance() {
    CaseInstance caseInstance = createCaseInstance();

    String caseInstanceId = caseInstance.getId();
    HistoricCaseInstance historicInstance = queryHistoricCaseInstance(caseInstanceId);
    assertNotNull(historicInstance);

    try {
      // should not be able to delete historic case instance cause the case instance is still running
      historyService.deleteHistoricCaseInstance(historicInstance.getId());
      fail("Exception expected");
    }
    catch (NullValueException e) {
      // expected
    }

    terminate(caseInstanceId);
    close(caseInstanceId);

    identityService.setAuthenticatedUserId("testUser");
    historyService.deleteHistoricCaseInstance(historicInstance.getId());
    identityService.clearAuthentication();

    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_FULL.getId()) {
      // a user operation log should have been created
      assertEquals(1, historyService.createUserOperationLogQuery().count());
      UserOperationLogEntry entry = historyService.createUserOperationLogQuery().singleResult();
      assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, entry.getCategory());
      assertEquals(EntityTypes.CASE_INSTANCE, entry.getEntityType());
      assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY, entry.getOperationType());
      assertEquals(caseInstanceId, entry.getCaseInstanceId());
      assertNull(entry.getProperty());
      assertNull(entry.getOrgValue());
      assertNull(entry.getNewValue());
    }

    assertCount(0, historicQuery());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/runtime/superProcessWithCaseCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  @Test
  public void testQueryBySuperProcessInstanceId() {
    String superProcessInstanceId = runtimeService.startProcessInstanceByKey("subProcessQueryTest").getId();

    HistoricCaseInstanceQuery query = historyService
        .createHistoricCaseInstanceQuery()
        .superProcessInstanceId(superProcessInstanceId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    HistoricCaseInstance subCaseInstance = query.singleResult();
    assertNotNull(subCaseInstance);
    assertEquals(superProcessInstanceId, subCaseInstance.getSuperProcessInstanceId());
    assertNull(subCaseInstance.getSuperCaseInstanceId());
  }

  @Test
  public void testQueryByInvalidSuperProcessInstanceId() {
    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery();

    query.superProcessInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testQueryBySubProcessInstanceId() {
    String superCaseInstanceId = caseService.createCaseInstanceByKey("oneProcessTaskCase").getId();

    String subProcessInstanceId = runtimeService
        .createProcessInstanceQuery()
        .superCaseInstanceId(superCaseInstanceId)
        .singleResult()
        .getId();

    HistoricCaseInstanceQuery query = historyService
        .createHistoricCaseInstanceQuery()
        .subProcessInstanceId(subProcessInstanceId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    HistoricCaseInstance caseInstance = query.singleResult();
    assertEquals(superCaseInstanceId, caseInstance.getId());
    assertNull(caseInstance.getSuperCaseInstanceId());
    assertNull(caseInstance.getSuperProcessInstanceId());
  }

  @Test
  public void testQueryByInvalidSubProcessInstanceId() {
    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery();

    query.subProcessInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  @Test
  public void testQueryBySuperCaseInstanceId() {
    String superCaseInstanceId = caseService.createCaseInstanceByKey("oneCaseTaskCase").getId();

    HistoricCaseInstanceQuery query = historyService
        .createHistoricCaseInstanceQuery()
        .superCaseInstanceId(superCaseInstanceId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    HistoricCaseInstance caseInstance = query.singleResult();
    assertEquals(superCaseInstanceId, caseInstance.getSuperCaseInstanceId());
    assertNull(caseInstance.getSuperProcessInstanceId());
  }

  @Test
  public void testQueryByInvalidSuperCaseInstanceId() {
    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery();

    query.superCaseInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
      })
  @Test
  public void testQueryBySubCaseInstanceId() {
    String superCaseInstanceId = caseService.createCaseInstanceByKey("oneCaseTaskCase").getId();

    String subCaseInstanceId = caseService
        .createCaseInstanceQuery()
        .superCaseInstanceId(superCaseInstanceId)
        .singleResult()
        .getId();

    HistoricCaseInstanceQuery query = historyService
        .createHistoricCaseInstanceQuery()
        .subCaseInstanceId(subCaseInstanceId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    HistoricCaseInstance caseInstance = query.singleResult();
    assertEquals(superCaseInstanceId, caseInstance.getId());
    assertNull(caseInstance.getSuperProcessInstanceId());
    assertNull(caseInstance.getSuperCaseInstanceId());
  }

  @Test
  public void testQueryByInvalidSubCaseInstanceId() {
    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery();

    query.subCaseInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  @Test
  public void testQueryByCaseActivityId() {

    // given
    createCaseInstanceByKey("oneTaskCase");

    // when
    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery()
      .caseActivityIdIn("PI_HumanTask_1");

    // then
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  @Test
  public void testQueryByCaseActivityIds() {

    // given
    createCaseInstanceByKey("oneCaseTaskCase");

    // when
    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery()
      .caseActivityIdIn("PI_HumanTask_1", "PI_CaseTask_1");

    // then
    assertEquals(2, query.list().size());
    assertEquals(2, query.count());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"
  })
  @Test
  public void testDistinctQueryByCaseActivityIds() {

    // given
    createCaseInstanceByKey("twoTaskCase");

    // when
    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery()
      .caseActivityIdIn("PI_HumanTask_1", "PI_HumanTask_2");

    // then
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByNonExistingCaseActivityId() {
    HistoricCaseInstanceQuery query = historyService
        .createHistoricCaseInstanceQuery()
        .caseActivityIdIn("nonExisting");

    assertEquals(0, query.count());
  }

  @Test
  public void testFailQueryByCaseActivityIdNull() {
    try {
      historyService.createHistoricCaseInstanceQuery()
        .caseActivityIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
  @Test
  public void testRetrieveCaseDefinitionKey() {

    // given
    String id = createCaseInstance("oneTaskCase").getId();

    // when
    HistoricCaseInstance caseInstance = historyService.createHistoricCaseInstanceQuery()
        .caseInstanceId(id)
        .singleResult();

    // then
    assertEquals("oneTaskCase", caseInstance.getCaseDefinitionKey());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
  @Test
  public void testRetrieveCaseDefinitionName() {

    // given
    String id = createCaseInstance("oneTaskCase").getId();

    // when
    HistoricCaseInstance caseInstance = historyService.createHistoricCaseInstanceQuery()
        .caseInstanceId(id)
        .singleResult();

    // then
    assertEquals("One Task Case", caseInstance.getCaseDefinitionName());

  }

  protected HistoricCaseInstance queryHistoricCaseInstance(String caseInstanceId) {
    HistoricCaseInstance historicCaseInstance = historicQuery()
      .caseInstanceId(caseInstanceId)
      .singleResult();
    assertNotNull(historicCaseInstance);
    return historicCaseInstance;
  }

  protected HistoricCaseInstanceQuery historicQuery() {
    return historyService.createHistoricCaseInstanceQuery();
  }

  protected void assertCount(long count, HistoricCaseInstanceQuery historicQuery) {
    assertEquals(count, historicQuery.count());
  }

  protected void assertDateSimilar(Date date1, Date date2) {
    long difference = Math.abs(date1.getTime() - date2.getTime());
    assertTrue(difference < 1000);
  }

}
