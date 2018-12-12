/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


public class FoxDeleteProcessInstanceCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processInstanceId;
  protected String deleteReason;

  public FoxDeleteProcessInstanceCmd(String processInstanceId, String deleteReason) {
    this.processInstanceId = processInstanceId;
    this.deleteReason = deleteReason;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull("processInstanceId", processInstanceId);

    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(processInstanceId);

    ensureNotNull("No process instance found for id '" + processInstanceId + "'", "execution", execution);

    commandContext
      .getTaskManager()
      .deleteTasksByProcessInstanceId(processInstanceId, deleteReason, false, false);

    for (PvmExecutionImpl currentExecution : this.collectExecutionToDelete(execution)) {
      currentExecution.deleteCascade2(deleteReason);
    }
    return null;
  }

  public List<PvmExecutionImpl> collectExecutionToDelete(PvmExecutionImpl execution) {
    List<PvmExecutionImpl> result = new ArrayList<PvmExecutionImpl>();
    for (PvmExecutionImpl currentExecution : execution.getExecutions()) {
      result.addAll(this.collectExecutionToDelete(currentExecution));
    }
    if (execution.getSubProcessInstance() != null) {
      result.addAll(this.collectExecutionToDelete(execution.getSubProcessInstance()));
    }
    result.add(execution);
    return result;
  }

}
