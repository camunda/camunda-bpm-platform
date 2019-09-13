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

import java.util.List;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.integrationtest.jobexecutor.beans.SampleTaskListenerBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TimeoutTaskListenerExecutionTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    WebArchive archive = initWebArchiveDeployment()
            .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/TimeoutTaskListenerExecution.bpmn20.xml")
            .addClass(SampleTaskListenerBean.class);

    return archive;
  }

  @Test
  public void testProcessExecution() {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");

    waitForJobExecutorToProcessAllJobs();

    List<ProcessInstance> finallyRunningInstances = runtimeService.createProcessInstanceQuery().processInstanceId(instance.getId()).list();
    Assert.assertEquals(1, finallyRunningInstances.size());

    Task task = taskService.createTaskQuery().processInstanceId(instance.getId()).singleResult();
    Assert.assertNotNull(task);

    Object variable = taskService.getVariable(task.getId(), "called");
    Assert.assertNotNull(variable);

    Assert.assertTrue((boolean) variable);
  }
}
