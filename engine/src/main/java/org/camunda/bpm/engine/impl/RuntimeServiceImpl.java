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
import org.camunda.bpm.engine.impl.cmd.ActivateProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.CorrelateMessageCmd;
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
import org.camunda.bpm.engine.impl.cmd.SignalEventReceivedCmd;
import org.camunda.bpm.engine.impl.cmd.StartProcessInstanceByMessageCmd;
import org.camunda.bpm.engine.impl.cmd.StartProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendProcessInstanceCmd;
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
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class RuntimeServiceImpl extends ServiceImpl implements RuntimeService {

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd(processDefinitionKey, null, null, null, null));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd(processDefinitionKey, null, businessKey, null, null));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, String caseInstanceId) {
    return commandExecutor.execute(new StartProcessInstanceCmd(processDefinitionKey, null, businessKey, caseInstanceId, null));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd(processDefinitionKey, null, null, null, variables));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd(processDefinitionKey, null, businessKey, null, variables));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, String caseInstanceId, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd(processDefinitionKey, null, businessKey, caseInstanceId, variables));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId) {
    return commandExecutor.execute(new StartProcessInstanceCmd(null, processDefinitionId, null, null, null));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd(null, processDefinitionId, businessKey, null, null));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, String caseInstanceId) {
    return commandExecutor.execute(new StartProcessInstanceCmd(null, processDefinitionId, businessKey, caseInstanceId, null));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd(null, processDefinitionId, null, null, variables));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd(null, processDefinitionId, businessKey, null, variables));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, String caseInstanceId, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd(null, processDefinitionId, businessKey, caseInstanceId, variables));
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    commandExecutor.execute(new DeleteProcessInstanceCmd(processInstanceId, deleteReason, false));
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners) {
    commandExecutor.execute(new DeleteProcessInstanceCmd(processInstanceId, deleteReason, skipCustomListeners));
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
    commandExecutor.execute(new SuspendProcessInstanceCmd(processInstanceId, null, null));
  }

  public void suspendProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    commandExecutor.execute(new SuspendProcessInstanceCmd(null, processDefinitionId, null));
  }

  public void suspendProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
    commandExecutor.execute(new SuspendProcessInstanceCmd(null, null, processDefinitionKey));
  }

  public void activateProcessInstanceById(String processInstanceId) {
    commandExecutor.execute(new ActivateProcessInstanceCmd(processInstanceId, null, null));
  }

  public void activateProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    commandExecutor.execute(new ActivateProcessInstanceCmd(null, processDefinitionId, null));
  }

  public void activateProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
    commandExecutor.execute(new ActivateProcessInstanceCmd(null, null, processDefinitionKey));
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName,null, null));
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, businessKey, null));
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName, Map<String, Object> processVariables) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, null, processVariables));
  }

  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
    return commandExecutor.execute(new StartProcessInstanceByMessageCmd(messageName, businessKey, processVariables));
  }

  public void signalEventReceived(String signalName) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, null));
  }

  public void signalEventReceived(String signalName, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, null, processVariables));
  }

  public void signalEventReceived(String signalName, String executionId) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, executionId, null));
  }

  public void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalEventReceivedCmd(signalName, executionId, processVariables));
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
    commandExecutor.execute(new CorrelateMessageCmd(messageName, null, correlationKeys, processVariables));
  }

  public void correlateMessage(String messageName, String businessKey,
      Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
    commandExecutor.execute(new CorrelateMessageCmd(messageName, businessKey, correlationKeys, processVariables));
  }

  public void correlateMessage(String messageName) {
    commandExecutor.execute(new CorrelateMessageCmd(messageName, null, null, null));
  }

  public void correlateMessage(String messageName, String businessKey) {
    commandExecutor.execute(new CorrelateMessageCmd(messageName, businessKey, null, null));
  }

  public void correlateMessage(String messageName,
      Map<String, Object> correlationKeys) {
    commandExecutor.execute(new CorrelateMessageCmd(messageName, null, correlationKeys, null));
  }

  public void correlateMessage(String messageName, String businessKey,
      Map<String, Object> processVariables) {
    commandExecutor.execute(new CorrelateMessageCmd(messageName, businessKey, null, processVariables));
  }

  public ProcessInstanceModificationBuilder createProcessInstanceModification(String processInstanceId) {
    return new ProcessInstanceModificationBuilderImpl(commandExecutor, processInstanceId);
  }

}
