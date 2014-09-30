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
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.runtime.VariableInstance;

/**
 * Creates variable mocks and simultaneously provides a ResponseSpecification
 * to assert a result of this mock in json; expectations can be overriden using the
 * methods {@link JsonSpec#setMatcher(String, org.hamcrest.Matcher)} and
 * {@link JsonSpec#setEnclosedJsonSpec(String, JsonSpec)}.
 *
 * @author Thorben Lindhauer
 */
public class MockVariableInstanceBuilder {

  protected String id;
  protected String name;
  protected String type;
  protected Object value;
  protected String valueTypeName;
  protected String processInstanceId;
  protected String executionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String taskId;
  protected String activityInstanceId;
  protected String errorMessage;

  protected MockSerializedValueBuilder serializedValueBuilder;
  protected boolean storesCustomObjects;

  public MockVariableInstanceBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockVariableInstanceBuilder name(String name) {
    this.name = name;
    return this;
  }

  public MockVariableInstanceBuilder typeName(String type) {
    this.type = type;
    return this;
  }

  public MockVariableInstanceBuilder value(Object value) {
    this.value = value;
    return this;
  }

  public MockVariableInstanceBuilder valueTypeName(String valueTypeName) {
    this.valueTypeName = valueTypeName;
    return this;
  }

  public MockVariableInstanceBuilder processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public MockVariableInstanceBuilder executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public MockVariableInstanceBuilder caseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public MockVariableInstanceBuilder caseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  public MockVariableInstanceBuilder taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  public MockVariableInstanceBuilder activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public MockVariableInstanceBuilder errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public MockVariableInstanceBuilder serializedValue(MockSerializedValueBuilder serializedValueBuilder) {
    this.serializedValueBuilder = serializedValueBuilder;
    return this;
  }

  public MockVariableInstanceBuilder storesCustomObjects(boolean storesCustomObjects) {
    this.storesCustomObjects = storesCustomObjects;
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

  public String getExecutionId() {
    return executionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public MockSerializedValueBuilder getSerializedValueBuilder() {
    return serializedValueBuilder;
  }

  public boolean isStoresCustomObjects() {
    return storesCustomObjects;
  }

  public VariableInstance build() {
    VariableInstance mockVariable = mock(VariableInstance.class);
    return build(mockVariable);
  }

  public VariableInstanceEntity buildEntity() {
    VariableInstanceEntity mockVariable = mock(VariableInstanceEntity.class);
    if (taskId != null) {
      when(mockVariable.getVariableScope()).thenReturn(taskId);
    }
    else if (executionId != null) {
      when(mockVariable.getVariableScope()).thenReturn(executionId);
    }
    else {
      when(mockVariable.getVariableScope()).thenReturn(caseExecutionId);
    }
    return build(mockVariable);
  }

  protected <T extends VariableInstance> T build(T mockVariable) {
    when(mockVariable.getId()).thenReturn(id);
    when(mockVariable.getName()).thenReturn(name);
    when(mockVariable.getTypeName()).thenReturn(type);
    when(mockVariable.getValueTypeName()).thenReturn(valueTypeName);
    when(mockVariable.getValue()).thenReturn(value);
    when(mockVariable.getProcessInstanceId()).thenReturn(processInstanceId);
    when(mockVariable.getExecutionId()).thenReturn(executionId);
    when(mockVariable.getCaseInstanceId()).thenReturn(caseInstanceId);
    when(mockVariable.getCaseExecutionId()).thenReturn(caseExecutionId);
    when(mockVariable.getTaskId()).thenReturn(taskId);
    when(mockVariable.getActivityInstanceId()).thenReturn(activityInstanceId);
    when(mockVariable.getErrorMessage()).thenReturn(errorMessage);
    when(mockVariable.storesCustomObjects()).thenReturn(storesCustomObjects);

    if (serializedValueBuilder != null) {
      SerializedVariableValue mockSerializedValue = serializedValueBuilder.build();
      when(mockVariable.getSerializedValue()).thenReturn(mockSerializedValue);
    }

    return mockVariable;
  }

}
