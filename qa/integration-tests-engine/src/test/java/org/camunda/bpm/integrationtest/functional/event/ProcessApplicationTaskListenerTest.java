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
package org.camunda.bpm.integrationtest.functional.event;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.integrationtest.functional.event.beans.TaskListenerProcessApplication;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class ProcessApplicationTaskListenerTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
        .addAsLibraries(DeploymentHelper.getEngineCdi())
        .addAsResource("META-INF/processes.xml", "META-INF/processes.xml")
        .addClass(AbstractFoxPlatformIntegrationTest.class)
        .addClass(TestContainer.class)
        .addClass(TaskListenerProcessApplication.class)
        .addAsResource("org/camunda/bpm/integrationtest/functional/event/ProcessApplicationEventSupportTest.testTaskListener.bpmn20.xml");

    TestContainer.addContainerSpecificResourcesForNonPa(archive);

    return archive;

  }

  @Test
  public void testTaskListener() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(TaskListener.EVENTNAME_CREATE, false);
    variables.put(TaskListener.EVENTNAME_ASSIGNMENT, false);
    variables.put(TaskListener.EVENTNAME_COMPLETE, false);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", variables);

    boolean createEventFired = (Boolean) runtimeService.getVariable(processInstance.getId(), TaskListener.EVENTNAME_CREATE);
    boolean assignmentEventFired = (Boolean) runtimeService.getVariable(processInstance.getId(), TaskListener.EVENTNAME_ASSIGNMENT);
    boolean completeEventFired = (Boolean) runtimeService.getVariable(processInstance.getId(), TaskListener.EVENTNAME_COMPLETE);

    Assert.assertTrue(createEventFired);
    Assert.assertFalse(assignmentEventFired);
    Assert.assertFalse(completeEventFired);

    Task task = taskService.createTaskQuery().singleResult();
    taskService.claim(task.getId(), "jonny");

    createEventFired = (Boolean) runtimeService.getVariable(processInstance.getId(), TaskListener.EVENTNAME_CREATE);
    assignmentEventFired = (Boolean) runtimeService.getVariable(processInstance.getId(), TaskListener.EVENTNAME_ASSIGNMENT);
    completeEventFired = (Boolean) runtimeService.getVariable(processInstance.getId(), TaskListener.EVENTNAME_COMPLETE);

    Assert.assertTrue(createEventFired);
    Assert.assertTrue(assignmentEventFired);
    Assert.assertFalse(completeEventFired);

    taskService.complete(task.getId());

    createEventFired = (Boolean) runtimeService.getVariable(processInstance.getId(), TaskListener.EVENTNAME_CREATE);
    assignmentEventFired = (Boolean) runtimeService.getVariable(processInstance.getId(), TaskListener.EVENTNAME_ASSIGNMENT);
    completeEventFired = (Boolean) runtimeService.getVariable(processInstance.getId(), TaskListener.EVENTNAME_COMPLETE);

    Assert.assertTrue(createEventFired);
    Assert.assertTrue(assignmentEventFired);
    Assert.assertTrue(completeEventFired);

  }

}
