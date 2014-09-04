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

import org.camunda.bpm.engine.delegate.SerializedVariableValue;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

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
  protected String type;
  protected Object value;
  protected String valueTypeName;
  protected String processInstanceId;
  protected String errorMessage;
  protected String activityInstanceId;

  protected MockSerializedValueBuilder serializedValueBuilder;
  protected boolean storesCustomObjects;

  public MockHistoricVariableInstanceBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockHistoricVariableInstanceBuilder name(String name) {
    this.name = name;
    return this;
  }

  public MockHistoricVariableInstanceBuilder typeName(String type) {
    this.type = type;
    return this;
  }

  public MockHistoricVariableInstanceBuilder value(Object value) {
    this.value = value;
    return this;
  }

  public MockHistoricVariableInstanceBuilder valueTypeName(String valueTypeName) {
    this.valueTypeName = valueTypeName;
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

  public MockHistoricVariableInstanceBuilder serializedValue(MockSerializedValueBuilder serializedValueBuilder) {
    this.serializedValueBuilder = serializedValueBuilder;
    return this;
  }

  public MockHistoricVariableInstanceBuilder storesCustomObjects(boolean storesCustomObjects) {
    this.storesCustomObjects = storesCustomObjects;
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

  public String getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public String getValueTypeName() {
    return valueTypeName;
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

  public MockSerializedValueBuilder getSerializedValueBuilder() {
    return serializedValueBuilder;
  }

  public boolean isStoresCustomObjects() {
    return storesCustomObjects;
  }

  public HistoricVariableInstance build() {
    HistoricVariableInstance mockVariable = mock(HistoricVariableInstance.class);
    when(mockVariable.getId()).thenReturn(id);
    when(mockVariable.getName()).thenReturn(name);
    when(mockVariable.getVariableName()).thenReturn(name);
    when(mockVariable.getTypeName()).thenReturn(type);
    when(mockVariable.getVariableTypeName()).thenReturn(type);
    when(mockVariable.getValueTypeName()).thenReturn(valueTypeName);
    when(mockVariable.getValue()).thenReturn(value);
    when(mockVariable.getProcessInstanceId()).thenReturn(processInstanceId);
    when(mockVariable.getErrorMessage()).thenReturn(errorMessage);
    when(mockVariable.getActivtyInstanceId()).thenReturn(activityInstanceId);
    when(mockVariable.getActivityInstanceId()).thenReturn(activityInstanceId);
    when(mockVariable.storesCustomObjects()).thenReturn(storesCustomObjects);

    if (serializedValueBuilder != null) {
      SerializedVariableValue mockSerializedValue = serializedValueBuilder.build();
      when(mockVariable.getSerializedValue()).thenReturn(mockSerializedValue);
    }

    return mockVariable;
  }

}
