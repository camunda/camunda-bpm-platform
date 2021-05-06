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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels.USER_TASK_ID;
import static org.junit.Assert.assertNotNull;

public class SetVariablesMigrationTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @After
  public void clearAuthentication() {
    rule.getIdentityService().clearAuthentication();
  }

  @After
  public void resetEngineConfig() {
    rule.getProcessEngineConfiguration()
        .setRestrictUserOperationLogToAuthenticatedUsers(true);
  }

  @Test
  public void shouldSetVariable() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(Collections.singletonMap("foo", "bar"))
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getVariables()).isEmpty();
    assertThat(testHelper.snapshotAfterMigration.getVariables())
        .extracting("name", "value", "executionId")
        .containsExactly(tuple("foo", "bar", processInstance.getId()));
  }

  @Test
  public void shouldSetVariables() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    Map<String, Object> variables = new HashMap<>();
    variables.put("foo", "bar");
    variables.put("bar", 5);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(variables)
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getVariables()).isEmpty();
    assertThat(testHelper.snapshotAfterMigration.getVariables())
        .extracting("name", "value", "executionId")
        .containsExactlyInAnyOrder(
            tuple("foo", "bar", processInstance.getId()),
            tuple("bar", 5, processInstance.getId())
        );
  }

  @Test
  public void shouldSetUntypedVariable() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(Variables.putValue("foo", "bar"))
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getVariables()).isEmpty();
    assertThat(testHelper.snapshotAfterMigration.getVariables())
        .extracting("name", "value", "executionId")
        .containsExactly(tuple("foo", "bar", processInstance.getId()));
  }

  @Test
  public void shouldSetUntypedVariables() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(
            Variables.putValue("foo", "bar")
                .putValue("bar", 5)
        )
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getVariables()).isEmpty();
    assertThat(testHelper.snapshotAfterMigration.getVariables())
        .extracting("name", "value", "executionId")
        .containsExactlyInAnyOrder(
            tuple("foo", "bar", processInstance.getId()),
            tuple("bar", 5, processInstance.getId())
        );
  }

  @Test
  public void shouldSetMapOfTypedVariable() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(Collections.singletonMap("foo", Variables.shortValue((short)5)))
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getVariables()).isEmpty();
    assertThat(testHelper.snapshotAfterMigration.getVariables())
        .extracting("name", "value", "executionId")
        .containsExactly(tuple("foo", (short)5, processInstance.getId()));
  }

  @Test
  public void shouldSetVariableMapOfTypedVariable() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(
            Variables.putValueTyped("foo", Variables.stringValue("bar"))
        )
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getVariables()).isEmpty();
    assertThat(testHelper.snapshotAfterMigration.getVariables())
        .extracting("name", "value", "executionId")
        .containsExactly(tuple("foo", "bar", processInstance.getId()));
  }

  @Test
  public void shouldSetTypedAndUntypedVariables() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(
            Variables.putValue("foo", "bar")
                .putValueTyped("bar", Variables.integerValue(5))
        )
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getVariables()).isEmpty();
    assertThat(testHelper.snapshotAfterMigration.getVariables())
        .extracting("name", "value", "executionId")
        .containsExactlyInAnyOrder(
            tuple("foo", "bar", processInstance.getId()),
            tuple("bar", 5, processInstance.getId())
        );
  }

  @Test
  public void shouldSetNullVariables() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(null)
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getVariables()).isEmpty();
    assertThat(testHelper.snapshotAfterMigration.getVariables()).isEmpty();
  }

  @Test
  public void shouldSetEmptyVariables() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(new HashMap<>())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getVariables()).isEmpty();
    assertThat(testHelper.snapshotAfterMigration.getVariables()).isEmpty();
  }

  @Test
  public void shouldSetTransientVariable() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(
        modify(ProcessModels.ONE_TASK_PROCESS)
            .activityBuilder(USER_TASK_ID)
              .camundaExecutionListenerClass("end", ReadTransientVariableExecutionListener.class)
            .done()
        );
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(Variables.putValueTyped("foo", Variables.stringValue("bar", true)))
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getVariables()).isEmpty();
    assertThat(testHelper.snapshotAfterMigration.getVariables()).isEmpty();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldWriteOperationLog() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(
            Variables.putValue("foo", "bar")
                .putValueTyped("bar", Variables.integerValue(5))
        )
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    rule.getIdentityService().setAuthenticatedUserId("user");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<UserOperationLogEntry> operationLogEntries = rule.getHistoryService()
        .createUserOperationLogQuery()
        .list();

    assertThat(operationLogEntries)
        .extracting("operationType", "userId", "property", "newValue")
        .containsExactlyInAnyOrder(
            tuple("Migrate", "user", "processDefinitionId", targetProcessDefinition.getId()),
            tuple("Migrate", "user", "nrOfInstances", "1"),
            tuple("Migrate", "user", "nrOfSetVariables", "2"),
            tuple("Migrate", "user", "async", "false")
        );
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldWriteOperationLogUnauthenticated() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(
            Variables.putValue("foo", "bar")
                .putValueTyped("bar", Variables.integerValue(5))
        )
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    rule.getProcessEngineConfiguration().setRestrictUserOperationLogToAuthenticatedUsers(false);

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<UserOperationLogEntry> operationLogEntries = rule.getHistoryService()
        .createUserOperationLogQuery()
        .list();

    assertThat(operationLogEntries)
        .extracting("operationType", "userId", "property", "newValue")
        .containsExactlyInAnyOrder(
            tuple("Migrate", null, "processDefinitionId", targetProcessDefinition.getId()),
            tuple("Migrate", null, "nrOfInstances", "1"),
            tuple("Migrate", null, "nrOfSetVariables", "2"),
            tuple("Migrate", null, "async", "false")
        );
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldWriteOperationLogForEmptyVariables() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(new HashMap<>())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    rule.getIdentityService().setAuthenticatedUserId("user");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<UserOperationLogEntry> operationLogEntries = rule.getHistoryService()
        .createUserOperationLogQuery()
        .list();

    assertThat(operationLogEntries)
        .extracting("operationType", "userId", "property", "newValue")
        .containsExactlyInAnyOrder(
            tuple("Migrate", "user", "processDefinitionId", targetProcessDefinition.getId()),
            tuple("Migrate", "user", "nrOfInstances", "1"),
            tuple("Migrate", "user", "nrOfSetVariables", "0"),
            tuple("Migrate", "user", "async", "false")
        );
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldNotWriteOperationLogForVariablesNull() {
    // given
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId());

    rule.getIdentityService().setAuthenticatedUserId("user");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<UserOperationLogEntry> operationLogEntries = rule.getHistoryService()
        .createUserOperationLogQuery()
        .list();

    assertThat(operationLogEntries)
        .extracting("operationType", "userId", "property", "newValue")
        .containsExactlyInAnyOrder(
            tuple("Migrate", "user", "processDefinitionId", targetProcessDefinition.getId()),
            tuple("Migrate", "user", "nrOfInstances", "1"),
            tuple("Migrate", "user", "async", "false")
        );
  }

  // helper ////////////////////////////////////////////////////////////////////////////////////////

  public static class ReadTransientVariableExecutionListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
      Object variable = execution.getVariable("foo");
      assertNotNull(variable);
    }
  }

}
