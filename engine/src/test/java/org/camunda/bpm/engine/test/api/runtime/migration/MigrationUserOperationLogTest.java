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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MigrationUserOperationLogTest {

  public static final String USER_ID = "userId";

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testLogCreation() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(
        modify(ProcessModels.ONE_TASK_PROCESS).changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    rule.getIdentityService().setAuthenticatedUserId(USER_ID);
    rule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();
    rule.getIdentityService().clearAuthentication();

    // then
    List<UserOperationLogEntry> opLogEntries = rule.getHistoryService().createUserOperationLogQuery().list();
    Assert.assertEquals(3, opLogEntries.size());

    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);

    UserOperationLogEntry procDefEntry = entries.get("processDefinitionId");
    Assert.assertNotNull(procDefEntry);
    Assert.assertEquals("ProcessInstance", procDefEntry.getEntityType());
    Assert.assertEquals("Migrate", procDefEntry.getOperationType());
    Assert.assertEquals(sourceProcessDefinition.getId(), procDefEntry.getProcessDefinitionId());
    Assert.assertEquals(sourceProcessDefinition.getKey(), procDefEntry.getProcessDefinitionKey());
    Assert.assertNull(procDefEntry.getProcessInstanceId());
    Assert.assertEquals(sourceProcessDefinition.getId(), procDefEntry.getOrgValue());
    Assert.assertEquals(targetProcessDefinition.getId(), procDefEntry.getNewValue());

    UserOperationLogEntry asyncEntry = entries.get("async");
    Assert.assertNotNull(asyncEntry);
    Assert.assertEquals("ProcessInstance", asyncEntry.getEntityType());
    Assert.assertEquals("Migrate", asyncEntry.getOperationType());
    Assert.assertEquals(sourceProcessDefinition.getId(), asyncEntry.getProcessDefinitionId());
    Assert.assertEquals(sourceProcessDefinition.getKey(), asyncEntry.getProcessDefinitionKey());
    Assert.assertNull(asyncEntry.getProcessInstanceId());
    Assert.assertNull(asyncEntry.getOrgValue());
    Assert.assertEquals("false", asyncEntry.getNewValue());

    UserOperationLogEntry numInstanceEntry = entries.get("nrOfInstances");
    Assert.assertNotNull(numInstanceEntry);
    Assert.assertEquals("ProcessInstance", numInstanceEntry.getEntityType());
    Assert.assertEquals("Migrate", numInstanceEntry.getOperationType());
    Assert.assertEquals(sourceProcessDefinition.getId(), numInstanceEntry.getProcessDefinitionId());
    Assert.assertEquals(sourceProcessDefinition.getKey(), numInstanceEntry.getProcessDefinitionKey());
    Assert.assertNull(numInstanceEntry.getProcessInstanceId());
    Assert.assertNull(numInstanceEntry.getOrgValue());
    Assert.assertEquals("1", numInstanceEntry.getNewValue());

    Assert.assertEquals(procDefEntry.getOperationId(), asyncEntry.getOperationId());
    Assert.assertEquals(asyncEntry.getOperationId(), numInstanceEntry.getOperationId());
  }

  protected Map<String, UserOperationLogEntry> asMap(List<UserOperationLogEntry> logEntries) {
    Map<String, UserOperationLogEntry> map = new HashMap<String, UserOperationLogEntry>();

    for (UserOperationLogEntry entry : logEntries) {

      UserOperationLogEntry previousValue = map.put(entry.getProperty(), entry);
      if (previousValue != null) {
        Assert.fail("expected only entry for every property");
      }
    }

    return map;
  }
}
