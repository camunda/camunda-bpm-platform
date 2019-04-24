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
package org.camunda.bpm.integrationtest.jobexecutor;

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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Arquillian.class)
public class JobPrioritizationTest extends AbstractFoxPlatformIntegrationTest {

  protected ProcessInstance processInstance;

  @Deployment
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment()
      .addClass(PriorityBean.class)
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/JobPrioritizationTest.priorityProcess.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/JobPrioritizationTest.serviceTask.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/JobPrioritizationTest.userTask.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/JobPrioritizationTest.intermediateMessage.bpmn20.xml");
  }

  @After
  public void tearDown() {
    if (processInstance != null) {
      runtimeService.deleteProcessInstance(processInstance.getId(), "");
    }
  }

  @Test
  public void testPriorityOnProcessElement() {
    // given
    processInstance = runtimeService.startProcessInstanceByKey("priorityProcess");

    Job job = managementService.createJobQuery().singleResult();

    // then
    Assert.assertEquals(PriorityBean.PRIORITY, job.getPriority());

  }

  @Test
  public void testPriorityOnProcessStart() {

    // given
    processInstance = runtimeService.startProcessInstanceByKey("serviceTaskProcess");

    Job job = managementService.createJobQuery().singleResult();

    // then
    Assert.assertEquals(PriorityBean.PRIORITY, job.getPriority());
  }

  @Test
  public void testPriorityOnModification() {

    // given
    processInstance = runtimeService.startProcessInstanceByKey("serviceTaskProcess");

    TransitionInstance transitionInstance = runtimeService.getActivityInstance(processInstance.getId())
        .getTransitionInstances("serviceTask")[0];

    // when
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("serviceTask")
      .cancelTransitionInstance(transitionInstance.getId())
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(PriorityBean.PRIORITY, job.getPriority());
  }

  @Test
  public void testPriorityOnInstantiationAtActivity() {

    // when
    processInstance = runtimeService.createProcessInstanceByKey("serviceTaskProcess")
      .startBeforeActivity("serviceTask")
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(PriorityBean.PRIORITY, job.getPriority());
  }

  @Test
  public void testPriorityOnAsyncAfterUserTask() {
    // given
    processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.complete(task.getId());

    // then
    Job asyncAfterJob = managementService.createJobQuery().singleResult();
    Assert.assertEquals(PriorityBean.PRIORITY, asyncAfterJob.getPriority());
  }

  @Test
  public void testPriorityOnAsyncAfterIntermediateCatchEvent() {
    // given
    processInstance = runtimeService.startProcessInstanceByKey("intermediateMessageProcess");

    // when
    runtimeService.correlateMessage("Message");

    // then
    Job asyncAfterJob = managementService.createJobQuery().singleResult();
    Assert.assertEquals(PriorityBean.PRIORITY, asyncAfterJob.getPriority());
  }

}
