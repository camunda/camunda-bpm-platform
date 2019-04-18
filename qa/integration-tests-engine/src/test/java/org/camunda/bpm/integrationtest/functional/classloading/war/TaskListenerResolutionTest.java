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
package org.camunda.bpm.integrationtest.functional.classloading.war;

import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.integrationtest.functional.classloading.beans.ExampleTaskListener;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TaskListenerResolutionTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createProcessArchiveDeplyoment() {
    return initWebArchiveDeployment()
            .addClass(ExampleTaskListener.class)
            .addAsResource("org/camunda/bpm/integrationtest/functional/classloading/TaskListenerResolutionTest.bpmn20.xml");
  }

  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "client.war")
            .addClass(AbstractFoxPlatformIntegrationTest.class);

    TestContainer.addContainerSpecificResources(webArchive);

    return webArchive;

  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveClassOnTaskComplete() {
    // assert that we cannot load the delegate here:
    try {
      Class.forName("org.camunda.bpm.integrationtest.functional.classloading.beans.ExampleTaskListener");
      Assert.fail("CNFE expected");
    }catch (ClassNotFoundException e) {
      // expected
    }

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testTaskListenerProcess");

    // the listener should execute successfully
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    taskService.setAssignee(task.getId(), "john doe");

    Execution execution = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).singleResult();
    Assert.assertNotNull(runtimeService.getVariable(execution.getId(), "listener"));
    runtimeService.removeVariable(execution.getId(), "listener");

    taskService.complete(task.getId());

    Assert.assertNotNull(runtimeService.getVariable(execution.getId(), "listener"));

    // the delegate expression listener should execute successfully
    runtimeService.removeVariable(execution.getId(), "listener");

    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();

    taskService.setAssignee(task.getId(), "john doe");
    Assert.assertNotNull(runtimeService.getVariable(execution.getId(), "listener"));
    runtimeService.removeVariable(execution.getId(), "listener");

    taskService.complete(task.getId());

    Assert.assertNotNull(runtimeService.getVariable(execution.getId(), "listener"));

  }

}
