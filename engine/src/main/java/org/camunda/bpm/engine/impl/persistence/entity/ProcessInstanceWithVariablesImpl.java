/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ProcessInstanceWithVariablesImpl implements ProcessInstanceWithVariables {

  protected final ExecutionEntity executionEntity;
  protected final VariableMap variables;

  public ProcessInstanceWithVariablesImpl(ExecutionEntity executionEntity, VariableMap variables) {
    this.executionEntity = executionEntity;
    this.variables = variables;
  }

  public ExecutionEntity getExecutionEntity() {
    return executionEntity;
  }

  @Override
  public VariableMap getVariables() {
    return variables;
  }

  @Override
  public String getProcessDefinitionId() {
    return executionEntity.getProcessDefinitionId();
  }

  @Override
  public String getBusinessKey() {
    return executionEntity.getBusinessKey();
  }

  @Override
  public String getCaseInstanceId() {
    return executionEntity.getCaseInstanceId();
  }

  @Override
  public boolean isSuspended() {
    return executionEntity.isSuspended();
  }

  @Override
  public String getId() {
    return executionEntity.getId();
  }

  @Override
  public boolean isEnded() {
    return executionEntity.isEnded();
  }

  @Override
  public String getProcessInstanceId() {
    return executionEntity.getProcessInstanceId();
  }

  @Override
  public String getTenantId() {
    return executionEntity.getTenantId();
  }
}
