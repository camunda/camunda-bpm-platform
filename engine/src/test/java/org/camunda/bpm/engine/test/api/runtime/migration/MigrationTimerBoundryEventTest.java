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
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MigrationTimerBoundryEventTest {

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

  @Test
  public void testMigrationNonInterruptingTimerEvent() {
    // given
    BpmnModelInstance model = createModel(false, "2018-02-11T12:13:14Z");
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
  }

  @Test
  public void testMigrationInterruptingTimerEvent() {
    // given
    BpmnModelInstance model = createModel(true, "2018-02-11T12:13:14Z");
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
  }

  @Test
  public void testMigrationNonTriggeredInterruptingTimerEvent() {
    // given
    BpmnModelInstance model = createModel(true, "2999-02-11T12:13:14Z");
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
  }

  @Test
  public void testMigrationTwoInterruptingTimerEvents() {
    // given
    String resource = "org/camunda/bpm/engine/test/api/runtime/migration/MigrationTimerBoundryEventTest.twoNonInterruptingTimers.bpmn20.xml";
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(resource).getDeployedProcessDefinitions().get(0);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(resource).getDeployedProcessDefinitions().get(0);

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
    assertEquals(1, taskService.createTaskQuery().taskName("past").count());
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
    BpmnModelInstance targetModel = createModel(false, "2018-02-11T12:13:14Z");
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
    assertEquals(1, managementService.createJobQuery().count());
  }

  @Test
  public void testMigrationWithSourceNonInterruptingTimerEvent() {
    // given
    BpmnModelInstance sourceModel = createModel(false, "2018-02-11T12:13:14Z");
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
    BpmnModelInstance sourceModel = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
        .boundaryEvent("timerPast")
          .cancelActivity(false)
          .timerWithDate("2018-02-11T12:13:14Z")
        .userTask("past")
        .moveToActivity("userTask")
          .boundaryEvent("timerFuture")
          .cancelActivity(false)
          .timerWithDate("2999-02-11T12:13:14Z")
        .userTask("future")
        .done();
    BpmnModelInstance targetModel = Bpmn.createExecutableProcess()
        .startEvent("startEvent")
        .userTask("userTask").name("User Task")
          .boundaryEvent("timerFuture")
          .cancelActivity(false)
          .timerWithDate("2999-02-11T12:13:14Z")
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
    List<Job> list = managementService.createJobQuery().list();
    assertEquals(1, list.size());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("userTask").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("future").count());
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
