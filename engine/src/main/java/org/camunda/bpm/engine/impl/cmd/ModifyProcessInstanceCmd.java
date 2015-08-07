/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.cmd;


import java.util.List;
import java.util.logging.Logger;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

/**
 * @author Thorben Lindhauer
 *
 */
public class ModifyProcessInstanceCmd implements Command<Void> {

  private static final Logger LOG = Logger.getLogger(ModifyProcessInstanceCmd.class.getName());
  protected static final String INSTRUCTION_LOG_FORMAT = "Modifying process instance '%s': Instruction %s: %s";

  protected ProcessInstanceModificationBuilderImpl builder;

  public ModifyProcessInstanceCmd(ProcessInstanceModificationBuilderImpl processInstanceModificationBuilder) {
    this.builder = processInstanceModificationBuilder;
  }

  public Void execute(CommandContext commandContext) {
    String processInstanceId = builder.getProcessInstanceId();

    ExecutionManager executionManager = commandContext.getExecutionManager();
    ExecutionEntity processInstance = executionManager.findExecutionById(processInstanceId);

    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkUpdateProcessInstance(processInstance);

    processInstance.setPreserveScope(true);

    List<AbstractProcessInstanceModificationCommand> instructions = builder.getModificationOperations();

    for (int i = 0; i < instructions.size(); i++) {
      AbstractProcessInstanceModificationCommand instruction = instructions.get(i);
      logInstruction(processInstanceId, i, instruction);

      instruction.setSkipCustomListeners(builder.isSkipCustomListeners());
      instruction.setSkipIoMappings(builder.isSkipIoMappings());
      instruction.execute(commandContext);
    }

    processInstance = executionManager.findExecutionById(processInstanceId);

    if (!processInstance.hasChildren()) {
      if (processInstance.getActivity() == null) {
        // process instance was cancelled
        authorizationManager.checkDeleteProcessInstance(processInstance);
        processInstance.deleteCascade("Cancellation due to process instance modification", builder.isSkipCustomListeners(), builder.isSkipIoMappings());
      }
      else if (processInstance.isEnded()) {
        // process instance has ended regularly
        processInstance.propagateEnd();
      }
    }

    commandContext.getOperationLogManager().logProcessInstanceOperation(getLogEntryOperation(), processInstanceId, null, null, PropertyChange.EMPTY_CHANGE);

    return null;
  }

  protected void logInstruction(String processInstanceId, int index, AbstractProcessInstanceModificationCommand instruction) {
    LOG.info(String.format(INSTRUCTION_LOG_FORMAT, processInstanceId, index + 1, instruction.describe()));
  }

  protected String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE;
  }
}
