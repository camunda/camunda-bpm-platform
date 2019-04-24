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
package org.camunda.bpm.engine.impl.cmmn.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;

import org.camunda.bpm.engine.exception.cmmn.CaseExecutionNotFoundException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Roman Smirnov
 * @author Daniel Meyer
 *
 */
public class GetCaseExecutionVariableTypedCmd implements Command<TypedValue>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String caseExecutionId;
  protected String variableName;
  protected boolean isLocal;
  protected boolean deserializeValue;

  public GetCaseExecutionVariableTypedCmd(String caseExecutionId, String variableName, boolean isLocal, boolean deserializeValue) {
    this.caseExecutionId = caseExecutionId;
    this.variableName = variableName;
    this.isLocal = isLocal;
    this.deserializeValue = deserializeValue;
  }

  public TypedValue execute(CommandContext commandContext) {
    ensureNotNull("caseExecutionId", caseExecutionId);
    ensureNotNull("variableName", variableName);

    CaseExecutionEntity caseExecution = commandContext
      .getCaseExecutionManager()
      .findCaseExecutionById(caseExecutionId);

    ensureNotNull(CaseExecutionNotFoundException.class, "case execution " + caseExecutionId + " doesn't exist", "caseExecution", caseExecution);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadCaseInstance(caseExecution);
    }

    TypedValue value;

    if (isLocal) {
      value = caseExecution.getVariableLocalTyped(variableName, deserializeValue);
    } else {
      value = caseExecution.getVariableTyped(variableName, deserializeValue);
    }

    return value;
  }

}
