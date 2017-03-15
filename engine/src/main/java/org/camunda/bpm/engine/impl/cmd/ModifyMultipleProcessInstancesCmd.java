package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.ModificationBuilderImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class ModifyMultipleProcessInstancesCmd extends AbstractModificationCmd<Void> {

  protected boolean writeUserOperationLog;

  public ModifyMultipleProcessInstancesCmd(ModificationBuilderImpl modificationBuilderImpl, boolean writeUserOperationLog) {
    super(modificationBuilderImpl);
    this.writeUserOperationLog = writeUserOperationLog;
   }


  @Override
  public Void execute(final CommandContext commandContext) {
    List<AbstractProcessInstanceModificationCommand> instructions = builder.getInstructions();
    final Collection<String> processInstanceIds = collectProcessInstanceIds(commandContext);

    ensureNotEmpty(BadUserRequestException.class, "Modification instructions cannot be empty", instructions);
    ensureNotEmpty(BadUserRequestException.class, "Process instance ids cannot be empty", "Process instance ids", processInstanceIds);
    ensureNotContainsNull(BadUserRequestException.class, "Process instance ids cannot be null", "Process instance ids", processInstanceIds);

    if (writeUserOperationLog)
      writeUserOperationLog(commandContext, processInstanceIds.size(), false);


    for (String processInstanceId : processInstanceIds) {
      ProcessInstanceModificationBuilderImpl builder = new ProcessInstanceModificationBuilderImpl(commandContext, processInstanceId);
      builder.setSkipCustomListeners(this.builder.isSkipCustomListeners());
      builder.setSkipIoMappings(this.builder.isSkipIoMappings());
      List<AbstractProcessInstanceModificationCommand> commands = generateOperationCmds(instructions, processInstanceId);
      builder.setModificationOperations(commands);
      new ModifyProcessInstanceCmd(builder, false).execute(commandContext);
    }

    return null;

  }

  private List<AbstractProcessInstanceModificationCommand> generateOperationCmds(List<AbstractProcessInstanceModificationCommand> instructions, String processInstanceId) {
    for (AbstractProcessInstanceModificationCommand operationCmd : instructions) {
      operationCmd.setProcessInstanceId(processInstanceId);
      operationCmd.setSkipCustomListeners(builder.isSkipCustomListeners());
      operationCmd.setSkipIoMappings(builder.isSkipIoMappings());
    }
    return instructions;
  }

}
