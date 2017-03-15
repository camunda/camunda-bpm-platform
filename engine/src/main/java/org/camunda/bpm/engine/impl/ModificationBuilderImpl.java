package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.cmd.ActivityAfterInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityBeforeInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.ModifyMultipleProcessInstancesBatchCmd;
import org.camunda.bpm.engine.impl.cmd.ModifyMultipleProcessInstancesCmd;
import org.camunda.bpm.engine.impl.cmd.TransitionInstantiationCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

public class ModificationBuilderImpl implements ModificationBuilder {

  protected CommandExecutor commandExecutor;
  protected ProcessInstanceQuery processInstanceQuery;
  protected List<String> processInstanceIds;
  protected List<AbstractProcessInstanceModificationCommand> instructions;

  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;

  public ModificationBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    processInstanceIds = new ArrayList<String>();
    instructions = new ArrayList<AbstractProcessInstanceModificationCommand>();
  }

  @Override
  public ModificationBuilder startBeforeActivity(String activityId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    instructions.add(new ActivityBeforeInstantiationCmd(null, activityId));
    return this;
  }

  @Override
  public ModificationBuilder startAfterActivity(String activityId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    instructions.add(new ActivityAfterInstantiationCmd(null, activityId));
    return this;
  }

  @Override
  public ModificationBuilder startTransition(String transitionId) {
    ensureNotNull(NotValidException.class, "activityId", transitionId);
    instructions.add(new TransitionInstantiationCmd(null, transitionId));
    return this;
  }

  @Override
  public ModificationBuilder cancelAllForActivity(String activityId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    instructions.add(new ActivityCancellationCmd(null, activityId));
    return this;
  }

  @Override
  public ModificationBuilder processInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  @Override
  public ModificationBuilder processInstanceIds(String... processInstanceIds) {
    ensureNotNull(BadUserRequestException.class, "Process instance ids", processInstanceIds);
    this.processInstanceIds = Arrays.asList(processInstanceIds);
    return this;
  }

  @Override
  public ModificationBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
    return this;
  }

  @Override
  public ModificationBuilder skipCustomListeners() {
    this.skipCustomListeners = true;
    return this;
  }

  @Override
  public ModificationBuilder skipIoMappings() {
    this.skipIoMappings = true;
    return this;
  }

  public void execute(boolean writeUserOperationLog) {
    commandExecutor.execute(new ModifyMultipleProcessInstancesCmd(this, writeUserOperationLog));
  }

  @Override
  public void execute() {
    execute(true);
  }

  @Override
  public Batch executeAsync() {
    return commandExecutor.execute(new ModifyMultipleProcessInstancesBatchCmd(this));
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public ProcessInstanceQuery getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public List<AbstractProcessInstanceModificationCommand> getInstructions() {
    return instructions;
  }

  @Override
  public void setInstructions(List<AbstractProcessInstanceModificationCommand> instructions) {
    this.instructions = instructions;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

}
