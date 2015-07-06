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
package org.camunda.bpm.integrationtest.jobexecutor;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.integrationtest.jobexecutor.beans.PriorityBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Arquillian.class)
public class JobPrioritizationTest extends AbstractFoxPlatformIntegrationTest {

  protected ProcessEngine engine1;
  protected RuntimeService runtimeService1;
  protected ManagementService managementService1;
  protected TaskService taskService1;

  protected ProcessInstance processInstance;

  @Before
  public void setEngines() {
    ProcessEngineService engineService = BpmPlatform.getProcessEngineService();
    engine1 = engineService.getProcessEngine("engine1");
    runtimeService1 = engine1.getRuntimeService();
    managementService1 = engine1.getManagementService();
    taskService1 = engine1.getTaskService();
  }

  @Deployment
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment("pa1.war", "org/camunda/bpm/integrationtest/jobexecutor/jobPriorityEngine.xml")
      .addClass(PriorityBean.class)
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/JobPrioritizationTest.serviceTask.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/JobPrioritizationTest.userTask.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/JobPrioritizationTest.intermediateMessage.bpmn20.xml");

  }

  @After
  public void tearDown() {
    if (processInstance != null) {
      runtimeService1.deleteProcessInstance(processInstance.getId(), "");
    }
  }

  @Test
  public void testPriorityOnProcessStart() {

    // given
    processInstance = runtimeService1.startProcessInstanceByKey("serviceTaskProcess");

    Job job = managementService1.createJobQuery().singleResult();

    // then
    Assert.assertEquals(PriorityBean.PRIORITY, job.getPriority());
  }

  @Test
  public void testPriorityOnModification() {

    // given
    processInstance = runtimeService1.startProcessInstanceByKey("serviceTaskProcess");

    TransitionInstance transitionInstance = runtimeService1.getActivityInstance(processInstance.getId())
        .getTransitionInstances("serviceTask")[0];

    // when
    runtimeService1.createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("serviceTask")
      .cancelTransitionInstance(transitionInstance.getId())
      .execute();

    // then
    Job job = managementService1.createJobQuery().singleResult();
    Assert.assertEquals(PriorityBean.PRIORITY, job.getPriority());
  }

  @Test
  public void testPriorityOnInstantiationAtActivity() {

    // when
    processInstance = runtimeService1.createProcessInstanceByKey("serviceTaskProcess")
      .startBeforeActivity("serviceTask")
      .execute();

    // then
    Job job = managementService1.createJobQuery().singleResult();
    Assert.assertEquals(PriorityBean.PRIORITY, job.getPriority());
  }

  @Test
  public void testPriorityOnAsyncAfterUserTask() {
    // given
    processInstance = runtimeService1.startProcessInstanceByKey("userTaskProcess");
    Task task = taskService1.createTaskQuery().singleResult();

    // when
    taskService1.complete(task.getId());

    // then
    Job asyncAfterJob = managementService1.createJobQuery().singleResult();
    Assert.assertEquals(PriorityBean.PRIORITY, asyncAfterJob.getPriority());
  }

  @Test
  public void testPriorityOnAsyncAfterIntermediateCatchEvent() {
    // given
    processInstance = runtimeService1.startProcessInstanceByKey("intermediateMessageProcess");

    // when
    runtimeService1.correlateMessage("Message");

    // then
    Job asyncAfterJob = managementService1.createJobQuery().singleResult();
    Assert.assertEquals(PriorityBean.PRIORITY, asyncAfterJob.getPriority());
  }

}
