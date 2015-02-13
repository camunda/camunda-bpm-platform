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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cmd.ModifyProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.ModifyProcessInstanceCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationBuilderImpl implements ProcessInstanceModificationBuilder {

  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected String processInstanceId;
  protected Set<String> activityInstancesToCancel = new HashSet<String>();

  // TODO: this needs to be a more sophisticated data strucutre:
  // first, it is possible to instantiate the same activity twice (i.e. no set)
  // second, it should be possible to add variables to an activity
  protected List<ActivityInstantiationInstruction> activitiesToStartBefore = new ArrayList<ActivityInstantiationInstruction>();
  protected ActivityInstantiationInstruction currentActivity;

  public ProcessInstanceModificationBuilderImpl(CommandExecutor commandExecutor, String processInstanceId) {
    this(processInstanceId);
    this.commandExecutor = commandExecutor;
  }

  public ProcessInstanceModificationBuilderImpl(CommandContext commandContext, String processInstanceId) {
    this(processInstanceId);
    this.commandContext = commandContext;
  }

  public ProcessInstanceModificationBuilderImpl(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public ProcessInstanceModificationBuilder cancelActivityInstance(String activityInstanceId) {
    activityInstancesToCancel.add(activityInstanceId);
    return this;
  }

  public ProcessInstanceModificationBuilder startBeforeActivity(String activityId) {
    currentActivity = new ActivityInstantiationInstruction(activityId);
    activitiesToStartBefore.add(currentActivity);
    return this;
  }


  public ProcessInstanceModificationBuilder setVariable(String name, Object value) {
    ensureNotNull(NotValidException.class, "Variable name must not be null", "name", name);
    ensureNotNull(NotValidException.class, "No activity to start specified", "variable", currentActivity);

    currentActivity.addVariable(name, value);
    return this;
  }

  public ProcessInstanceModificationBuilder setVariableLocal(String name, Object value) {
    ensureNotNull(NotValidException.class, "Variable name must not be null", "name", name);
    ensureNotNull(NotValidException.class, "No activity to start specified", "variableLocal", currentActivity);

    currentActivity.addVariableLocal(name, value);
    return this;
  }

  public void execute() {
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

  public Set<String> getActivityInstancesToCancel() {
    return activityInstancesToCancel;
  }

  public List<ActivityInstantiationInstruction> getActivitiesToStartBefore() {
    return activitiesToStartBefore;
  }

}
