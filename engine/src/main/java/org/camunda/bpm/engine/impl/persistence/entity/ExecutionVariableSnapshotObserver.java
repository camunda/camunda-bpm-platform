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

  protected boolean localVariables = true;
  protected boolean deserializeValues = false;

  public ExecutionVariableSnapshotObserver(ExecutionEntity executionEntity) {
    this(executionEntity, true, false);
  }

  public ExecutionVariableSnapshotObserver(ExecutionEntity executionEntity, boolean localVariables, boolean deserializeValues) {
    this.execution = executionEntity;
    this.execution.addExecutionObserver(this);
    this.localVariables = localVariables;
    this.deserializeValues = deserializeValues;
  }

  @Override
  public void onClear(ExecutionEntity execution) {
    if (variableSnapshot == null) {
      variableSnapshot = getVariables(this.localVariables);
    }
  }

  public VariableMap getVariables() {
    if (variableSnapshot == null) {
      return getVariables(this.localVariables);
    } else {
      return variableSnapshot;
    }
  }

  private VariableMap getVariables(final boolean localVariables) {
    return this.localVariables ? execution.getVariablesLocalTyped(deserializeValues) : execution.getVariablesTyped(deserializeValues);
  }
}
