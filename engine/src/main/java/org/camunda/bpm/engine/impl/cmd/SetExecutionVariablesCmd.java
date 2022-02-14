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
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SetExecutionVariablesCmd extends AbstractSetVariableCmd {

  private static final long serialVersionUID = 1L;

  public SetExecutionVariablesCmd(String executionId,
                                  Map<String, ?> variables,
                                  boolean isLocal,
                                  boolean skipJavaSerializationFormatCheck) {
    super(executionId, variables, isLocal, skipJavaSerializationFormatCheck);
  }

  public SetExecutionVariablesCmd(String executionId, Map<String, ? extends Object> variables, boolean isLocal) {
    super(executionId, variables, isLocal, false);
  }

  protected ExecutionEntity getEntity() {
    ensureNotNull("executionId", entityId);

    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(entityId);

    ensureNotNull("execution " + entityId + " doesn't exist", "execution", execution);

    checkSetExecutionVariables(execution);

    return execution;
  }

  @Override
  protected ExecutionEntity getContextExecution() {
    return getEntity();
  }

  protected void logVariableOperation(AbstractVariableScope scope) {
    ExecutionEntity execution = (ExecutionEntity) scope;
    commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), execution.getId(),
        null, PropertyChange.EMPTY_CHANGE);
  }

  protected void checkSetExecutionVariables(ExecutionEntity execution) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateProcessInstanceVariables(execution);
    }
  }
}

