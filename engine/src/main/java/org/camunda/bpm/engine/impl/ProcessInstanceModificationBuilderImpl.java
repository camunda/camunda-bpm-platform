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

import org.camunda.bpm.engine.impl.cmd.AbstractInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.cmd.ActivityAfterInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityInstanceCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ModifyProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.TransitionInstantiationCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstanceActivityInstantiationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationBuilderImpl implements ProcessInstanceModificationBuilder {

  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected String processInstanceId;

  protected boolean skipCustomListeners = false;
  protected boolean skipIoMappings = false;

  protected List<AbstractProcessInstanceModificationCommand> operations = new ArrayList<AbstractProcessInstanceModificationCommand>();

  public ProcessInstanceModificationBuilderImpl(CommandExecutor commandExecutor, String processInstanceId) {
    this(processInstanceId);
    this.commandExecutor = commandExecutor;
  }

  public ProcessInstanceModificationBuilderImpl(CommandContext commandContext, String processInstanceId) {
    this(processInstanceId);
    this.commandContext = commandContext;
  }

  public ProcessInstanceModificationBuilderImpl(String processInstanceId) {
    ensureNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
  }

  public ProcessInstanceModificationBuilder cancelActivityInstance(String activityInstanceId) {
    ensureNotNull("activityInstanceId", activityInstanceId);
    operations.add(new ActivityInstanceCancellationCmd(processInstanceId, activityInstanceId));
    return this;
  }

  public ProcessInstanceModificationBuilder cancelAllInActivity(String activityId) {
    ensureNotNull("activityId", activityId);
    operations.add(new ActivityCancellationCmd(processInstanceId, activityId));
    return this;
  }

  public ProcessInstanceActivityInstantiationBuilder startBeforeActivity(String activityId) {
    ensureNotNull("activityId", activityId);
    AbstractInstantiationCmd currentInstantiation = new ActivityInstantiationCmd(processInstanceId, activityId);
    operations.add(currentInstantiation);
    return new ProcessInstanceActivityInstantiationBuilderImpl(this, currentInstantiation);
  }

  public ProcessInstanceActivityInstantiationBuilder startBeforeActivity(String activityId, String ancestorActivityInstanceId) {
    ensureNotNull("activityId", activityId);
    ensureNotNull("ancestorActivityInstanceId", ancestorActivityInstanceId);
    AbstractInstantiationCmd currentInstantiation = new ActivityInstantiationCmd(processInstanceId, activityId, ancestorActivityInstanceId);
    operations.add(currentInstantiation);
    return new ProcessInstanceActivityInstantiationBuilderImpl(this, currentInstantiation);
  }

  public ProcessInstanceActivityInstantiationBuilder startAfterActivity(String activityId) {
    ensureNotNull("activityId", activityId);
    AbstractInstantiationCmd currentInstantiation = new ActivityAfterInstantiationCmd(processInstanceId, activityId);
    operations.add(currentInstantiation);
    return new ProcessInstanceActivityInstantiationBuilderImpl(this, currentInstantiation);
  }

  public ProcessInstanceActivityInstantiationBuilder startAfterActivity(String activityId, String ancestorActivityInstanceId) {
    ensureNotNull("activityId", activityId);
    ensureNotNull("ancestorActivityInstanceId", ancestorActivityInstanceId);
    AbstractInstantiationCmd currentInstantiation = new ActivityAfterInstantiationCmd(processInstanceId, activityId, ancestorActivityInstanceId);
    operations.add(currentInstantiation);
    return new ProcessInstanceActivityInstantiationBuilderImpl(this, currentInstantiation);
  }

  public ProcessInstanceActivityInstantiationBuilder startTransition(String transitionId) {
    ensureNotNull("transitionId", transitionId);
    AbstractInstantiationCmd currentInstantiation = new TransitionInstantiationCmd(processInstanceId, transitionId);
    operations.add(currentInstantiation);
    return new ProcessInstanceActivityInstantiationBuilderImpl(this, currentInstantiation);
  }

  public ProcessInstanceActivityInstantiationBuilder startTransition(String transitionId, String ancestorActivityInstanceId) {
    ensureNotNull("transitionId", transitionId);
    ensureNotNull("ancestorActivityInstanceId", ancestorActivityInstanceId);
    AbstractInstantiationCmd currentInstantiation = new TransitionInstantiationCmd(processInstanceId, transitionId, ancestorActivityInstanceId);
    operations.add(currentInstantiation);
    return new ProcessInstanceActivityInstantiationBuilderImpl(this, currentInstantiation);
  }

  public void execute() {
    execute(false, false);
  }

  public void execute(boolean skipCustomListeners, boolean skipIoMappings) {
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;

    ModifyProcessInstanceCmd cmd = new ModifyProcessInstanceCmd(this);
    if (commandExecutor != null) {
      commandExecutor.execute(cmd);
    } else {
      cmd.execute(commandContext);
    }
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

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }


}
