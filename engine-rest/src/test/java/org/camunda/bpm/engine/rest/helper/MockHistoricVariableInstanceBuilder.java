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
package org.camunda.bpm.engine.rest.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Creates variable mocks and simultaneously provides a ResponseSpecification
 * to assert a result of this mock in json; expectations can be overriden using the
 * methods {@link JsonSpec#setMatcher(String, org.hamcrest.Matcher)} and
 * {@link JsonSpec#setEnclosedJsonSpec(String, JsonSpec)}.
 *
 * @author Thorben Lindhauer
 */
public class MockHistoricVariableInstanceBuilder {

  protected String id;
  protected String name;
  protected TypedValue value;
  protected String processInstanceId;
  protected String errorMessage;
  protected String activityInstanceId;

  public MockHistoricVariableInstanceBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockHistoricVariableInstanceBuilder name(String name) {
    this.name = name;
    return this;
  }

  public MockHistoricVariableInstanceBuilder typedValue(TypedValue value) {
    this.value = value;
    return this;
  }

  public MockHistoricVariableInstanceBuilder processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public MockHistoricVariableInstanceBuilder errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public MockHistoricVariableInstanceBuilder activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public TypedValue getTypedValue() {
    return value;
  }

  public Object getValue() {
    return value.getValue();
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public HistoricVariableInstance build() {
    HistoricVariableInstance mockVariable = mock(HistoricVariableInstance.class);
    when(mockVariable.getId()).thenReturn(id);
    when(mockVariable.getName()).thenReturn(name);
    when(mockVariable.getVariableName()).thenReturn(name);
    when(mockVariable.getTypeName()).thenReturn(value.getType().getName());
    when(mockVariable.getVariableTypeName()).thenReturn(value.getType().getName());

    if (ObjectValue.class.isAssignableFrom(value.getClass())) {
      ObjectValue objectValue = (ObjectValue) value;
      if (objectValue.isDeserialized()) {
        when(mockVariable.getValue()).thenReturn(value.getValue());
      } else {
        when(mockVariable.getValue()).thenReturn(null);
      }
    } else {
      when(mockVariable.getValue()).thenReturn(value.getValue());
    }

    when(mockVariable.getTypedValue()).thenReturn(value);
    when(mockVariable.getProcessInstanceId()).thenReturn(processInstanceId);
    when(mockVariable.getErrorMessage()).thenReturn(errorMessage);
    when(mockVariable.getActivtyInstanceId()).thenReturn(activityInstanceId);
    when(mockVariable.getActivityInstanceId()).thenReturn(activityInstanceId);

    return mockVariable;
  }

}
