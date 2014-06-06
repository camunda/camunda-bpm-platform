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

import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
  public void testExecutionListenerWithErrorBoundaryEvent() {
    final AtomicInteger eventCount = new AtomicInteger();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        return new ExecutionListener() {
          public void notify(DelegateExecution execution) throws Exception {
            eventCount.incrementAndGet();
          }
        };
      }
    };

    // register app so that it is notified about events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // 1. (start)startEvent(end) -(take)-> (start)serviceTask(end) -(take)-> (start)endEvent(end) (8 Events)

    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener");

    assertEquals(8, eventCount.get());

    // reset counter
    eventCount.set(0);

    // 2. (start)startEvent(end) -(take)-> (start)serviceTask(end)/(start)errorBoundaryEvent(end) -(take)-> (start)endEvent(end) (10 Events)

    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener", Collections.<String, Object>singletonMap("shouldThrowError", true));

    assertEquals(10, eventCount.get());
  }

  @Deployment
  public void testExecutionListenerWithTimerBoundaryEvent() {
    final AtomicInteger eventCount = new AtomicInteger();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        return new ExecutionListener() {
          public void notify(DelegateExecution execution) throws Exception {
            eventCount.incrementAndGet();
          }
        };
      }
    };

    // register app so that it is notified about events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // 1. (start)startEvent(end) -(take)-> (start)userTask(end) -(take)-> (start)endEvent(end) (8 Events)

    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener");

    // complete task
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertEquals(8, eventCount.get());

    // reset counter
    eventCount.set(0);

    // 2. (start)startEvent(end) -(take)-> (start)userTask(end)/(start)timerBoundaryEvent(end) -(take)-> (start)endEvent(end) (10 Events)

    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener");

    // fire timer event
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    assertEquals(10, eventCount.get());
  }

  @Deployment
  public void testExecutionListenerWithSignalBoundaryEvent() {
    final AtomicInteger eventCount = new AtomicInteger();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        return new ExecutionListener() {
          public void notify(DelegateExecution execution) throws Exception {
            eventCount.incrementAndGet();
          }
        };
      }
    };

    // register app so that it is notified about events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // 1. (start)startEvent(end) -(take)-> (start)userTask(end) -(take)-> (start)endEvent(end) (8 Events)

    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener");

    // complete task
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertEquals(8, eventCount.get());

    // reset counter
    eventCount.set(0);

    // 2. (start)startEvent(end) -(take)-> (start)userTask(end)/(start)signalBoundaryEvent(end) -(take)-> (start)endEvent(end) (10 Events)

    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener");

    // signal event
    runtimeService.signalEventReceived("signal");

    assertEquals(10, eventCount.get());
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
    ProcessInstance taskListenerProcess = runtimeService.startProcessInstanceByKey("taskListenerProcess");

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
    assertEquals(4, events.size());
    assertEquals(TaskListener.EVENTNAME_COMPLETE, events.get(2));
    // next task was created
    assertEquals(TaskListener.EVENTNAME_CREATE, events.get(3));

    // delete process instance so last task will be deleted
    runtimeService.deleteProcessInstance(taskListenerProcess.getProcessInstanceId(), "test delete event");
    assertEquals(5, events.size());
    assertEquals(TaskListener.EVENTNAME_DELETE, events.get(4));

  }

}
