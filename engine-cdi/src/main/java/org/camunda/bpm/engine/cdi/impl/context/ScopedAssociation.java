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
package org.camunda.bpm.engine.cdi.impl.context;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.cdi.ProcessEngineCdiException;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

import javax.inject.Inject;

public class ScopedAssociation {

  @Inject
  private RuntimeService runtimeService;

  @Inject
  private TaskService taskService;

  protected VariableMap cachedVariables = new VariableMapImpl();
  protected VariableMap cachedVariablesLocal = new VariableMapImpl();
  protected Execution execution;
  protected Task task;

  public Execution getExecution() {
    return execution;
  }

  public void setExecution(Execution execution) {
    this.execution = execution;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  @SuppressWarnings("unchecked")
  public <T extends TypedValue> T getVariable(String variableName) {
    TypedValue value = cachedVariables.getValueTyped(variableName);
    if(value == null) {
      if(execution != null) {
        value = runtimeService.getVariableTyped(execution.getId(), variableName);
        cachedVariables.put(variableName, value);
      }
    }
    return (T) value;
  }

  public void setVariable(String variableName, Object value) {
    cachedVariables.put(variableName, value);
  }

  public VariableMap getCachedVariables() {
    return cachedVariables;
  }

  @SuppressWarnings("unchecked")
  public <T extends TypedValue> T getVariableLocal(String variableName) {
    TypedValue value = cachedVariablesLocal.getValueTyped(variableName);
    if (value == null) {
      if (task != null) {
        value = taskService.getVariableLocalTyped(task.getId(), variableName);
        cachedVariablesLocal.put(variableName, value);
      } else if (execution != null) {
        value = runtimeService.getVariableLocalTyped(execution.getId(), variableName);
        cachedVariablesLocal.put(variableName, value);
      }
    }
    return (T) value;
  }

  public void setVariableLocal(String variableName, Object value) {
    if (execution == null && task == null) {
      throw new ProcessEngineCdiException("Cannot set a local cached variable: neither a Task nor an Execution is associated.");
    }
    cachedVariablesLocal.put(variableName, value);
  }

  public VariableMap getCachedVariablesLocal() {
    return cachedVariablesLocal;
  }

  public void flushVariableCache() {
    if(task != null) {
      taskService.setVariablesLocal(task.getId(), cachedVariablesLocal);
      taskService.setVariables(task.getId(), cachedVariables);

    } else if(execution != null) {
      runtimeService.setVariablesLocal(execution.getId(), cachedVariablesLocal);
      runtimeService.setVariables(execution.getId(), cachedVariables);

    } else {
      throw new ProcessEngineCdiException("Cannot flush variable cache: neither a Task nor an Execution is associated.");

    }

    // clear variable cache after flush
    cachedVariables.clear();
    cachedVariablesLocal.clear();
  }

}
