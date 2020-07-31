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
package org.camunda.bpm.engine.test.api.authorization.migration;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Parameterized.class)
public class MigrateProcessInstanceSyncTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  @Parameter
  public AuthorizationScenario scenario;

  @Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_INSTANCE, "processInstance", "userId", Permissions.READ))
        .failsDueToRequired(
          grant(Resources.PROCESS_DEFINITION, "sourceDefinitionKey", "userId", Permissions.MIGRATE_INSTANCE),
          grant(Resources.PROCESS_DEFINITION, "targetDefinitionKey", "userId", Permissions.MIGRATE_INSTANCE)),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_INSTANCE, "processInstance", "userId", Permissions.READ),
          grant(Resources.PROCESS_DEFINITION, "sourceDefinitionKey", "userId", Permissions.MIGRATE_INSTANCE))
        .failsDueToRequired(
          grant(Resources.PROCESS_DEFINITION, "sourceDefinitionKey", "userId", Permissions.MIGRATE_INSTANCE),
          grant(Resources.PROCESS_DEFINITION, "targetDefinitionKey", "userId", Permissions.MIGRATE_INSTANCE)),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_INSTANCE, "processInstance", "userId", Permissions.READ),
          grant(Resources.PROCESS_DEFINITION, "targetDefinitionKey", "userId", Permissions.MIGRATE_INSTANCE))
        .failsDueToRequired(
          grant(Resources.PROCESS_DEFINITION, "sourceDefinitionKey", "userId", Permissions.MIGRATE_INSTANCE),
          grant(Resources.PROCESS_DEFINITION, "targetDefinitionKey", "userId", Permissions.MIGRATE_INSTANCE)),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_INSTANCE, "processInstance", "userId", Permissions.READ),
          grant(Resources.PROCESS_DEFINITION, "sourceDefinitionKey", "userId", Permissions.MIGRATE_INSTANCE),
          grant(Resources.PROCESS_DEFINITION, "targetDefinitionKey", "userId", Permissions.MIGRATE_INSTANCE))
        .succeeds(),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_INSTANCE, "processInstance", "userId", Permissions.READ),
          grant(Resources.PROCESS_DEFINITION, "*", "userId", Permissions.MIGRATE_INSTANCE))
        .succeeds()
      );
  }

  @Before
  public void setUp() {
    authRule.createUserAndGroup("userId", "groupId");
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/authorization/oneIncidentProcess.bpmn20.xml")
  public void testMigrate() {

    // given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS)
        .changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("sourceDefinitionKey", sourceDefinition.getKey())
      .bindResource("targetDefinitionKey", targetDefinition.getKey())
      .bindResource("processInstance", processInstance.getId())
      .start();

    engineRule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    if (authRule.assertScenario(scenario)) {
      ProcessInstance processInstanceAfterMigration = engineRule.getRuntimeService().createProcessInstanceQuery().singleResult();

      Assert.assertEquals(targetDefinition.getId(), processInstanceAfterMigration.getProcessDefinitionId());
    }
  }

  @Test
  public void testMigrateWithQuery() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS)
        .changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("sourceDefinitionKey", sourceDefinition.getKey())
      .bindResource("targetDefinitionKey", targetDefinition.getKey())
      .bindResource("processInstance", processInstance.getId())
      .start();

    engineRule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceQuery(query)
      .execute();

    // then
    if (authRule.assertScenario(scenario)) {
      ProcessInstance processInstanceAfterMigration = engineRule.getRuntimeService().createProcessInstanceQuery().singleResult();

      Assert.assertEquals(targetDefinition.getId(), processInstanceAfterMigration.getProcessDefinitionId());
    }
  }
}
