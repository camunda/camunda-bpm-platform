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
package org.camunda.bpm.engine.test.bpmn.tasklistener.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

/**
 * @author Thorben Lindhauer
 */
public class RecorderTaskListener implements TaskListener, Serializable {

  private static final long serialVersionUID = 1L;

  private static LinkedList<RecordedTaskEvent> recordedEvents = new LinkedList<>();
  private static Map<String, Integer> eventCounters  = new HashMap<>();

  public static class RecordedTaskEvent {

    protected String taskId;
    protected String executionId;
    protected String event;
    protected String activityInstanceId;

    public RecordedTaskEvent(String event) {
      this.event = event;
    }

    public RecordedTaskEvent(String taskId, String executionId, String event, String activityInstanceId) {
      this.executionId = executionId;
      this.taskId = taskId;
      this.event = event;
      this.activityInstanceId = activityInstanceId;
    }

    public String getExecutionId() {
      return executionId;
    }

    public String getTaskId() {
      return taskId;
    }

    public String getEvent() {
      return event;
    }

    public String getActivityInstanceId() {
      return activityInstanceId;
    }

    @Override
    public boolean equals(Object obj) {
      return event.equals(((RecordedTaskEvent) obj).getEvent());
    }
  }

  public void notify(DelegateTask task) {
    DelegateExecution execution = task.getExecution();
    String eventName = task.getEventName();

    recordedEvents.addLast(new RecordedTaskEvent(task.getId(),
                                             task.getExecutionId(),
                                             eventName,
                                             execution.getActivityInstanceId()));

    Integer counter = eventCounters.get(eventName);
    if (counter == null) {
      eventCounters.put(eventName, 1);
    } else {
      eventCounters.put(eventName, ++counter);
    }
  }

  public static void clear() {
    recordedEvents.clear();
    eventCounters.clear();
  }

  public static List<RecordedTaskEvent> getRecordedEvents() {
    return recordedEvents;
  }

  public static Map<String, Integer> getEventCounters() {
    return eventCounters;
  }

  public static int getEventCount(String eventName) {
    Integer count = eventCounters.get(eventName);
    return (count != null)? count : 0;
  }
}
