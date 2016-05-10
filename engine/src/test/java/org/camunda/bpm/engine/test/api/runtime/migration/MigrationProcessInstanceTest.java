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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MigrationProcessInstanceTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  protected RuntimeService runtimeService;

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
  }

  @Test
  public void testNullMigrationPlan() {
    try {
      runtimeService.newMigration(null).processInstanceIds(Collections.<String>emptyList()).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("migration plan is null"));
    }
  }

  @Test
  public void testNullProcessInstanceIds() {
    ProcessDefinition testProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceIds(null).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("process instance ids is empty"));
    }
  }

  @Test
  public void testEmptyProcessInstanceIds() {
    ProcessDefinition testProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceIds(Collections.<String>emptyList()).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("process instance ids is empty"));
    }
  }

  @Test
  public void testNotMigrateProcessInstanceOfWrongProcessDefinition() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition wrongProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(wrongProcessDefinition.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceIds(Collections.singletonList(processInstance.getId())).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.startsWith("ENGINE-23002"));
    }
  }

  @Test
  public void testNotMigrateUnknownProcessInstance() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceIds(Collections.singletonList("unknown")).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.startsWith("ENGINE-23003"));
    }
  }

  @Test
  public void testNotMigrateNullProcessInstance() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceIds(Collections.<String>singletonList(null)).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("process instance id is null"));
    }
  }

  @Test
  public void testMigrateProcessInstanceQuery() {
    int processInstanceCount = 10;

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    for(int i = 0; i < processInstanceCount; i++) {
      runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    }

    ProcessInstanceQuery sourceProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(sourceProcessDefinition.getId());
    ProcessInstanceQuery targetProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(targetProcessDefinition.getId());

    assertEquals(processInstanceCount, sourceProcessInstanceQuery.count());
    assertEquals(0, targetProcessInstanceQuery.count());


    runtimeService.newMigration(migrationPlan)
      .processInstanceQuery(sourceProcessInstanceQuery)
      .execute();

    assertEquals(0, sourceProcessInstanceQuery.count());
    assertEquals(processInstanceCount, targetProcessInstanceQuery.count());
  }

  @Test
  public void testNullProcessInstanceQuery() {
    ProcessDefinition testProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceQuery(null).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("process instance ids is empty"));
    }
  }

  @Test
  public void testEmptyProcessInstanceQuery() {
    ProcessDefinition testProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstanceQuery emptyProcessInstanceQuery = runtimeService.createProcessInstanceQuery();
    assertEquals(0, emptyProcessInstanceQuery.count());

    try {
      runtimeService.newMigration(migrationPlan).processInstanceQuery(emptyProcessInstanceQuery).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("process instance ids is empty"));
    }
  }

  @Test
  public void testProcessInstanceQueryOfWrongProcessDefinition() {
    ProcessDefinition testProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition wrongProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    runtimeService.startProcessInstanceById(wrongProcessDefinition.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstanceQuery wrongProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(wrongProcessDefinition.getId());
    assertEquals(1, wrongProcessInstanceQuery.count());

    try {
      runtimeService.newMigration(migrationPlan).processInstanceQuery(wrongProcessInstanceQuery).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.startsWith("ENGINE-23002"));
    }
  }

  @Test
  public void testProcessInstanceIdsAndQuery() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    ProcessInstanceQuery sourceProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance2.getId());
    ProcessInstanceQuery targetProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(targetProcessDefinition.getId());

    assertEquals(0, targetProcessInstanceQuery.count());

    runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Collections.singletonList(processInstance1.getId()))
      .processInstanceQuery(sourceProcessInstanceQuery)
      .execute();

    assertEquals(2, targetProcessInstanceQuery.count());
  }

  @Test
  public void testOverlappingProcessInstanceIdsAndQuery() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    ProcessInstanceQuery sourceProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(sourceProcessDefinition.getId());
    ProcessInstanceQuery targetProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(targetProcessDefinition.getId());

    assertEquals(0, targetProcessInstanceQuery.count());

    runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance1.getId(), processInstance2.getId()))
      .processInstanceQuery(sourceProcessInstanceQuery)
      .execute();

    assertEquals(2, targetProcessInstanceQuery.count());
  }

}
