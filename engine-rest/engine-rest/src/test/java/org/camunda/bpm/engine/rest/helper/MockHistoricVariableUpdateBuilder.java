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

import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Thorben Lindhauer
 *
 */
public class MockHistoricVariableUpdateBuilder {

  protected String id;
  protected String name;
  protected String variableInstanceId;
  protected TypedValue typedValue;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String errorMessage;
  protected String activityInstanceId;
  protected int revision;
  protected String time;
  protected String executionId;
  protected String taskId;
  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String tenantId;
  protected String userOperationId;
  protected boolean initial;

  public MockHistoricVariableUpdateBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockHistoricVariableUpdateBuilder name(String name) {
    this.name = name;
    return this;
  }

  public MockHistoricVariableUpdateBuilder variableInstanceId(String variableInstanceId) {
    this.variableInstanceId = variableInstanceId;
    return this;
  }

  public MockHistoricVariableUpdateBuilder typedValue(TypedValue value) {
    this.typedValue = value;
    return this;
  }

  public MockHistoricVariableUpdateBuilder processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public MockHistoricVariableUpdateBuilder processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
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

  public MockHistoricVariableUpdateBuilder caseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
    return this;
  }

  public MockHistoricVariableUpdateBuilder caseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  public MockHistoricVariableUpdateBuilder caseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public MockHistoricVariableUpdateBuilder caseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  public MockHistoricVariableUpdateBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public MockHistoricVariableUpdateBuilder userOperationId(String userOperationId) {
    this.userOperationId = userOperationId;
    return this;
  }

  public MockHistoricVariableUpdateBuilder initial(boolean initial) {
    this.initial = initial;
    return this;
  }

  public HistoricVariableUpdate build() {
    HistoricVariableUpdate mockVariable = mock(HistoricVariableUpdate.class);
    when(mockVariable.getId()).thenReturn(id);
    when(mockVariable.getVariableName()).thenReturn(name);
    when(mockVariable.getVariableInstanceId()).thenReturn(variableInstanceId);
    when(mockVariable.getVariableTypeName()).thenReturn(typedValue.getType().getName());

    if (ObjectValue.class.isAssignableFrom(typedValue.getClass())) {
      ObjectValue objectValue = (ObjectValue) typedValue;
      if (objectValue.isDeserialized()) {
        when(mockVariable.getValue()).thenReturn(typedValue.getValue());
      } else {
        when(mockVariable.getValue()).thenReturn(null);
      }
    } else {
      when(mockVariable.getValue()).thenReturn(typedValue.getValue());
    }

    when(mockVariable.getTypedValue()).thenReturn(typedValue);
    when(mockVariable.getProcessDefinitionKey()).thenReturn(processDefinitionKey);
    when(mockVariable.getProcessDefinitionId()).thenReturn(processDefinitionId);
    when(mockVariable.getProcessInstanceId()).thenReturn(processInstanceId);
    when(mockVariable.getErrorMessage()).thenReturn(errorMessage);
    when(mockVariable.getRevision()).thenReturn(revision);
    when(mockVariable.getTime()).thenReturn(DateTimeUtil.parseDate(time));
    when(mockVariable.getActivityInstanceId()).thenReturn(activityInstanceId);
    when(mockVariable.getTaskId()).thenReturn(taskId);
    when(mockVariable.getExecutionId()).thenReturn(executionId);
    when(mockVariable.getTypeName()).thenReturn(typedValue.getType().getName());
    when(mockVariable.getCaseDefinitionKey()).thenReturn(caseDefinitionKey);
    when(mockVariable.getCaseDefinitionId()).thenReturn(caseDefinitionId);
    when(mockVariable.getCaseInstanceId()).thenReturn(caseInstanceId);
    when(mockVariable.getCaseExecutionId()).thenReturn(caseExecutionId);
    when(mockVariable.getTenantId()).thenReturn(tenantId);
    when(mockVariable.getUserOperationId()).thenReturn(userOperationId);
    when(mockVariable.isInitial()).thenReturn(initial);

    return mockVariable;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getVariableInstanceId() {
    return variableInstanceId;
  }

  public Object getValue() {
    return typedValue.getValue();
  }

  public TypedValue getTypedValue() {
    return typedValue;
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

  public String getUserOperationId() {
    return userOperationId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public boolean isInitial() {
    return initial;
  }
}
