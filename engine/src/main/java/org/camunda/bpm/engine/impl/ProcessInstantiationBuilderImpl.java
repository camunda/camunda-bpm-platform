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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import java.util.Map;

import org.camunda.bpm.engine.impl.cmd.StartProcessInstanceAtActivitiesCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;

/**
 * Simply wraps a modification builder because their API is equivalent.
 *
 * @author Thorben Lindhauer
 */
public class ProcessInstantiationBuilderImpl implements ProcessInstantiationBuilder {

  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected String processDefinitionId;
  protected String processDefinitionKey;

  protected String businessKey;
  protected String caseInstanceId;

  protected ProcessInstanceModificationBuilderImpl modificationBuilder;

  public ProcessInstantiationBuilderImpl(String processDefinitionId, String processDefinitionKey) {
    ensureOnlyOneNotNull("either process definition id or key must be set", processDefinitionId, processDefinitionKey);
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
    modificationBuilder = new ProcessInstanceModificationBuilderImpl();
  }

  public ProcessInstantiationBuilderImpl(CommandExecutor commandExecutor, String processDefinitionId, String processDefinitionKey) {
    this(processDefinitionId, processDefinitionKey);
    this.commandExecutor = commandExecutor;
  }

  public ProcessInstantiationBuilderImpl(CommandContext commandContext, String processDefinitionId, String processDefinitionKey) {
    this(processDefinitionId, processDefinitionKey);
    this.commandContext = commandContext;
  }


  public ProcessInstantiationBuilder startBeforeActivity(String activityId) {
    modificationBuilder.startBeforeActivity(activityId);
    return this;
  }

  public ProcessInstantiationBuilder startAfterActivity(String activityId) {
    modificationBuilder.startAfterActivity(activityId);
    return this;
  }

  public ProcessInstantiationBuilder startTransition(String transitionId) {
    modificationBuilder.startTransition(transitionId);
    return this;
  }

  public ProcessInstantiationBuilder setVariable(String name, Object value) {
    modificationBuilder.setVariable(name, value);
    return this;
  }

  public ProcessInstantiationBuilder setVariableLocal(String name, Object value) {
    modificationBuilder.setVariableLocal(name, value);
    return this;
  }

  public ProcessInstantiationBuilder setVariables(Map<String, Object> variables) {
    modificationBuilder.setVariables(variables);
    return this;
  }

  public ProcessInstantiationBuilder setVariablesLocal(Map<String, Object> variables) {
    modificationBuilder.setVariablesLocal(variables);
    return this;
  }

  public ProcessInstantiationBuilder businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public ProcessInstantiationBuilder caseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public ProcessInstance execute() {
    return execute(false, false);
  }

  public ProcessInstance execute(boolean skipCustomListeners, boolean skipIoMappings) {
    modificationBuilder.setSkipCustomListeners(skipCustomListeners);
    modificationBuilder.setSkipIoMappings(skipIoMappings);

    StartProcessInstanceAtActivitiesCmd cmd = new StartProcessInstanceAtActivitiesCmd(this);
    if (commandExecutor != null) {
      return commandExecutor.execute(cmd);
    } else {
      return cmd.execute(commandContext);
    }
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public ProcessInstanceModificationBuilderImpl getModificationBuilder() {
    return modificationBuilder;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

}
