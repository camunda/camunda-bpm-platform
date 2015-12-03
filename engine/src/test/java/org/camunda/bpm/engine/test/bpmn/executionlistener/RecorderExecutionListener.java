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

package org.camunda.bpm.engine.test.bpmn.executionlistener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.core.model.PropertyKey;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Bernd Ruecker
 */
public class RecorderExecutionListener implements ExecutionListener, Serializable {

  private static final long serialVersionUID = 1L;

  private FixedValue parameter;

  private static List<RecorderExecutionListener.RecordedEvent> recordedEvents = new ArrayList<RecorderExecutionListener.RecordedEvent>();

  public static class RecordedEvent {
    private final String activityId;
    private final String eventName;
    private final String activityName;
    private final String parameter;
    private final String activityInstanceId;
    private final String transitionId;
    private final boolean canceled;
    private final String executionId;

    public RecordedEvent(String activityId, String activityName, String eventName, String parameter, String activityInstanceId, String transitionId, boolean canceled, String executionId) {
      this.activityId = activityId;
      this.activityName = activityName;
      this.parameter = parameter;
      this.eventName = eventName;
      this.activityInstanceId = activityInstanceId;
      this.transitionId = transitionId;
      this.canceled = canceled;
      this.executionId = executionId;
    }

    public String getActivityId() {
      return activityId;
    }

    public String getEventName() {
      return eventName;
    }


    public String getActivityName() {
      return activityName;
    }


    public String getParameter() {
      return parameter;
    }

    public String getActivityInstanceId() {
      return activityInstanceId;
    }

    public String getTransitionId() {
      return transitionId;
    }

    public boolean isCanceled(){
      return canceled;
    }

    public String getExecutionId(){
      return executionId;
    }
  }

  public void notify(DelegateExecution execution) throws Exception {
    ExecutionEntity executionCasted = ((ExecutionEntity)execution);
    String parameterValue = null;
    if (parameter != null) {
      parameterValue = (String)parameter.getValue(execution);
    }

    String activityName = null;
    if (executionCasted.getActivity() != null) {
      activityName = executionCasted.getActivity().getProperties().get(new PropertyKey<String>("name"));
    }

    recordedEvents.add( new RecordedEvent(
                    executionCasted.getActivityId(),
                    activityName,
                    execution.getEventName(),
                    parameterValue,
                    execution.getActivityInstanceId(),
                    execution.getCurrentTransitionId(),
                    execution.isCanceled(),
                    execution.getId()));
  }

  public static void clear() {
    recordedEvents.clear();
  }

  public static List<RecordedEvent> getRecordedEvents() {
    return recordedEvents;
  }


}
