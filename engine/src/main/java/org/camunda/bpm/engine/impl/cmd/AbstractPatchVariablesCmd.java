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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Stefan Hentschel.
 */
public abstract class AbstractPatchVariablesCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String entityId;
  protected Map<String, ? extends Object> variables;
  protected Collection<String> deletions;
  protected boolean isLocal;

  public AbstractPatchVariablesCmd(String entityId, Map<String, ? extends Object> variables, Collection<String> deletions, boolean isLocal) {
    this.entityId = entityId;
    this.variables = variables;
    this.deletions = deletions;
    this.isLocal = isLocal;
  }

  public Void execute(CommandContext commandContext) {
    getSetVariableCmd().disableLogUserOperation().execute(commandContext);
    getRemoveVariableCmd().disableLogUserOperation().execute(commandContext);
    logVariableOperation(commandContext);
    return null;
  }

  protected String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_MODIFY_VARIABLE;
  }

  protected abstract AbstractSetVariableCmd getSetVariableCmd();

  protected abstract AbstractRemoveVariableCmd getRemoveVariableCmd();

  protected abstract void logVariableOperation(CommandContext commandContext);

}
