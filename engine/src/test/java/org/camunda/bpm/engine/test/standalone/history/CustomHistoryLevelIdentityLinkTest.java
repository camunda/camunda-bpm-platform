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
package org.camunda.bpm.engine.test.standalone.history;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.task.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

  protected HistoryService historyService;
  protected RuntimeService runtimeService;
  protected IdentityService identityService;
  protected RepositoryService repositoryService;
  protected TaskService taskService;
  protected ProcessEngineConfigurationImpl engineConfiguration;
  protected String deploymentId;

  @Before
  public void setUp() throws Exception {
    ProcessEngine engine = configureEngine();
    runtimeService = engine.getRuntimeService();
    historyService = engine.getHistoryService();
    identityService = engine.getIdentityService();
    repositoryService = engine.getRepositoryService();
    taskService = engine.getTaskService();
  }

  protected ProcessEngine configureEngine() {
    engineConfiguration = (ProcessEngineConfigurationImpl)ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
    engineConfiguration.setJdbcUrl("jdbc:h2:mem:" + getClass().getSimpleName());
    engineConfiguration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_CREATE_DROP);

    HistoryLevel customHisstoryLevelIL = new CustomHistoryLevelIdentityLink(eventTypes);
    engineConfiguration.setCustomHistoryLevels(Arrays.asList(customHisstoryLevelIL));
    engineConfiguration.setHistory("aCustomHistoryLevelIL");

    return engineConfiguration.buildProcessEngine();
  }

  @After
  public void tearDown() throws Exception {
    identityService.clearAuthentication();
    if (deploymentId != null) {
      repositoryService.deleteDeployment(deploymentId, true);
    }
      final HistoryService historyService = engineConfiguration.getHistoryService();
      engineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {

          List<Job> jobs = historyService.findHistoryCleanupJobs();
          for (Job job: jobs) {
            commandContext.getJobManager().deleteJob((JobEntity) job);
            commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
          }

          //cleanup "detached" historic job logs
          final List<HistoricJobLog> list = historyService.createHistoricJobLogQuery().list();
          for (HistoricJobLog jobLog: list) {
            commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobLog.getJobId());
          }

          commandContext.getMeterLogManager().deleteAll();

          return null;
        }
      });

      engineConfiguration.close();
    }

  @Test
  public void testDeletingIdentityLinkByProcDefId() {
    // Pre test
    deploymentId = repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml").deploy().getId();

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
