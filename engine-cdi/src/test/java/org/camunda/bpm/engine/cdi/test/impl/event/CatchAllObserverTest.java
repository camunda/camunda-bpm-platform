package org.camunda.bpm.engine.cdi.test.impl.event;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.cdi.BusinessProcessEvent;
import org.camunda.bpm.engine.cdi.BusinessProcessEventType;
import org.camunda.bpm.engine.cdi.annotation.event.CreateTask;
import org.camunda.bpm.engine.cdi.impl.event.CdiBusinessProcessEvent;
import org.camunda.bpm.engine.cdi.impl.event.CdiEventListener;
import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ExecutionContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.junit.Test;

public class CatchAllObserverTest extends CdiProcessEngineTestCase {

  @Singleton
  public static class Observer {

    private int calls;

    public void handleConcrete(@Observes @CreateTask("doSomething") BusinessProcessEvent event) {
      calls++;
    }

    public void handleGeneric(@Observes @CreateTask BusinessProcessEvent event) {
      calls++;
    }
  }

  @Inject
  private Observer observer;

  private final CdiEventListener cdiEventListener = new CdiEventListener();

  @Test
  public void catches_both_generic_and_special_event() {
    final TaskEntity task = new TaskEntity(UUID.randomUUID().toString());
    task.setEventName(TaskListener.EVENTNAME_CREATE);
    task.setTaskDefinitionKey("doSomething");

    // This is caught by both methods
    cdiEventListener.notify(task);
    assertThat(observer.calls, is(2));

    // do no catch on assignment.
    task.setEventName(TaskListener.EVENTNAME_ASSIGNMENT);
    cdiEventListener.notify(task);
    assertThat(observer.calls, is(2));

    // catch generic task create but ignore because of definition name
    task.setEventName(TaskListener.EVENTNAME_CREATE);
    task.setTaskDefinitionKey("otherTask");
    cdiEventListener.notify(task);
    assertThat(observer.calls, is(3));
  }

  protected BusinessProcessEvent createEvent(DelegateTask task) {
    ExecutionContext executionContext = Context.getExecutionContext();
    ProcessDefinitionEntity processDefinition = null;
    if (executionContext != null) {
      processDefinition = executionContext.getProcessDefinition();
    }

    // map type
    String eventName = task.getEventName();
    BusinessProcessEventType type = null;
    if (TaskListener.EVENTNAME_CREATE.equals(eventName)) {
      type = BusinessProcessEventType.CREATE_TASK;
    } else if (TaskListener.EVENTNAME_ASSIGNMENT.equals(eventName)) {
      type = BusinessProcessEventType.ASSIGN_TASK;
    } else if (TaskListener.EVENTNAME_COMPLETE.equals(eventName)) {
      type = BusinessProcessEventType.COMPLETE_TASK;
    } else if (TaskListener.EVENTNAME_DELETE.equals(eventName)) {
      type = BusinessProcessEventType.DELETE_TASK;
    }

    return new CdiBusinessProcessEvent(task, processDefinition, type, ClockUtil.getCurrentTime());
  }

}
