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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Collection;
import java.util.List;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.ModificationBuilderImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.runtime.ActivityInstance;

public class ProcessInstanceModificationCmd extends AbstractModificationCmd<Void> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;
  protected boolean writeUserOperationLog;

  public ProcessInstanceModificationCmd(ModificationBuilderImpl builder,
                                        boolean writeUserOperationLog) {
    super(builder);
    this.writeUserOperationLog = writeUserOperationLog;
   }

  @Override
  public Void execute(final CommandContext commandContext) {
    List<AbstractProcessInstanceModificationCommand> instructions = builder.getInstructions();
    ensureNotEmpty(BadUserRequestException.class,
        "Modification instructions cannot be empty", instructions);

    Collection<String> processInstanceIds = collectProcessInstanceIds();
    ensureNotEmpty(BadUserRequestException.class,
        "Process instance ids cannot be empty", "Process instance ids", processInstanceIds);

    ensureNotContainsNull(BadUserRequestException.class,
        "Process instance ids cannot be null", "Process instance ids", processInstanceIds);

    ProcessDefinitionEntity processDefinition =
        getProcessDefinition(commandContext, builder.getProcessDefinitionId());

    ensureNotNull(BadUserRequestException.class, "Process definition id cannot be null", processDefinition);

    if (writeUserOperationLog) {
      String annotation = builder.getAnnotation();
      writeUserOperationLog(commandContext, processDefinition,
          processInstanceIds.size(), false, annotation);
    }

    boolean skipCustomListeners = builder.isSkipCustomListeners();
    boolean skipIoMappings = builder.isSkipIoMappings();

    for (String processInstanceId : processInstanceIds) {
      ExecutionEntity processInstance = commandContext.getExecutionManager()
          .findExecutionById(processInstanceId);

      ensureProcessInstanceExist(processInstanceId, processInstance);
      ensureSameProcessDefinition(processInstance, processDefinition.getId());

      ProcessInstanceModificationBuilderImpl builder =
          createProcessInstanceModificationBuilder(processInstanceId, commandContext);
      builder.execute(false, skipCustomListeners, skipIoMappings);
    }

    return null;
  }

  protected void ensureSameProcessDefinition(ExecutionEntity processInstance,
                                             String processDefinitionId) {
    if (!processDefinitionId.equals(processInstance.getProcessDefinitionId())) {
      throw LOG.processDefinitionOfInstanceDoesNotMatchModification(processInstance,
          processDefinitionId);
    }
  }

  protected void ensureProcessInstanceExist(String processInstanceId,
                                            ExecutionEntity processInstance) {
    if (processInstance == null) {
      throw LOG.processInstanceDoesNotExist(processInstanceId);
    }
  }

  protected ProcessInstanceModificationBuilderImpl createProcessInstanceModificationBuilder(
      String processInstanceId, CommandContext commandContext) {

    ProcessInstanceModificationBuilderImpl processInstanceModificationBuilder =
        new ProcessInstanceModificationBuilderImpl(commandContext, processInstanceId);

    List<AbstractProcessInstanceModificationCommand> operations =
        processInstanceModificationBuilder.getModificationOperations();

    ActivityInstance activityInstanceTree = null;

    for (AbstractProcessInstanceModificationCommand instruction : builder.getInstructions()) {

      instruction.setProcessInstanceId(processInstanceId);

      if (!(instruction instanceof ActivityCancellationCmd) ||
          !((ActivityCancellationCmd)instruction).isCancelCurrentActiveActivityInstances()) {
        operations.add(instruction);
      }
      else {

        if (activityInstanceTree == null) {
          activityInstanceTree = commandContext.runWithoutAuthorization(new GetActivityInstanceCmd(processInstanceId));
        }

        ActivityCancellationCmd cancellationInstruction = (ActivityCancellationCmd) instruction;
        List<AbstractInstanceCancellationCmd> cmds =
            cancellationInstruction.createActivityInstanceCancellations(activityInstanceTree,
                commandContext);

        operations.addAll(cmds);
      }

    }

    return processInstanceModificationBuilder;
  }

}
