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
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MigrationTimerBoundryEventTest {

  private static final String DUE_DATE_IN_THE_PAST = "2018-02-11T12:13:14Z";
  protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);
  protected ManagementService managementService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void init() {
    managementService = rule.getManagementService();
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
  }

  @After
  public void cleanUpJobs() {
    List<Job> jobs = managementService.createJobQuery().list();
    if (!jobs.isEmpty()) {
      for (Job job : jobs) {
        managementService.deleteJob(job.getId());
      }
    }
  }

  @Test
  public void testMigrationNonInterruptingTimerEvent() {
    // given
    BpmnModelInstance model = createModel(false, DUE_DATE_IN_THE_PAST);
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(model);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(model);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<Job> list = managementService.createJobQuery().list();
    assertTrue(list.isEmpty());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("afterTimer").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("userTask").count());
  }

  @Test
  public void testMigrationInterruptingTimerEvent() {
    // given
    BpmnModelInstance model = createModel(true, DUE_DATE_IN_THE_PAST);
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(model);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(model);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<Job> list = managementService.createJobQuery().list();
    assertTrue(list.isEmpty());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("afterTimer").count());
    assertEquals(0, taskService.createTaskQuery().taskDefinitionKey("userTask").count());
  }

  @Test
  public void testMigrationNonTriggeredInterruptingTimerEvent() {
    // given
    Date futureDueDate = DateUtils.addYears(ClockUtil.getCurrentTime(), 1);
    BpmnModelInstance model = createModel(true, sdf.format(futureDueDate));
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(model);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(model);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<Job> list = managementService.createJobQuery().list();
    assertEquals(1, list.size());
    assertEquals(0, taskService.createTaskQuery().taskDefinitionKey("afterTimer").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("userTask").count());
  }

  @Test
  public void testMigrationTwoNonInterruptingTimerEvents() {
    // given
    Date futureDueDate = DateUtils.addYears(ClockUtil.getCurrentTime(), 1);
    BpmnModelInstance model = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
        .boundaryEvent("timerPast")
          .cancelActivity(false)
          .timerWithDate(DUE_DATE_IN_THE_PAST)
        .userTask("past")
        .moveToActivity("userTask")
          .boundaryEvent("timerFuture")
          .cancelActivity(false)
          .timerWithDate(sdf.format(futureDueDate))
        .userTask("future")
        .done();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(model);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(model);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    Job job = managementService.createJobQuery().duedateLowerThan(ClockUtil.getCurrentTime()).singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<Job> list = managementService.createJobQuery().list();
    assertEquals(1, list.size());
    assertEquals(1, managementService.createJobQuery().duedateHigherThan(ClockUtil.getCurrentTime()).count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("past").count());
    assertEquals(0, taskService.createTaskQuery().taskDefinitionKey("future").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("userTask").count());
  }

  @Test
  public void testMigrationWithTargetNonInterruptingTimerEvent() {
    // given
    BpmnModelInstance sourceModel = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
        .userTask("afterTimer")
        .endEvent("endEvent")
        .done();
    BpmnModelInstance targetModel = createModel(false, DUE_DATE_IN_THE_PAST);
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceModel);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetModel);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("userTask").count());
    assertEquals(0, taskService.createTaskQuery().taskDefinitionKey("afterTimer").count());
    assertEquals(1, managementService.createJobQuery().count());
  }

  @Test
  public void testMigrationWithSourceNonInterruptingTimerEvent() {
    // given
    BpmnModelInstance sourceModel = createModel(false, DUE_DATE_IN_THE_PAST);
    BpmnModelInstance targetModel = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
        .userTask("afterTimer")
        .endEvent("endEvent")
        .done();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceModel);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetModel);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<Job> list = managementService.createJobQuery().list();
    assertTrue(list.isEmpty());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("userTask").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("afterTimer").count());
  }

  @Test
  public void testMigrationTwoToOneNonInterruptingTimerEvents() {
    // given
    Date futureDueDate = DateUtils.addYears(ClockUtil.getCurrentTime(), 1);
    BpmnModelInstance sourceModel = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
        .boundaryEvent("timerPast")
          .cancelActivity(false)
          .timerWithDate(DUE_DATE_IN_THE_PAST)
        .userTask("past")
        .moveToActivity("userTask")
          .boundaryEvent("timerFuture")
          .cancelActivity(false)
          .timerWithDate(sdf.format(futureDueDate))
        .userTask("future")
        .done();
    BpmnModelInstance targetModel = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
          .boundaryEvent("timerFuture")
          .cancelActivity(false)
          .timerWithDate(sdf.format(futureDueDate))
        .userTask("future")
        .done();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceModel);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetModel);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Job job = managementService.createJobQuery().activityId("timerPast").singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapEqualActivities()
        .mapActivities("timerPast", "timerFuture")
        .mapActivities("past", "future")
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<Job> list = managementService.createJobQuery().duedateHigherThan(ClockUtil.getCurrentTime()).list();
    assertEquals(1, list.size());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("userTask").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("future").count());
  }

  @Test
  public void testMigrationOneToTwoNonInterruptingTimerEvents() {
    // given
    Date futureDueDate = DateUtils.addYears(ClockUtil.getCurrentTime(), 1);
    BpmnModelInstance sourceModel = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
          .boundaryEvent("timerFuture")
          .cancelActivity(false)
          .timerWithDate(sdf.format(futureDueDate))
        .userTask("future")
        .done();
    BpmnModelInstance targetModel = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
        .boundaryEvent("timerPast")
          .cancelActivity(false)
          .timerWithDate(DUE_DATE_IN_THE_PAST)
        .userTask("past")
        .moveToActivity("userTask")
          .boundaryEvent("timerFuture")
          .cancelActivity(false)
          .timerWithDate(sdf.format(futureDueDate))
        .userTask("future")
        .done();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceModel);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetModel);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    assertNull(managementService.createJobQuery().activityId("timerPast").singleResult());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapEqualActivities()
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertBoundaryTimerJobMigrated("timerFuture", "timerFuture");
    testHelper.assertBoundaryTimerJobCreated("timerPast");
  }

  @Test
  public void testMigrationNonInterruptingTimerEventDifferentActivityId() {
    // given
    BpmnModelInstance sourceModel = createModel(false, DUE_DATE_IN_THE_PAST);
    BpmnModelInstance targetModel = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
        .boundaryEvent("timer2")
          .cancelActivity(false)
          .timerWithDate(DUE_DATE_IN_THE_PAST)
        .userTask("afterTimer")
        .endEvent("endEvent")
        .done();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceModel);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetModel);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .mapActivities("timer", "timer2")
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<Job> list = managementService.createJobQuery().list();
    assertTrue(list.isEmpty());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("afterTimer").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("userTask").count());
  }

  protected BpmnModelInstance createModel(boolean isCancelActivity, String date) {
    BpmnModelInstance model = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
        .boundaryEvent("timer")
          .cancelActivity(isCancelActivity)
          .timerWithDate(date)
        .userTask("afterTimer")
        .endEvent("endEvent")
        .done();
    return model;
  }
}
