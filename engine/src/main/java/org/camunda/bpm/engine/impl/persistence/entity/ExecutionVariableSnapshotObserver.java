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

import org.camunda.bpm.engine.variable.VariableMap;

/**
 * Provides access to the snapshot of latest variables of an execution.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ExecutionVariableSnapshotObserver implements ExecutionObserver {

  /**
   * The variables which are observed during the execution.
   */
  protected VariableMap variableSnapshot;

  protected ExecutionEntity execution;

  public ExecutionVariableSnapshotObserver(ExecutionEntity executionEntity) {
    this.execution = executionEntity;
    this.execution.addExecutionObserver(this);
  }

  @Override
  public void onClear(ExecutionEntity execution) {
    if (variableSnapshot == null)
    {
      variableSnapshot = execution.getVariablesLocalTyped(false);
    }
  }

  public VariableMap getVariables() {
    if (variableSnapshot == null)
    {
      return execution.getVariablesLocalTyped(false);
    }
    else {
      return variableSnapshot;
    }
  }
}
