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
package org.camunda.bpm.spring.boot.starter.test.nonpa;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
public class TestEventCaptor {

  public Stack<HistoryEvent> historyEvents = new Stack<>();
  public Stack<TaskEvent> taskEvents = new Stack<>();
  public Stack<ExecutionEvent> executionEvents = new Stack<>();

  @EventListener
  public void onEvent(HistoryEvent event) {
    historyEvents.push(event);
  }

  @EventListener
  public void onEvent(DelegateExecution event) {
    executionEvents.push(new ExecutionEvent(event));
  }

  @EventListener
  public void onEvent(DelegateTask event) {
    taskEvents.push(new TaskEvent(event));
  }

  public static class ExecutionEvent {
    public final String id;
    public final String processInstanceId;
    public final String activityId;
    public final String eventName;

    public ExecutionEvent(DelegateExecution execution) {
      this.id = execution.getId();
      this.processInstanceId = execution.getProcessInstanceId();
      this.activityId = execution.getCurrentActivityId();
      this.eventName = execution.getEventName();
    }
  }

  public static class TaskEvent {
    public final String id;
    public final String processInstanceId;
    public final String eventName;

    public TaskEvent(DelegateTask task) {
      this.id = task.getId();
      this.processInstanceId = task.getProcessInstanceId();
      this.eventName = task.getEventName();
    }
  }

}
