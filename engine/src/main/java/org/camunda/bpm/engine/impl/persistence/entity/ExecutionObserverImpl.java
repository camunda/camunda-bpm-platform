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

import org.camunda.bpm.engine.impl.core.variable.scope.VariableStore.VariableStoreObserver;

/**
 * Represents an execution observer, which is used to remove
 * the variable store observer in the remove method of the execution.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ExecutionObserverImpl implements ExecutionObserver {

  /**
   * The variable store observer which should be removed from the execution
   * if the onRemove is called.
   */
  protected final VariableStoreObserver variableStoreObserver;

  public ExecutionObserverImpl(VariableStoreObserver observer) {
    this.variableStoreObserver = observer;
  }

  @Override
  public void onRemove(ExecutionEntity execution) {
    execution.removeVariableStoreObserver(variableStoreObserver);
  }
}
