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

import java.io.Serializable;
import java.util.Collection;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class GetExecutionVariablesCmd implements Command<VariableMap>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String executionId;
  protected Collection<String> variableNames;
  protected boolean isLocal;
  protected boolean deserializeValues;

  public GetExecutionVariablesCmd(String executionId, Collection<String> variableNames, boolean isLocal, boolean deserializeValues) {
    this.executionId = executionId;
    this.variableNames = variableNames;
    this.isLocal = isLocal;
    this.deserializeValues = deserializeValues;
  }

  public VariableMap execute(CommandContext commandContext) {
    ensureNotNull("executionId", executionId);

    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(executionId);

    ensureNotNull("execution " + executionId + " doesn't exist", "execution", execution);

    checkGetExecutionVariables(execution, commandContext);

    VariableMapImpl executionVariables = new VariableMapImpl();

    // collect variables from execution
    execution.collectVariables(executionVariables, variableNames, isLocal, deserializeValues);

    return executionVariables;
  }

  protected void checkGetExecutionVariables(ExecutionEntity execution, CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadProcessInstanceVariable(execution);
    }
  }
}
