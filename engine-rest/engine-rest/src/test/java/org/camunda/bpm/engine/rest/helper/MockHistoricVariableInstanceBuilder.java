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
package org.camunda.bpm.engine.rest.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

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
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String executionId;
  protected String errorMessage;
  protected String activityInstanceId;
  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String taskId;
  protected String tenantId;
  protected Date createTime;
  protected Date removalTime;
  protected String rootProcessInstanceId;

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

  public MockHistoricVariableInstanceBuilder processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public MockHistoricVariableInstanceBuilder processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public MockHistoricVariableInstanceBuilder processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public MockHistoricVariableInstanceBuilder executionId(String executionId) {
    this.executionId = executionId;
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

  public MockHistoricVariableInstanceBuilder caseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
    return this;
  }

  public MockHistoricVariableInstanceBuilder caseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  public MockHistoricVariableInstanceBuilder caseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public MockHistoricVariableInstanceBuilder caseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  public MockHistoricVariableInstanceBuilder taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  public MockHistoricVariableInstanceBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public MockHistoricVariableInstanceBuilder createTime(Date createTime) {
    this.createTime = createTime;
    return this;
  }

  public MockHistoricVariableInstanceBuilder removalTime(Date removalTime) {
    this.removalTime = removalTime;
    return this;
  }

  public MockHistoricVariableInstanceBuilder rootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
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

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
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

  public String getTenantId() {
    return tenantId;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
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
    when(mockVariable.getProcessDefinitionKey()).thenReturn(processDefinitionKey);
    when(mockVariable.getProcessDefinitionId()).thenReturn(processDefinitionId);
    when(mockVariable.getProcessInstanceId()).thenReturn(processInstanceId);
    when(mockVariable.getExecutionId()).thenReturn(executionId);
    when(mockVariable.getErrorMessage()).thenReturn(errorMessage);
    when(mockVariable.getActivtyInstanceId()).thenReturn(activityInstanceId);
    when(mockVariable.getActivityInstanceId()).thenReturn(activityInstanceId);
    when(mockVariable.getCaseDefinitionKey()).thenReturn(caseDefinitionKey);
    when(mockVariable.getCaseDefinitionId()).thenReturn(caseDefinitionId);
    when(mockVariable.getCaseInstanceId()).thenReturn(caseInstanceId);
    when(mockVariable.getCaseExecutionId()).thenReturn(caseExecutionId);
    when(mockVariable.getTaskId()).thenReturn(taskId);
    when(mockVariable.getTenantId()).thenReturn(tenantId);
    when(mockVariable.getCreateTime()).thenReturn(createTime);
    when(mockVariable.getRemovalTime()).thenReturn(removalTime);
    when(mockVariable.getRootProcessInstanceId()).thenReturn(rootProcessInstanceId);

    return mockVariable;
  }

}
