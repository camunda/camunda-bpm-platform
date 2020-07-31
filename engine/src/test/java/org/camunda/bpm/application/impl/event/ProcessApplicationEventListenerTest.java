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
package org.camunda.bpm.application.impl.event;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationEventListenerTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      "org/camunda/bpm/application/impl/event/pa.event.listener.camunda.cfg.xml");
  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected TaskService taskService;
  protected ManagementService managementService;

  protected String deploymentId;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();
    taskService = engineRule.getTaskService();
    managementService = engineRule.getManagementService();
    deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
  }

  @After
  public void closeDownProcessEngine() {
    managementService.unregisterProcessApplication(deploymentId, false);
  }

  @Test
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

  @Test
  @Deployment(resources = { "org/camunda/bpm/application/impl/event/ProcessApplicationEventListenerTest.testExecutionListener.bpmn20.xml" })
  public void testShouldInvokeExecutionListenerOnStartAndEndOfProcessInstance() {
    final AtomicInteger processDefinitionEventCount = new AtomicInteger();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        // this process application returns an execution listener
        return execution -> {
          if (((CoreExecution) execution).getEventSource() instanceof ProcessDefinitionEntity)
            processDefinitionEventCount.incrementAndGet();
        };
      }
    };

    // register app so that it receives events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // Start process instance.
    runtimeService.startProcessInstanceByKey("startToEnd");

    // Start and end of the process 
    assertEquals(2, processDefinitionEventCount.get());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/application/impl/event/ProcessApplicationEventListenerTest.testExecutionListener.bpmn20.xml" })
  public void testShouldNotIncrementExecutionListenerCountOnStartAndEndOfProcessInstance() {
    final AtomicInteger eventCount = new AtomicInteger();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        // this process application returns an execution listener
        return execution -> {
          if (!(((CoreExecution) execution).getEventSource() instanceof ProcessDefinitionEntity))
            eventCount.incrementAndGet();
        };
      }
    };

    // register app so that it receives events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // Start process instance.
    runtimeService.startProcessInstanceByKey("startToEnd");

    assertEquals(5, eventCount.get());
  }

  @Test
  @Deployment
  public void testExecutionListener() {
    final AtomicInteger eventCount = new AtomicInteger();
    
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        // this process application returns an execution listener
        return execution -> eventCount.incrementAndGet();
      }
    };

    // register app so that it is notified about events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // start process instance
    runtimeService.startProcessInstanceByKey("startToEnd");

    // 7 events received
    assertEquals(7, eventCount.get());
   }

  @Test
  @Deployment
  public void testExecutionListenerWithErrorBoundaryEvent() {
    final AtomicInteger eventCount = new AtomicInteger();
    
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        return execution -> eventCount.incrementAndGet();
      }
    };

    // register app so that it is notified about events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // 1. (start)startEvent(end) -(take)-> (start)serviceTask(end) -(take)-> (start)endEvent(end) (8 Events)

    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener");

    assertEquals(10, eventCount.get());
    
    // reset counter
    eventCount.set(0);
    
    // 2. (start)startEvent(end) -(take)-> (start)serviceTask(end)/(start)errorBoundaryEvent(end) -(take)-> (start)endEvent(end) (10 Events)

    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener", Collections.singletonMap("shouldThrowError", true));

    assertEquals(12, eventCount.get());
  }

  @Test
  @Deployment
  public void testExecutionListenerWithTimerBoundaryEvent() {
    final AtomicInteger eventCount = new AtomicInteger();
    
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        return execution -> eventCount.incrementAndGet();
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

    assertEquals(10, eventCount.get());
    
    // reset counter
    eventCount.set(0);
    
    // 2. (start)startEvent(end) -(take)-> (start)userTask(end)/(start)timerBoundaryEvent(end) -(take)-> (start)endEvent(end) (10 Events)

    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener");

    // fire timer event
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    assertEquals(12, eventCount.get());
  }

  @Test
  @Deployment
  public void testExecutionListenerWithSignalBoundaryEvent() {
    final AtomicInteger eventCount = new AtomicInteger();
    
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        return execution -> eventCount.incrementAndGet();
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

    assertEquals(10, eventCount.get());
    
    // reset counter
    eventCount.set(0);
    
    // 2. (start)startEvent(end) -(take)-> (start)userTask(end)/(start)signalBoundaryEvent(end) -(take)-> (start)endEvent(end) (10 Events)

    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener");

    // signal event
    runtimeService.signalEventReceived("signal");

    assertEquals(12, eventCount.get());
  }

  @Test
  @Deployment
  public void testExecutionListenerWithMultiInstanceBody() {
    final AtomicInteger eventCountForMultiInstanceBody = new AtomicInteger();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        return execution -> {
          if ("miTasks#multiInstanceBody".equals(execution.getCurrentActivityId())
              && (ExecutionListener.EVENTNAME_START.equals(execution.getEventName())
                  || ExecutionListener.EVENTNAME_END.equals(execution.getEventName()))) {
            eventCountForMultiInstanceBody.incrementAndGet();
          }
        };
      }
    };

    // register app so that it is notified about events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());


    // start process instance
    runtimeService.startProcessInstanceByKey("executionListener");

    // complete task
    List<Task> miTasks = taskService.createTaskQuery().list();
    for (Task task : miTasks) {
      taskService.complete(task.getId());
    }

    // 2 events are expected: one for mi body start; one for mi body end
    assertEquals(2, eventCountForMultiInstanceBody.get());
  }

  @Test
  @Deployment
  public void testTaskListener() {

    final List<String> events = new ArrayList<>();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public TaskListener getTaskListener() {
        return delegateTask -> events.add(delegateTask.getEventName());
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
    assertEquals(3, events.size());
    assertEquals(TaskListener.EVENTNAME_UPDATE, events.get(1));
    assertEquals(TaskListener.EVENTNAME_ASSIGNMENT, events.get(2));

    // complete task
    taskService.complete(task.getId());
    assertEquals(5, events.size());
    assertEquals(TaskListener.EVENTNAME_COMPLETE, events.get(3));
    // next task was created
    assertEquals(TaskListener.EVENTNAME_CREATE, events.get(4));

    // delete process instance so last task will be deleted
    runtimeService.deleteProcessInstance(taskListenerProcess.getProcessInstanceId(), "test delete event");
    assertEquals(6, events.size());
    assertEquals(TaskListener.EVENTNAME_DELETE, events.get(5));

  }

  @Test
  @Deployment
  public void testIntermediateTimerEvent() {

    // given
    final List<String> timerEvents = new ArrayList<>();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        return delegateExecution -> {
          String currentActivityId = delegateExecution.getCurrentActivityId();
          String eventName = delegateExecution.getEventName();
          if ("timer".equals(currentActivityId) &&
              (ExecutionListener.EVENTNAME_START.equals(eventName) || ExecutionListener.EVENTNAME_END.equals(eventName))) {
            timerEvents.add(delegateExecution.getEventName());
          }
        };
      }
    };

    // register app so that it is notified about events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // when
    runtimeService.startProcessInstanceByKey("process");
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.executeJob(jobId);

    // then
    assertEquals(2, timerEvents.size());

    // "start" event listener
    assertEquals(ExecutionListener.EVENTNAME_START, timerEvents.get(0));

    // "end" event listener
    assertEquals(ExecutionListener.EVENTNAME_END, timerEvents.get(1));
  }

  @Test
  @Deployment
  public void testIntermediateSignalEvent() {

    // given
    final List<String> timerEvents = new ArrayList<>();

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication() {
      public ExecutionListener getExecutionListener() {
        return delegateExecution -> {
          String currentActivityId = delegateExecution.getCurrentActivityId();
          String eventName = delegateExecution.getEventName();
          if ("signal".equals(currentActivityId) &&
              (ExecutionListener.EVENTNAME_START.equals(eventName) || ExecutionListener.EVENTNAME_END.equals(eventName))) {
            timerEvents.add(delegateExecution.getEventName());
          }
        };
      }
    };

    // register app so that it is notified about events
    managementService.registerProcessApplication(deploymentId, processApplication.getReference());

    // when
    runtimeService.startProcessInstanceByKey("process");
    runtimeService.signalEventReceived("abort");

    // then
    assertEquals(2, timerEvents.size());

    // "start" event listener
    assertEquals(ExecutionListener.EVENTNAME_START, timerEvents.get(0));

    // "end" event listener
    assertEquals(ExecutionListener.EVENTNAME_END, timerEvents.get(1));
  }

}
