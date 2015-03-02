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

package org.camunda.bpm.engine.test.examples.bpmn.tasklistener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

/**
 * @author Thorben Lindhauer
 */
public class RecorderTaskListener implements TaskListener, Serializable {

  private static final long serialVersionUID = 1L;

  private static List<RecorderTaskListener.RecordedTaskEvent> recordedEvents = new ArrayList<RecorderTaskListener.RecordedTaskEvent>();

  public static class RecordedTaskEvent {

    protected String taskId;
    protected String executionId;
    protected String event;

    public RecordedTaskEvent(String taskId, String executionId, String event) {
      this.executionId = executionId;
      this.taskId = taskId;
      this.event = event;
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

  }

  public void notify(DelegateTask task) {
    recordedEvents.add(new RecordedTaskEvent(task.getId(), task.getExecutionId(), task.getEventName()));
  }

  public static void clear() {
    recordedEvents.clear();
  }

  public static List<RecordedTaskEvent> getRecordedEvents() {
    return recordedEvents;
  }


}
