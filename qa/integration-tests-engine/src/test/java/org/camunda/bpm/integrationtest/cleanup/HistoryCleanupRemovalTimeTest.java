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
package org.camunda.bpm.integrationtest.cleanup;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import static org.apache.commons.lang.time.DateUtils.addDays;

/**
 * @author Tassilo Weidner
 */
@RunWith(Arquillian.class)
public class HistoryCleanupRemovalTimeTest extends AbstractFoxPlatformIntegrationTest {

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ManagementService managementService;

  @Before
  public void setEngines() {
    ProcessEngineService engineService = BpmPlatform.getProcessEngineService();
    ProcessEngine processEngine = engineService.getProcessEngine("engineOne");

    runtimeService = processEngine.getRuntimeService();
    historyService = processEngine.getHistoryService();
    managementService = processEngine.getManagementService();
  }

  @Deployment(name = "engineOne", order = 1)
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment("archiveOne.war", "singleEngineWithHistoryCleanupRemovalTime.xml")
      .addClass(DateUtils.class)
      .addAsResource("org/camunda/bpm/integrationtest/cleanup/CleanupProcess.bpmn20.xml");
  }

  @Test
  @OperateOnDeployment("engineOne")
  public void shouldCleanup() {
    // given
    Date END_DATE = new Date();

    ClockUtil.setCurrentTime(END_DATE);

    runtimeService.startProcessInstanceByKey("cleanupProcess");

    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().list();

    // assume
    Assert.assertEquals(100, tasks.size());

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    for (HistoricTaskInstance task : tasks) {
      System.out.println(task.getRemovalTime());
    }

    // when
    runHistoryCleanup();

    tasks = historyService.createHistoricTaskInstanceQuery().list();

    // then
    Assert.assertEquals(0, tasks.size());
  }

  protected void runHistoryCleanup() {
    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();
    for (Job job : jobs) {
      managementService.executeJob(job.getId());
    }

  }

}
