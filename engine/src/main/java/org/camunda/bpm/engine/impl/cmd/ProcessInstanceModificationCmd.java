package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

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

  public ProcessInstanceModificationCmd(ModificationBuilderImpl modificationBuilder, boolean writeUserOperationLog) {
    super(modificationBuilder);
    this.writeUserOperationLog = writeUserOperationLog;
   }

  @Override
  public Void execute(final CommandContext commandContext) {
    List<AbstractProcessInstanceModificationCommand> instructions = builder.getInstructions();
    final Collection<String> processInstanceIds = collectProcessInstanceIds(commandContext);

    ensureNotEmpty(BadUserRequestException.class, "Modification instructions cannot be empty", instructions);
    ensureNotEmpty(BadUserRequestException.class, "Process instance ids cannot be empty", "Process instance ids", processInstanceIds);
    ensureNotContainsNull(BadUserRequestException.class, "Process instance ids cannot be null", "Process instance ids", processInstanceIds);

    ProcessDefinitionEntity processDefinition = getProcessDefinition(commandContext, builder.getProcessDefinitionId());
    ensureNotNull(BadUserRequestException.class, "Process definition id cannot be null", processDefinition);

    if (writeUserOperationLog) {
      writeUserOperationLog(commandContext, processDefinition, processInstanceIds.size(), false);
    }

    boolean skipCustomListeners = builder.isSkipCustomListeners();
    boolean skipIoMappings = builder.isSkipIoMappings();

    for (String processInstanceId : processInstanceIds) {
      ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);

      ensureProcessInstanceExist(processInstanceId, processInstance);
      ensureSameProcessDefinition(processInstance, processDefinition.getId());

      ProcessInstanceModificationBuilderImpl builder = createProcessInstanceModificationBuilder(processInstanceId, commandContext);
      builder.execute(false, skipCustomListeners, skipIoMappings);
    }

    return null;
  }

  protected void ensureSameProcessDefinition(ExecutionEntity processInstance, String processDefinitionId) {
    if (!processDefinitionId.equals(processInstance.getProcessDefinitionId())) {
      throw LOG.processDefinitionOfInstanceDoesNotMatchModification(processInstance, processDefinitionId);
    }
  }

  protected void ensureProcessInstanceExist(String processInstanceId, ExecutionEntity processInstance) {
    if (processInstance == null) {
      throw LOG.processInstanceDoesNotExist(processInstanceId);
    }
  }

  protected ProcessInstanceModificationBuilderImpl createProcessInstanceModificationBuilder(final String processInstanceId, final CommandContext commandContext) {
    ProcessInstanceModificationBuilderImpl processInstanceModificationBuilder = new ProcessInstanceModificationBuilderImpl(commandContext, processInstanceId);
    List<AbstractProcessInstanceModificationCommand> operations = processInstanceModificationBuilder.getModificationOperations();

    ActivityInstance activityInstanceTree = null;

    for (AbstractProcessInstanceModificationCommand instruction : builder.getInstructions()) {

      instruction.setProcessInstanceId(processInstanceId);

      if (!(instruction instanceof ActivityCancellationCmd) || !((ActivityCancellationCmd)instruction).isCancelCurrentActiveActivityInstances()) {
        operations.add(instruction);
      }
      else {

        if (activityInstanceTree == null) {
          activityInstanceTree = commandContext.runWithoutAuthorization(new Callable<ActivityInstance>() {
            @Override
            public ActivityInstance call() throws Exception {
              return new GetActivityInstanceCmd(processInstanceId).execute(commandContext);
            }
          });
        }

        ActivityCancellationCmd cancellationInstruction = (ActivityCancellationCmd) instruction;
        List<AbstractInstanceCancellationCmd> cmds = cancellationInstruction.createActivityInstanceCancellations(activityInstanceTree, commandContext);
        operations.addAll(cmds);
      }

    }

    return processInstanceModificationBuilder;
  }

}
