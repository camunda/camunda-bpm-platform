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
package org.camunda.bpm.engine.impl.variable.listener;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.variable.value.TypedValue;
/**
 * @author Thorben Lindhauer
 *
 */
public class DelegateCaseVariableInstanceImpl implements DelegateCaseVariableInstance {

  protected String eventName;
  protected DelegateCaseExecution sourceExecution;
  protected DelegateCaseExecution scopeExecution;

  // fields copied from variable instance
  protected String variableId;
  protected String processInstanceId;
  protected String executionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String taskId;
  protected String activityInstanceId;
  protected String tenantId;
  protected String errorMessage;
  protected String name;
  protected TypedValue value;

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public DelegateCaseExecution getSourceExecution() {
    return sourceExecution;
  }

  public void setSourceExecution(DelegateCaseExecution sourceExecution) {
    this.sourceExecution = sourceExecution;
  }

  /**
   * Currently not part of public interface.
   */
  public DelegateCaseExecution getScopeExecution() {
    return scopeExecution;
  }

  public void setScopeExecution(DelegateCaseExecution scopeExecution) {
    this.scopeExecution = scopeExecution;
  }

  //// methods delegated to wrapped variable ////

  public String getId() {
    return variableId;
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

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getTypeName() {
    if(value != null) {
      return value.getType().getName();
    }
    else {
      return null;
    }
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    if(value != null) {
      return value.getValue();
    }
    else {
      return null;
    }
  }

  public TypedValue getTypedValue() {
    return value;
  }

  public ProcessEngineServices getProcessEngineServices() {
    return Context.getProcessEngineConfiguration().getProcessEngine();
  }

  public static DelegateCaseVariableInstanceImpl fromVariableInstance(VariableInstance variableInstance) {
    DelegateCaseVariableInstanceImpl delegateInstance = new DelegateCaseVariableInstanceImpl();
    delegateInstance.variableId = variableInstance.getId();
    delegateInstance.processInstanceId = variableInstance.getProcessInstanceId();
    delegateInstance.executionId = variableInstance.getExecutionId();
    delegateInstance.caseExecutionId = variableInstance.getCaseExecutionId();
    delegateInstance.caseInstanceId = variableInstance.getCaseInstanceId();
    delegateInstance.taskId = variableInstance.getTaskId();
    delegateInstance.activityInstanceId = variableInstance.getActivityInstanceId();
    delegateInstance.tenantId = variableInstance.getTenantId();
    delegateInstance.errorMessage = variableInstance.getErrorMessage();
    delegateInstance.name = variableInstance.getName();
    delegateInstance.value = variableInstance.getTypedValue();

    return delegateInstance;
  }

}