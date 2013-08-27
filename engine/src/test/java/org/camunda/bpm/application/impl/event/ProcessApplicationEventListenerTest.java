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
package org.camunda.bpm.application.impl.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationEventListenerTest extends ResourceProcessEngineTestCase {

  public ProcessApplicationEventListenerTest() {
    // ProcessApplicationEventListenerPlugin is activated in configuration
    super("org/camunda/bpm/application/impl/event/pa.event.listener.camunda.cfg.xml");
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void closeDownProcessEngine() {
    managementService.unregisterProcessApplication(deploymentId, false);
    processEngine.close();
    super.closeDownProcessEngine();
  }

  @Deployment(resources = { "org/camunda/bpm/application/impl/event/ProcessApplicationEventListenerTest.testExecutionListener.bpmn20.xml" })
  public void testExecutionListenerNull() {

    // this test verifies that the process application can return a 'null'
    // execution listener
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    // register app so that it receives events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());
    // I can start a process event though the process app does not provide an
    // event listener.
    runtimeService.startProcessInstanceByKey("startToEnd");

  }

  @Deployment
  public void testExecutionListener() {
    final AtomicInteger eventCount = new AtomicInteger();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        // this process application returns an execution listener
        return new ExecutionListener() {
          public void notify(DelegateExecution execution) throws Exception {
            eventCount.incrementAndGet();
          }
        };
      }
    };

    // register app so that it is notified about events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // start process instance
    runtimeService.startProcessInstanceByKey("startToEnd");

    // 5 events received
    assertEquals(5, eventCount.get());

  }

  @Deployment
  public void testTaskListener() {

    final List<String> events = new ArrayList<String>();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public TaskListener getTaskListener() {
        return new TaskListener() {
          public void notify(DelegateTask delegateTask) {
            events.add(delegateTask.getEventName());
          }
        };
      }
    };

    // register app so that it is notified about events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // start process instance
    runtimeService.startProcessInstanceByKey("taskListenerProcess");

    // create event received
    assertEquals(1, events.size());
    assertEquals(TaskListener.EVENTNAME_CREATE, events.get(0));

    Task task = taskService.createTaskQuery().singleResult();
    //assign task:
    taskService.setAssignee(task.getId(), "jonny");
    assertEquals(2, events.size());
    assertEquals(TaskListener.EVENTNAME_ASSIGNMENT, events.get(1));

    // complete task
    taskService.complete(task.getId());
    assertEquals(3, events.size());
    assertEquals(TaskListener.EVENTNAME_COMPLETE, events.get(2));

  }

}
