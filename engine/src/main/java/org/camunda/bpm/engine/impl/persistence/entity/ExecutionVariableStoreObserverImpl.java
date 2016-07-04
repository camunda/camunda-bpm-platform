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
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * Represents an implementation of the the variable store observer interface,
 * which observes the used variables.
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ExecutionVariableStoreObserverImpl implements VariableStoreObserver<VariableInstanceEntity> {

  /**
   * The variables which are observed during the execution.
   */
  protected final VariableMap variables;

  public ExecutionVariableStoreObserverImpl(VariableMap variables) {
    this.variables = variables;
  }

  @Override
  public void onAdd(VariableInstanceEntity variable) {
    variables.putValueTyped(variable.getName(), variable.getTypedValue(false));
  }

  @Override
  public void onUpdate(VariableInstanceEntity variable) {
    variables.putValueTyped(variable.getName(), variable.getTypedValue(false));
  }

  @Override
  public void onRemove(VariableInstanceEntity variable) {
    variables.remove(variable.getName());
  }
}
