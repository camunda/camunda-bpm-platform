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

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.MigratingProcessInstanceValidationReportAssert.assertThat;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MigrationProcessInstanceTest {

  protected ProcessEngineRule rule = new ProcessEngineRule(true);
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
      runtimeService.executeMigrationPlan(null).processInstanceIds(Collections.<String>emptyList()).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("migration plan is null"));
    }
  }

  @Test
  public void testNullProcessInstanceIds() {
    ProcessDefinition testProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.executeMigrationPlan(migrationPlan).processInstanceIds(null).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("process instance ids is null"));
    }
  }

  @Test
  public void testNotMigrateProcessInstanceOfWrongProcessDefinition() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition wrongProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(wrongProcessDefinition.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.executeMigrationPlan(migrationPlan).processInstanceIds(Collections.singletonList(processInstance.getId())).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.startsWith("ENGINE-23002"));
    }
  }

  @Test
  public void testNotMigrateUnknownProcessInstance() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.executeMigrationPlan(migrationPlan).processInstanceIds(Collections.singletonList("unknown")).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.startsWith("ENGINE-23003"));
    }
  }

  @Test
  public void testNotMigrateNullProcessInstance() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.executeMigrationPlan(migrationPlan).processInstanceIds(Collections.<String>singletonList(null)).execute();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("process instance id is null"));
    }
  }

  @Test
  public void testNotMigrateProcessInstanceWithAsyncTransition() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .userTaskBuilder("userTask")
        .camundaAsyncBefore()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      testHelper.createProcessInstanceAndMigrate(migrationPlan);
      fail("Should not be able to migrate");
    }
    catch (MigratingProcessInstanceValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures("Process instance contains not migrated jobs")
        .hasActivityInstanceFailures(sourceProcessDefinition.getId(), "Has active asynchronous child transitions");
    }
  }

  @Test
  public void testNotMigrateProcessInstanceWithNestedAsyncTransition() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.TRIPLE_SUBPROCESS_PROCESS)
      .userTaskBuilder("userTask")
      .camundaAsyncBefore()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      testHelper.createProcessInstanceAndMigrate(migrationPlan);
      fail("Should not be able to migrate");
    }
    catch (MigratingProcessInstanceValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures("Process instance contains not migrated jobs")
        .hasActivityInstanceFailures("subProcess3", "Has active asynchronous child transitions");
    }
  }

}
