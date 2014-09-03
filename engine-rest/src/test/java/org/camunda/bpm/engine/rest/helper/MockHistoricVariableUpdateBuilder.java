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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.SerializedVariableValue;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public class MockHistoricVariableUpdateBuilder {

  protected String id;
  protected String name;
  protected String typeName;
  protected Object value;
  protected String valueTypeName;
  protected String processInstanceId;
  protected String errorMessage;
  protected String activityInstanceId;
  protected int revision;
  protected String time;
  protected String executionId;
  protected String taskId;


  protected MockSerializedValueBuilder serializedValueBuilder;
//  protected Map<String, Object> serializedValueConfiguration = new HashMap<String, Object>();
  protected boolean storesCustomObjects;

  public MockHistoricVariableUpdateBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockHistoricVariableUpdateBuilder name(String name) {
    this.name = name;
    return this;
  }

  public MockHistoricVariableUpdateBuilder typeName(String type) {
    this.typeName = type;
    return this;
  }

  public MockHistoricVariableUpdateBuilder value(Object value) {
    this.value = value;
    return this;
  }

  public MockHistoricVariableUpdateBuilder valueTypeName(String valueTypeName) {
    this.valueTypeName = valueTypeName;
    return this;
  }

  public MockHistoricVariableUpdateBuilder processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public MockHistoricVariableUpdateBuilder errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public MockHistoricVariableUpdateBuilder serializedValue(MockSerializedValueBuilder serializedValueBuilder) {
    this.serializedValueBuilder = serializedValueBuilder;
    return this;
  }

  public MockHistoricVariableUpdateBuilder storesCustomObjects(boolean storesCustomObjects) {
    this.storesCustomObjects = storesCustomObjects;
    return this;
  }

  public MockHistoricVariableUpdateBuilder activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public MockHistoricVariableUpdateBuilder executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public MockHistoricVariableUpdateBuilder taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  public MockHistoricVariableUpdateBuilder time(String time) {
    this.time = time;
    return this;
  }

  public MockHistoricVariableUpdateBuilder revision(int revision) {
    this.revision = revision;
    return this;
  }

  public HistoricVariableUpdate build() {
    HistoricVariableUpdate mockVariable = mock(HistoricVariableUpdate.class);
    when(mockVariable.getId()).thenReturn(id);
    when(mockVariable.getVariableName()).thenReturn(name);
    when(mockVariable.getVariableTypeName()).thenReturn(typeName);
    when(mockVariable.getValueTypeName()).thenReturn(valueTypeName);
    when(mockVariable.getValue()).thenReturn(value);
    when(mockVariable.getProcessInstanceId()).thenReturn(processInstanceId);
    when(mockVariable.getErrorMessage()).thenReturn(errorMessage);
    when(mockVariable.storesCustomObjects()).thenReturn(storesCustomObjects);
    when(mockVariable.getRevision()).thenReturn(revision);
    when(mockVariable.getTime()).thenReturn(DateTimeUtil.parseDate(time));
    when(mockVariable.getActivityInstanceId()).thenReturn(activityInstanceId);
    when(mockVariable.getTaskId()).thenReturn(taskId);
    when(mockVariable.getExecutionId()).thenReturn(executionId);

    if (serializedValueBuilder != null) {
      SerializedVariableValue mockSerializedValue = serializedValueBuilder.build();
      when(mockVariable.getSerializedValue()).thenReturn(mockSerializedValue);
    }

    return mockVariable;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getTypeName() {
    return typeName;
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

  public int getRevision() {
    return revision;
  }

  public String getTime() {
    return time;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getTaskId() {
    return taskId;
  }

  public MockSerializedValueBuilder getSerializedValueBuilder() {
    return serializedValueBuilder;
  }

  public boolean isStoresCustomObjects() {
    return storesCustomObjects;
  }

}
