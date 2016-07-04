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
package org.camunda.bpm.engine.test.api.multitenancy;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskCountByCandidateGroupResult;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Stefan Hentschel.
 */
public class MultiTenancyTaskCountByCandidateGroupTest {

  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule processEngineTestRule = new ProcessEngineTestRule(processEngineRule);

  @Rule
  public RuleChain ruleChain = RuleChain
    .outerRule(processEngineTestRule)
    .around(processEngineRule);


  protected TaskService taskService;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  protected String userId = "aUser";
  protected String groupId = "aGroup";
  protected String tenantId = "aTenant";
  protected String anotherTenantId = "anotherTenant";

  protected List<String> taskIds = new ArrayList<String>();

  @Before
  public void setUp() {
    taskService = processEngineRule.getTaskService();
    identityService = processEngineRule.getIdentityService();
    authorizationService = processEngineRule.getAuthorizationService();
    processEngineConfiguration = processEngineRule.getProcessEngineConfiguration();

    createTask(groupId, tenantId);
    createTask(groupId, anotherTenantId);
    createTask(groupId, anotherTenantId);

    processEngineConfiguration.setTenantCheckEnabled(true);
  }

  @After
  public void cleanUp() {
    processEngineConfiguration.setTenantCheckEnabled(false);

    for (String taskId : taskIds) {
      taskService.deleteTask(taskId, true);
    }
  }

  @Test
  public void shouldOnlyShowTenantSpecificTasks() {
    // given

    identityService.setAuthentication(userId, null, Collections.singletonList(tenantId));

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();

    // then
    assertEquals(1, results.size());
  }

  protected void createTask(String groupId, String tenantId) {
    Task task = taskService.newTask();
    task.setTenantId(tenantId);
    taskService.saveTask(task);

    if (groupId != null) {
      taskService.addCandidateGroup(task.getId(), groupId);
      taskIds.add(task.getId());
    }
  }
}
