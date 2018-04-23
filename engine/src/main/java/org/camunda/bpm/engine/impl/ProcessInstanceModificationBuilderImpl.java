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
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cmd.AbstractInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.cmd.ActivityAfterInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityBeforeInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityInstanceCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.ModifyProcessInstanceAsyncCmd;
import org.camunda.bpm.engine.impl.cmd.ModifyProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.TransitionInstanceCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.TransitionInstantiationCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationInstantiationBuilder;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationBuilderImpl implements ProcessInstanceModificationInstantiationBuilder {

  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected String processInstanceId;
  protected String modificationReason;

  protected boolean skipCustomListeners = false;
  protected boolean skipIoMappings = false;

  protected List<AbstractProcessInstanceModificationCommand> operations = new ArrayList<AbstractProcessInstanceModificationCommand>();

  // variables not associated with an activity that are to be set on the instance itself
  protected VariableMap processVariables = new VariableMapImpl();

  public ProcessInstanceModificationBuilderImpl(CommandExecutor commandExecutor, String processInstanceId) {
    this(processInstanceId);
    this.commandExecutor = commandExecutor;
  }

  public ProcessInstanceModificationBuilderImpl(CommandContext commandContext, String processInstanceId) {
    this(processInstanceId);
    this.commandContext = commandContext;
  }

  public ProcessInstanceModificationBuilderImpl(CommandContext commandContext, String processInstanceId, String modificationReason) {
    this(processInstanceId);
    this.commandContext = commandContext;
    this.modificationReason = modificationReason;
  }

  public ProcessInstanceModificationBuilderImpl(String processInstanceId) {
    ensureNotNull(NotValidException.class, "processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
  }

  public ProcessInstanceModificationBuilderImpl() {
  }

  @Override
  public ProcessInstanceModificationBuilder cancelActivityInstance(String activityInstanceId) {
    ensureNotNull(NotValidException.class, "activityInstanceId", activityInstanceId);
    operations.add(new ActivityInstanceCancellationCmd(processInstanceId, activityInstanceId, this.modificationReason));
    return this;
  }

  @Override
  public ProcessInstanceModificationBuilder cancelTransitionInstance(String transitionInstanceId) {
    ensureNotNull(NotValidException.class, "transitionInstanceId", transitionInstanceId);
    operations.add(new TransitionInstanceCancellationCmd(processInstanceId, transitionInstanceId));
    return this;
  }

  @Override
  public ProcessInstanceModificationBuilder cancelAllForActivity(String activityId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    operations.add(new ActivityCancellationCmd(processInstanceId, activityId));
    return this;
  }

  @Override
  public ProcessInstanceModificationInstantiationBuilder startBeforeActivity(String activityId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    AbstractInstantiationCmd currentInstantiation = new ActivityBeforeInstantiationCmd(processInstanceId, activityId);
    operations.add(currentInstantiation);
    return this;
  }

  @Override
  public ProcessInstanceModificationInstantiationBuilder startBeforeActivity(String activityId, String ancestorActivityInstanceId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    ensureNotNull(NotValidException.class, "ancestorActivityInstanceId", ancestorActivityInstanceId);
    AbstractInstantiationCmd currentInstantiation = new ActivityBeforeInstantiationCmd(processInstanceId, activityId, ancestorActivityInstanceId);
    operations.add(currentInstantiation);
    return this;
  }

  @Override
  public ProcessInstanceModificationInstantiationBuilder startAfterActivity(String activityId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    AbstractInstantiationCmd currentInstantiation = new ActivityAfterInstantiationCmd(processInstanceId, activityId);
    operations.add(currentInstantiation);
    return this;
  }

  @Override
  public ProcessInstanceModificationInstantiationBuilder startAfterActivity(String activityId, String ancestorActivityInstanceId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    ensureNotNull(NotValidException.class, "ancestorActivityInstanceId", ancestorActivityInstanceId);
    AbstractInstantiationCmd currentInstantiation = new ActivityAfterInstantiationCmd(processInstanceId, activityId, ancestorActivityInstanceId);
    operations.add(currentInstantiation);
    return this;
  }

  @Override
  public ProcessInstanceModificationInstantiationBuilder startTransition(String transitionId) {
    ensureNotNull(NotValidException.class, "transitionId", transitionId);
    AbstractInstantiationCmd currentInstantiation = new TransitionInstantiationCmd(processInstanceId, transitionId);
    operations.add(currentInstantiation);
    return this;
  }

  @Override
  public ProcessInstanceModificationInstantiationBuilder startTransition(String transitionId, String ancestorActivityInstanceId) {
    ensureNotNull(NotValidException.class, "transitionId", transitionId);
    ensureNotNull(NotValidException.class, "ancestorActivityInstanceId", ancestorActivityInstanceId);
    AbstractInstantiationCmd currentInstantiation = new TransitionInstantiationCmd(processInstanceId, transitionId, ancestorActivityInstanceId);
    operations.add(currentInstantiation);
    return this;
  }

  protected AbstractInstantiationCmd getCurrentInstantiation() {
    if (operations.isEmpty()) {
      return null;
    }

    // casting should be safe
    AbstractProcessInstanceModificationCommand lastInstantiationCmd = operations.get(operations.size() - 1);

    if (!(lastInstantiationCmd instanceof AbstractInstantiationCmd)) {
      throw new ProcessEngineException("last instruction is not an instantiation");
    }

    return (AbstractInstantiationCmd) lastInstantiationCmd;
  }

  @Override
  public ProcessInstanceModificationInstantiationBuilder setVariable(String name, Object value) {
    ensureNotNull(NotValidException.class, "Variable name must not be null", "name", name);

    AbstractInstantiationCmd currentInstantiation = getCurrentInstantiation();
    if (currentInstantiation != null) {
      currentInstantiation.addVariable(name, value);
    }
    else {
      processVariables.put(name, value);
    }

    return this;
  }

  @Override
  public ProcessInstanceModificationInstantiationBuilder setVariableLocal(String name, Object value) {
    ensureNotNull(NotValidException.class, "Variable name must not be null", "name", name);

    AbstractInstantiationCmd currentInstantiation = getCurrentInstantiation();
    if (currentInstantiation != null) {
      currentInstantiation.addVariableLocal(name, value);
    }
    else {
      processVariables.put(name, value);
    }

    return this;
  }

  @Override
  public ProcessInstanceModificationInstantiationBuilder setVariables(Map<String, Object> variables) {
    ensureNotNull(NotValidException.class, "Variable map must not be null", "variables", variables);

    AbstractInstantiationCmd currentInstantiation = getCurrentInstantiation();
    if (currentInstantiation != null) {
      currentInstantiation.addVariables(variables);
    }
    else {
      processVariables.putAll(variables);
    }
    return this;
  }

  @Override
  public ProcessInstanceModificationInstantiationBuilder setVariablesLocal(Map<String, Object> variables) {
    ensureNotNull(NotValidException.class, "Variable map must not be null", "variablesLocal", variables);

    AbstractInstantiationCmd currentInstantiation = getCurrentInstantiation();
    if (currentInstantiation != null) {
      currentInstantiation.addVariablesLocal(variables);
    }
    else {
      processVariables.putAll(variables);
    }
    return this;
  }


  @Override
  public void execute() {
    execute(false, false);
  }

  @Override
  public void execute(boolean skipCustomListeners, boolean skipIoMappings) {
    execute(true, skipCustomListeners, skipIoMappings);
  }

  public void execute(boolean writeUserOperationLog, boolean skipCustomListeners, boolean skipIoMappings) {
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;

    ModifyProcessInstanceCmd cmd = new ModifyProcessInstanceCmd(this, writeUserOperationLog);
    if (commandExecutor != null) {
      commandExecutor.execute(cmd);
    } else {
      cmd.execute(commandContext);
    }
  }

  @Override
  public Batch executeAsync() {
    return executeAsync(false, false);
  }

  @Override
  public Batch executeAsync(boolean skipCustomListeners, boolean skipIoMappings) {
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;

    return commandExecutor.execute(new ModifyProcessInstanceAsyncCmd(this));
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public CommandContext getCommandContext() {
    return commandContext;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public List<AbstractProcessInstanceModificationCommand> getModificationOperations() {
    return operations;
  }

  public void setModificationOperations(List<AbstractProcessInstanceModificationCommand> operations) {
    this.operations = operations;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public void setSkipIoMappings(boolean skipIoMappings) {
    this.skipIoMappings = skipIoMappings;
  }

  public VariableMap getProcessVariables() {
    return processVariables;
  }

  public String getModificationReason() {
    return modificationReason;
  }

  public void setModificationReason(String modificationReason) {
    this.modificationReason = modificationReason;
  }
}
