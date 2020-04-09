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

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
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

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrateProcessInstanceSyncQueryTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  protected List<Authorization> authorizations;

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  @Before
  public void setUp() {
    authorizations = new ArrayList<Authorization>();
    authRule.createUserAndGroup("userId", "groupId");
  }

  @After
  public void tearDown() {
    for (Authorization authorization : authorizations) {
      engineRule.getAuthorizationService().deleteAuthorization(authorization.getId());
    }

    authRule.deleteUsersAndGroups();
  }

  @Test
  public void testMigrateWithQuery() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS)
        .changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    ProcessInstance instance1 = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
    ProcessInstance instance2 = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    grantAuthorization("user", Resources.PROCESS_INSTANCE, instance2.getId(), Permissions.READ);
    grantAuthorization("user", Resources.PROCESS_DEFINITION, "*", Permissions.MIGRATE_INSTANCE);

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();

    // when
    authRule.enableAuthorization("user");
    engineRule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceQuery(query)
      .execute();

    authRule.disableAuthorization();


    // then
    ProcessInstance instance1AfterMigration = engineRule
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(instance1.getId())
      .singleResult();

    Assert.assertEquals(sourceDefinition.getId(), instance1AfterMigration.getProcessDefinitionId());
  }

  protected void grantAuthorization(String userId, Resource resource, String resourceId, Permission permission) {
    Authorization authorization = engineRule.getAuthorizationService().createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(resource);
    authorization.setResourceId(resourceId);
    authorization.addPermission(permission);
    authorization.setUserId(userId);
    engineRule.getAuthorizationService().saveAuthorization(authorization);
    authorizations.add(authorization);
  }
}
