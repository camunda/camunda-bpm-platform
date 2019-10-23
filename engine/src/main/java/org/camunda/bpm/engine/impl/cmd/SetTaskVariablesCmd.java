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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableInstanceLifecycleListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SetTaskVariablesCmd extends AbstractSetVariableCmd implements VariableInstanceLifecycleListener<VariableInstanceEntity> {

  private static final long serialVersionUID = 1L;

  protected boolean taskLocalVariablesUpdated = false;


  public SetTaskVariablesCmd(String taskId, Map<String, ? extends Object> variables, boolean isLocal) {
    super(taskId, variables, isLocal);
  }

  protected TaskEntity getEntity() {
    ensureNotNull("taskId", entityId);

    TaskEntity task =  commandContext
      .getTaskManager()
      .findTaskById(entityId);

    ensureNotNull("task " + entityId + " doesn't exist", "task", task);

    checkSetTaskVariables(task);

    task.addCustomLifecycleListener(this);

    return task;
  }

  @Override
  protected void onSuccess(AbstractVariableScope scope) {
    TaskEntity task = (TaskEntity) scope;

    if (taskLocalVariablesUpdated) {
      task.triggerUpdateEvent();
    }

    task.removeCustomLifecycleListener(this);

    super.onSuccess(scope);
  }

  @Override
  protected ExecutionEntity getContextExecution() {
    return getEntity().getExecution();
  }

  protected void logVariableOperation(AbstractVariableScope scope) {
    TaskEntity task = (TaskEntity) scope;
    commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), null, task.getId(),
      PropertyChange.EMPTY_CHANGE);
  }

  protected void checkSetTaskVariables(TaskEntity task) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateTaskVariable(task);
    }
  }

  protected void onLocalVariableChanged() {
    taskLocalVariablesUpdated = true;
  }

  @Override
  public void onCreate(VariableInstanceEntity variableInstance, AbstractVariableScope sourceScope) {
    onLocalVariableChanged();
  }

  @Override
  public void onDelete(VariableInstanceEntity variableInstance, AbstractVariableScope sourceScope) {
    onLocalVariableChanged();
  }

  @Override
  public void onUpdate(VariableInstanceEntity variableInstance, AbstractVariableScope sourceScope) {
    onLocalVariableChanged();
  }
}
