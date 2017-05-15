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

import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.cmd.StartProcessInstanceAtActivitiesCmd;
import org.camunda.bpm.engine.impl.cmd.StartProcessInstanceCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;

/**
 * Simply wraps a modification builder because their API is equivalent.
 *
 * @author Thorben Lindhauer
 */
public class ProcessInstantiationBuilderImpl implements ProcessInstantiationBuilder {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected CommandExecutor commandExecutor;

  protected String processDefinitionId;
  protected String processDefinitionKey;

  protected String businessKey;
  protected String caseInstanceId;
  protected String tenantId;

  protected String processDefinitionTenantId;
  protected boolean isProcessDefinitionTenantIdSet = false;

  protected ProcessInstanceModificationBuilderImpl modificationBuilder;

  protected ProcessInstantiationBuilderImpl(CommandExecutor commandExecutor) {
    modificationBuilder = new ProcessInstanceModificationBuilderImpl();

    this.commandExecutor = commandExecutor;
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
    if (variables != null) {
      modificationBuilder.setVariables(variables);
    }
    return this;
  }

  public ProcessInstantiationBuilder setVariablesLocal(Map<String, Object> variables) {
    if (variables != null) {
      modificationBuilder.setVariablesLocal(variables);
    }
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

  public ProcessInstantiationBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public ProcessInstantiationBuilder processDefinitionTenantId(String tenantId) {
    this.processDefinitionTenantId = tenantId;
    isProcessDefinitionTenantIdSet = true;
    return this;
  }

  public ProcessInstantiationBuilder processDefinitionWithoutTenantId() {
    this.processDefinitionTenantId = null;
    isProcessDefinitionTenantIdSet = true;
    return this;
  }

  public ProcessInstance execute() {
    return execute(false, false);
  }

  public ProcessInstance execute(boolean skipCustomListeners, boolean skipIoMappings) {
    return executeWithVariablesInReturn(skipCustomListeners, skipIoMappings);
  }

  @Override
  public ProcessInstanceWithVariables executeWithVariablesInReturn() {
    return executeWithVariablesInReturn(false, false);
  }
  
  @Override
  public ProcessInstanceWithVariables executeWithVariablesInReturn(boolean skipCustomListeners, boolean skipIoMappings) {
    ensureOnlyOneNotNull("either process definition id or key must be set", processDefinitionId, processDefinitionKey);

    if (isProcessDefinitionTenantIdSet && processDefinitionId != null) {
      throw LOG.exceptionStartProcessInstanceByIdAndTenantId();
    }

    Command<ProcessInstanceWithVariables> command;

    if (modificationBuilder.getModificationOperations().isEmpty()) {

      if(skipCustomListeners || skipIoMappings) {
        throw LOG.exceptionStartProcessInstanceAtStartActivityAndSkipListenersOrMapping();
      }
      // start at the default start activity
      command = new StartProcessInstanceCmd(this);

    } else {
      // start at any activity using the instructions
      modificationBuilder.setSkipCustomListeners(skipCustomListeners);
      modificationBuilder.setSkipIoMappings(skipIoMappings);

      command = new StartProcessInstanceAtActivitiesCmd(this);
    }

    return commandExecutor.execute(command);
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

  public Map<String, Object> getVariables() {
    return modificationBuilder.getProcessVariables();
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getProcessDefinitionTenantId() {
    return processDefinitionTenantId;
  }

  public boolean isProcessDefinitionTenantIdSet() {
    return isProcessDefinitionTenantIdSet;
  }

  public void setModificationBuilder(ProcessInstanceModificationBuilderImpl modificationBuilder) {
    this.modificationBuilder = modificationBuilder;
  }

  public static ProcessInstantiationBuilder createProcessInstanceById(CommandExecutor commandExecutor, String processDefinitionId) {
    ProcessInstantiationBuilderImpl builder = new ProcessInstantiationBuilderImpl(commandExecutor);
    builder.processDefinitionId = processDefinitionId;
    return builder;
  }

  public static ProcessInstantiationBuilder createProcessInstanceByKey(CommandExecutor commandExecutor, String processDefinitionKey) {
    ProcessInstantiationBuilderImpl builder = new ProcessInstantiationBuilderImpl(commandExecutor);
    builder.processDefinitionKey = processDefinitionKey;
    return builder;
  }

}
