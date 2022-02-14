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


import java.util.Collections;
import java.util.List;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.ModificationUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class ModifyProcessInstanceCmd implements Command<Void> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected ProcessInstanceModificationBuilderImpl builder;
  protected boolean writeOperationLog;

  public ModifyProcessInstanceCmd(ProcessInstanceModificationBuilderImpl processInstanceModificationBuilder) {
    this(processInstanceModificationBuilder, true);
  }

  public ModifyProcessInstanceCmd(ProcessInstanceModificationBuilderImpl processInstanceModificationBuilder, boolean writeOperationLog) {
    this.builder = processInstanceModificationBuilder;
    this.writeOperationLog = writeOperationLog;
  }


  @Override
  public Void execute(CommandContext commandContext) {
    String processInstanceId = builder.getProcessInstanceId();

    ExecutionManager executionManager = commandContext.getExecutionManager();
    ExecutionEntity processInstance = executionManager.findExecutionById(processInstanceId);

    ensureProcessInstanceExist(processInstanceId, processInstance);

    checkUpdateProcessInstance(processInstance, commandContext);

    processInstance.setPreserveScope(true);

    List<AbstractProcessInstanceModificationCommand> instructions = builder.getModificationOperations();

    checkCancellation(commandContext);
    for (int i = 0; i < instructions.size(); i++) {

      AbstractProcessInstanceModificationCommand instruction = instructions.get(i);
      LOG.debugModificationInstruction(processInstanceId, i + 1, instruction.describe());

      instruction.setSkipCustomListeners(builder.isSkipCustomListeners());
      instruction.setSkipIoMappings(builder.isSkipIoMappings());
      instruction.setExternallyTerminated(builder.isExternallyTerminated());
      instruction.execute(commandContext);
    }

    processInstance = executionManager.findExecutionById(processInstanceId);

    if (!processInstance.hasChildren() && !processInstance.isCanceled() && !processInstance.isRemoved()) {
      if (processInstance.getActivity() == null) {
        // process instance was cancelled
        checkDeleteProcessInstance(processInstance, commandContext);
        deletePropagate(processInstance, builder.getModificationReason(), builder.isSkipCustomListeners(), builder.isSkipIoMappings(),
            builder.isExternallyTerminated());
      }
      else if (processInstance.isEnded()) {
        // process instance has ended regularly
        processInstance.propagateEnd();
      }
    }

    if (writeOperationLog) {
      commandContext.getOperationLogManager().logProcessInstanceOperation(getLogEntryOperation(),
        processInstanceId,
        null,
        null,
        Collections.singletonList(PropertyChange.EMPTY_CHANGE),
        builder.getAnnotation());
    }

    return null;
  }

  private void checkCancellation(final CommandContext commandContext) {
    for (final AbstractProcessInstanceModificationCommand instruction : builder.getModificationOperations()) {
      if (instruction instanceof ActivityCancellationCmd
          && ((ActivityCancellationCmd) instruction).cancelCurrentActiveActivityInstances) {
        ActivityInstance activityInstanceTree = commandContext.runWithoutAuthorization(
            new GetActivityInstanceCmd(((ActivityCancellationCmd) instruction).processInstanceId));
        ((ActivityCancellationCmd) instruction).setActivityInstanceTreeToCancel(activityInstanceTree);
      }
    }
  }

  protected void ensureProcessInstanceExist(String processInstanceId, ExecutionEntity processInstance) {
    if (processInstance == null) {
      throw LOG.processInstanceDoesNotExist(processInstanceId);
    }
  }

  protected String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE;
  }

  protected void checkUpdateProcessInstance(ExecutionEntity execution, CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateProcessInstance(execution);
    }
  }

  protected void checkDeleteProcessInstance(ExecutionEntity execution, CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteProcessInstance(execution);
    }
  }

  protected void deletePropagate(ExecutionEntity processInstance, String deleteReason, boolean skipCustomListeners, boolean skipIoMappings, boolean externallyTerminated) {
    ExecutionEntity topmostDeletableExecution = processInstance;
    ExecutionEntity parentScopeExecution = (ExecutionEntity) topmostDeletableExecution.getParentScopeExecution(true);

    while (parentScopeExecution != null && (parentScopeExecution.getNonEventScopeExecutions().size() <= 1)) {
        topmostDeletableExecution = parentScopeExecution;
        parentScopeExecution = (ExecutionEntity) topmostDeletableExecution.getParentScopeExecution(true);
    }

    topmostDeletableExecution.deleteCascade(deleteReason, skipCustomListeners, skipIoMappings, externallyTerminated, false);
    ModificationUtil.handleChildRemovalInScope(topmostDeletableExecution);
  }

}
