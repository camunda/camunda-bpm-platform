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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.impl.cmd.DeleteProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.FindActiveActivityIdsCmd;
import org.camunda.bpm.engine.impl.cmd.GetActivityInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.GetExecutionVariableCmd;
import org.camunda.bpm.engine.impl.cmd.GetExecutionVariableTypedCmd;
import org.camunda.bpm.engine.impl.cmd.GetExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.GetStartFormCmd;
import org.camunda.bpm.engine.impl.cmd.MessageEventReceivedCmd;
import org.camunda.bpm.engine.impl.cmd.PatchExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.RemoveExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.SetExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.SignalCmd;
import org.camunda.bpm.engine.impl.migration.MigrationPlanBuilderImpl;
import org.camunda.bpm.engine.impl.migration.MigrationPlanExecutionBuilderImpl;
import org.camunda.bpm.engine.impl.runtime.UpdateProcessInstanceSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.NativeExecutionQuery;
import org.camunda.bpm.engine.runtime.NativeProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class RuntimeServiceImpl extends ServiceImpl implements RuntimeService {

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return createProcessInstanceByKey(processDefinitionKey)
        .execute();
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey) {
    return createProcessInstanceByKey(processDefinitionKey)
        .businessKey(businessKey)
        .execute();
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, String caseInstanceId) {
    return createProcessInstanceByKey(processDefinitionKey)
        .businessKey(businessKey)
        .caseInstanceId(caseInstanceId)
        .execute();
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables) {
    return createProcessInstanceByKey(processDefinitionKey)
        .setVariables(variables)
        .execute();
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
    return createProcessInstanceByKey(processDefinitionKey)
        .businessKey(businessKey)
        .setVariables(variables)
        .execute();
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, String caseInstanceId, Map<String, Object> variables) {
    return createProcessInstanceByKey(processDefinitionKey)
        .businessKey(businessKey)
        .caseInstanceId(caseInstanceId)
        .setVariables(variables)
        .execute();
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId) {
    return createProcessInstanceById(processDefinitionId)
        .execute();
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey) {
    return createProcessInstanceById(processDefinitionId)
        .businessKey(businessKey)
        .execute();
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, String caseInstanceId) {
    return createProcessInstanceById(processDefinitionId)
        .businessKey(businessKey)
        .caseInstanceId(caseInstanceId)
        .execute();
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
    return createProcessInstanceById(processDefinitionId)
        .setVariables(variables)
        .execute();
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
    return createProcessInstanceById(processDefinitionId)
        .businessKey(businessKey)
        .setVariables(variables)
        .execute();
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, String caseInstanceId, Map<String, Object> variables) {
    return createProcessInstanceById(processDefinitionId)
        .businessKey(businessKey)
        .caseInstanceId(caseInstanceId)
        .setVariables(variables)
        .execute();
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    deleteProcessInstance(processInstanceId,deleteReason,false);
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners) {
    deleteProcessInstance(processInstanceId,deleteReason,skipCustomListeners,false);
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated) {
    commandExecutor.execute(new DeleteProcessInstanceCmd(processInstanceId, deleteReason, skipCustomListeners, externallyTerminated));
  }

  public ExecutionQuery createExecutionQuery() {
    return new ExecutionQueryImpl(commandExecutor);
  }

  public NativeExecutionQuery createNativeExecutionQuery() {
    return new NativeExecutionQueryImpl(commandExecutor);
  }

  public NativeProcessInstanceQuery createNativeProcessInstanceQuery() {
    return new NativeProcessInstanceQueryImpl(commandExecutor);
  }

  public IncidentQuery createIncidentQuery() {
    return new IncidentQueryImpl(commandExecutor);
  }


  public EventSubscriptionQuery createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(commandExecutor);
  }

  public VariableInstanceQuery createVariableInstanceQuery() {
    return new VariableInstanceQueryImpl(commandExecutor);
  }

  public VariableMap getVariables(String executionId) {
    return getVariablesTyped(executionId);
  }

  public VariableMap getVariablesTyped(String executionId) {
    return getVariablesTyped(executionId, true);
  }

  public VariableMap getVariablesTyped(String executionId, boolean deserializeObjectValues) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, null, false, deserializeObjectValues));
  }

  public VariableMap getVariablesLocal(String executionId) {
    return getVariablesLocalTyped(executionId);
  }

  public VariableMap getVariablesLocalTyped(String executionId) {
    return getVariablesLocalTyped(executionId, true);
  }

  public VariableMap getVariablesLocalTyped(String executionId, boolean deserializeObjectValues) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, null, true, deserializeObjectValues));
  }

  public VariableMap getVariables(String executionId, Collection<String> variableNames) {
    return getVariablesTyped(executionId, variableNames, true);
  }

  public VariableMap getVariablesTyped(String executionId, Collection<String> variableNames, boolean deserializeObjectValues) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, variableNames, false, deserializeObjectValues));
  }

  public VariableMap getVariablesLocal(String executionId, Collection<String> variableNames) {
    return getVariablesLocalTyped(executionId, variableNames, true);
  }

  public VariableMap getVariablesLocalTyped(String executionId, Collection<String> variableNames, boolean deserializeObjectValues) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, variableNames, true, deserializeObjectValues));
  }

  public Object getVariable(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName, false));
  }

  public <T extends TypedValue> T getVariableTyped(String executionId, String variableName) {
    return getVariableTyped(executionId, variableName, true);
  }

  public <T extends TypedValue> T getVariableTyped(String executionId, String variableName, boolean deserializeObjectValue) {
    return commandExecutor.execute(new GetExecutionVariableTypedCmd<T>(executionId, variableName, false, deserializeObjectValue));
  }

  public <T extends TypedValue> T getVariableLocalTyped(String executionId, String variableName) {
    return getVariableLocalTyped(executionId, variableName, true);
  }

  public <T extends TypedValue> T getVariableLocalTyped(String executionId, String variableName, boolean deserializeObjectValue) {
    return commandExecutor.execute(new GetExecutionVariableTypedCmd<T>(executionId, variableName, true, deserializeObjectValue));
  }

  public Object getVariableLocal(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName, true));
  }

  public void setVariable(String executionId, String variableName, Object value) {
    ensureNotNull("variableName", variableName);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, false));
  }

  public void setVariableLocal(String executionId, String variableName, Object value) {
    ensureNotNull("variableName", variableName);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, true));
  }

  public void setVariables(String executionId, Map<String, ? extends Object> variables) {
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, false));
  }

  public void setVariablesLocal(String executionId, Map<String, ? extends Object> variables) {
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, true));
  }

  public void removeVariable(String executionId, String variableName) {
    Collection<String> variableNames = new ArrayList<String>();
    variableNames.add(variableName);
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, false));
  }

  public void removeVariableLocal(String executionId, String variableName) {
    Collection<String> variableNames = new ArrayList<String>();
    variableNames.add(variableName);
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, true));

  }

  public void removeVariables(String executionId, Collection<String> variableNames) {
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, false));
  }

  public void removeVariablesLocal(String executionId, Collection<String> variableNames) {
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, true));
  }

  public void updateVariables(String executionId, Map<String, ? extends Object> modifications, Collection<String> deletions) {
    commandExecutor.execute(new PatchExecutionVariablesCmd(executionId, modifications, deletions, false));
  }

  public void updateVariablesLocal(String executionId, Map<String, ? extends Object> modifications, Collection<String> deletions) {
    commandExecutor.execute(new PatchExecutionVariablesCmd(executionId, modifications, deletions, true));
  }

  public void signal(String executionId) {
    commandExecutor.execute(new SignalCmd(executionId, null, null, null));
  }

  public void signal(String executionId, String signalName, Object signalData, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalCmd(executionId, signalName, signalData, processVariables));
  }

  public void signal(String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalCmd(executionId, null, null, processVariables));
  }

  public ProcessInstanceQuery createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl(commandExecutor);
  }

  public List<String> getActiveActivityIds(String executionId) {
    return commandExecutor.execute(new FindActiveActivityIdsCmd(executionId));
  }

  public ActivityInstance getActivityInstance(String processInstanceId) {
    return commandExecutor.execute(new GetActivityInstanceCmd(processInstanceId));
  }

  public FormData getFormInstanceById(String processDefinitionId) {
    return commandExecutor.execute(new GetStartFormCmd(processDefinitionId));
  }

  public void suspendProcessInstanceById(String processInstanceId) {
    updateProcessInstanceSuspensionState()
      .byProcessInstanceId(processInstanceId)
      .suspend();
  }

  public void suspendProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    updateProcessInstanceSuspensionState()
      .byProcessDefinitionId(processDefinitionId)
      .suspend();
  }

  public void suspendProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
    updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(processDefinitionKey)
      .suspend();
  }

  public void activateProcessInstanceById(String processInstanceId) {
    updateProcessInstanceSuspensionState()
      .byProcessInstanceId(processInstanceId)
      .activate();
  }

  public void activateProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    updateProcessInstanceSuspensionState()
      .byProcessDefinitionId(processDefinitionId)
      .activate();
  }

  public void activateProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
    updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(processDefinitionKey)
      .activate();
  }

  public UpdateProcessInstanceSuspensionStateSelectBuilder updateProcessInstanceSuspensionState() {
    return new UpdateProcessInstanceSuspensionStateBuilderImpl(commandExecutor);
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName) {
    return createMessageCorrelation(messageName).correlateStartMessage();
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey) {
    return createMessageCorrelation(messageName)
        .processInstanceBusinessKey(businessKey)
        .correlateStartMessage();
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName, Map<String, Object> processVariables) {
    return createMessageCorrelation(messageName)
        .setVariables(processVariables)
        .correlateStartMessage();
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
    return createMessageCorrelation(messageName)
        .processInstanceBusinessKey(businessKey)
        .setVariables(processVariables)
        .correlateStartMessage();
  }

  public ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId) {
    return createMessageCorrelation(messageName)
        .processDefinitionId(processDefinitionId)
        .correlateStartMessage();
  }

  public ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId, String businessKey) {
    return createMessageCorrelation(messageName)
        .processDefinitionId(processDefinitionId)
        .processInstanceBusinessKey(businessKey)
        .correlateStartMessage();
  }

  public ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId, Map<String, Object> processVariables) {
    return createMessageCorrelation(messageName)
        .processDefinitionId(processDefinitionId)
        .setVariables(processVariables)
        .correlateStartMessage();
  }

  public ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId, String businessKey, Map<String, Object> processVariables) {
    return createMessageCorrelation(messageName)
        .processDefinitionId(processDefinitionId)
        .processInstanceBusinessKey(businessKey)
        .setVariables(processVariables)
        .correlateStartMessage();
  }

  public void signalEventReceived(String signalName) {
    createSignalEvent(signalName).send();
  }

  public void signalEventReceived(String signalName, Map<String, Object> processVariables) {
    createSignalEvent(signalName).setVariables(processVariables).send();
  }

  public void signalEventReceived(String signalName, String executionId) {
    createSignalEvent(signalName).executionId(executionId).send();
  }

  public void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables) {
    createSignalEvent(signalName).executionId(executionId).setVariables(processVariables).send();
  }

  public SignalEventReceivedBuilder createSignalEvent(String signalName) {
    return new SignalEventReceivedBuilderImpl(commandExecutor, signalName);
  }

  public void messageEventReceived(String messageName, String executionId) {
    ensureNotNull("messageName", messageName);
    commandExecutor.execute(new MessageEventReceivedCmd(messageName, executionId, null));
  }

  public void messageEventReceived(String messageName, String executionId, Map<String, Object> processVariables) {
    ensureNotNull("messageName", messageName);
    commandExecutor.execute(new MessageEventReceivedCmd(messageName, executionId, processVariables));
  }

  public MessageCorrelationBuilder createMessageCorrelation(String messageName) {
    return new MessageCorrelationBuilderImpl(commandExecutor, messageName);
  }

  public void correlateMessage(String messageName, Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
    createMessageCorrelation(messageName)
      .processInstanceVariablesEqual(correlationKeys)
      .setVariables(processVariables)
      .correlate();
  }

  public void correlateMessage(String messageName, String businessKey,
      Map<String, Object> correlationKeys, Map<String, Object> processVariables) {

    createMessageCorrelation(messageName)
      .processInstanceVariablesEqual(correlationKeys)
      .processInstanceBusinessKey(businessKey)
      .setVariables(processVariables)
      .correlate();
  }

  public void correlateMessage(String messageName) {
    createMessageCorrelation(messageName).correlate();
  }

  public void correlateMessage(String messageName, String businessKey) {
    createMessageCorrelation(messageName)
      .processInstanceBusinessKey(businessKey)
      .correlate();
  }

  public void correlateMessage(String messageName, Map<String, Object> correlationKeys) {
    createMessageCorrelation(messageName)
      .processInstanceVariablesEqual(correlationKeys)
      .correlate();
  }

  public void correlateMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
    createMessageCorrelation(messageName)
      .processInstanceBusinessKey(businessKey)
      .setVariables(processVariables)
      .correlate();
  }

  public ProcessInstanceModificationBuilder createProcessInstanceModification(String processInstanceId) {
    return new ProcessInstanceModificationBuilderImpl(commandExecutor, processInstanceId);
  }

  public ProcessInstantiationBuilder createProcessInstanceById(String processDefinitionId) {
    return ProcessInstantiationBuilderImpl.createProcessInstanceById(commandExecutor, processDefinitionId);
  }

  public ProcessInstantiationBuilder createProcessInstanceByKey(String processDefinitionKey) {
    return ProcessInstantiationBuilderImpl.createProcessInstanceByKey(commandExecutor, processDefinitionKey);
  }

  public MigrationPlanBuilder createMigrationPlan(String sourceProcessDefinitionId, String targetProcessDefinitionId) {
    return new MigrationPlanBuilderImpl(commandExecutor, sourceProcessDefinitionId, targetProcessDefinitionId);
  }

  public MigrationPlanExecutionBuilder newMigration(MigrationPlan migrationPlan) {
    return new MigrationPlanExecutionBuilderImpl(commandExecutor, migrationPlan);
  }

}
