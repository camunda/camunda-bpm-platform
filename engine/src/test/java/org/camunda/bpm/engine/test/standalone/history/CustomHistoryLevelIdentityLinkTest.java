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
package org.camunda.bpm.engine.test.standalone.history;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CustomHistoryLevelIdentityLinkTest {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      new Object[]{ Arrays.asList(HistoryEventTypes.IDENTITY_LINK_ADD) },
      new Object[]{ Arrays.asList(HistoryEventTypes.IDENTITY_LINK_DELETE, HistoryEventTypes.IDENTITY_LINK_ADD) }
    });
  }

  @Parameter
  public List<HistoryEventTypes> eventTypes;

  static CustomHistoryLevelIdentityLink customHisstoryLevelIL = new CustomHistoryLevelIdentityLink();

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
    configuration.setJdbcUrl("jdbc:h2:mem:" + CustomHistoryLevelIdentityLinkTest.class.getSimpleName());
    List<HistoryLevel> levels = new ArrayList<>();
    levels.add(customHisstoryLevelIL);
    configuration.setCustomHistoryLevels(levels);
    configuration.setHistory("aCustomHistoryLevelIL");
    configuration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_CREATE_DROP);
  });
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected HistoryService historyService;
  protected RuntimeService runtimeService;
  protected IdentityService identityService;
  protected RepositoryService repositoryService;
  protected TaskService taskService;

  @Before
  public void setUp() throws Exception {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    identityService = engineRule.getIdentityService();
    repositoryService = engineRule.getRepositoryService();
    taskService = engineRule.getTaskService();

    customHisstoryLevelIL.setEventTypes(eventTypes);
  }

  @After
  public void tearDown() {
    customHisstoryLevelIL.setEventTypes(null);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testDeletingIdentityLinkByProcDefId() {
    // Pre test
    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    identityService.setAuthenticatedUserId("anAuthUser");
    taskService.addCandidateUser(taskId, "aUser");
    taskService.deleteCandidateUser(taskId, "aUser");

    // assume
    historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertTrue(historicIdentityLinks.size() > 0);

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey("oneTaskProcess")
      .cascade()
      .delete();

    // then
    historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(0, historicIdentityLinks.size());
  }

  @Test
  public void testDeletingIdentityLinkByTaskId() {
    // Pre test
    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();

    identityService.setAuthenticatedUserId("anAuthUser");
    taskService.addCandidateUser(taskId, "aUser");
    taskService.deleteCandidateUser(taskId, "aUser");

    // assume
    historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertTrue(historicIdentityLinks.size() > 0);

    // when
    taskService.deleteTask(taskId, true);

    // then
    historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(0, historicIdentityLinks.size());
  }

}
