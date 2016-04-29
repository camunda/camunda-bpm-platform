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
package org.camunda.bpm.engine.test.bpmn.event.compensate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

/**
 * @author Thorben Lindhauer
 *
 */
public class ReadLocalVariableListener implements Serializable, ExecutionListener {

  private static final long serialVersionUID = 1L;

  protected List<VariableEvent> variableEvents = new ArrayList<VariableEvent>();
  protected String variableName;

  public ReadLocalVariableListener(String variableName) {
    this.variableName = variableName;
  }

  public List<VariableEvent> getVariableEvents() {
    return variableEvents;
  }

  public void setVariableEvents(List<VariableEvent> variableEvents) {
    this.variableEvents = variableEvents;
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    if (!execution.hasVariableLocal(variableName)) {
      return;
    }

    Object value = execution.getVariableLocal(variableName);

    VariableEvent event = new VariableEvent();
    event.variableName = variableName;
    event.variableValue = value;
    event.eventName = execution.getEventName();
    event.activityInstanceId = execution.getActivityInstanceId();

    variableEvents.add(event);
  }

  public static class VariableEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String variableName;
    protected Object variableValue;

    protected String activityInstanceId;
    protected String eventName;

    public String getVariableName() {
      return variableName;
    }

    public Object getVariableValue() {
      return variableValue;
    }

    public String getEventName() {
      return eventName;
    }

    public String getActivityInstanceId() {
      return activityInstanceId;
    }
  }
}
