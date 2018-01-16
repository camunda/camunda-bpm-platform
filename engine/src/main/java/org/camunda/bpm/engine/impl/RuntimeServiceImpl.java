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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.impl.cmd.CreateIncidentCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteProcessInstancesCmd;
import org.camunda.bpm.engine.impl.cmd.FindActiveActivityIdsCmd;
import org.camunda.bpm.engine.impl.cmd.GetActivityInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.GetExecutionVariableCmd;
import org.camunda.bpm.engine.impl.cmd.GetExecutionVariableTypedCmd;
import org.camunda.bpm.engine.impl.cmd.GetExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.GetStartFormCmd;
import org.camunda.bpm.engine.impl.cmd.MessageEventReceivedCmd;
import org.camunda.bpm.engine.impl.cmd.PatchExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.RemoveExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.ResolveIncidentCmd;
import org.camunda.bpm.engine.impl.cmd.SetExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.SignalCmd;
import org.camunda.bpm.engine.impl.cmd.batch.DeleteProcessInstanceBatchCmd;
import org.camunda.bpm.engine.impl.migration.MigrationPlanBuilderImpl;
import org.camunda.bpm.engine.impl.migration.MigrationPlanExecutionBuilderImpl;
import org.camunda.bpm.engine.impl.runtime.UpdateProcessInstanceSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ConditionEvaluationBuilder;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ModificationBuilder;
import org.camunda.bpm.engine.runtime.NativeExecutionQuery;
import org.camunda.bpm.engine.runtime.NativeProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.runtime.RestartProcessInstanceBuilder;
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

  @Override
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return createProcessInstanceByKey(processDefinitionKey)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey) {
    return createProcessInstanceByKey(processDefinitionKey)
        .businessKey(businessKey)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, String caseInstanceId) {
    return createProcessInstanceByKey(processDefinitionKey)
        .businessKey(businessKey)
        .caseInstanceId(caseInstanceId)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables) {
    return createProcessInstanceByKey(processDefinitionKey)
        .setVariables(variables)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
    return createProcessInstanceByKey(processDefinitionKey)
        .businessKey(businessKey)
        .setVariables(variables)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, String caseInstanceId, Map<String, Object> variables) {
    return createProcessInstanceByKey(processDefinitionKey)
        .businessKey(businessKey)
        .caseInstanceId(caseInstanceId)
        .setVariables(variables)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceById(String processDefinitionId) {
    return createProcessInstanceById(processDefinitionId)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey) {
    return createProcessInstanceById(processDefinitionId)
        .businessKey(businessKey)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, String caseInstanceId) {
    return createProcessInstanceById(processDefinitionId)
        .businessKey(businessKey)
        .caseInstanceId(caseInstanceId)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
    return createProcessInstanceById(processDefinitionId)
        .setVariables(variables)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
    return createProcessInstanceById(processDefinitionId)
        .businessKey(businessKey)
        .setVariables(variables)
        .execute();
  }

  @Override
  public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, String caseInstanceId, Map<String, Object> variables) {
    return createProcessInstanceById(processDefinitionId)
        .businessKey(businessKey)
        .caseInstanceId(caseInstanceId)
        .setVariables(variables)
        .execute();
  }

  @Override
  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    deleteProcessInstance(processInstanceId,deleteReason,false);
  }

  @Override
  public Batch deleteProcessInstancesAsync(List<String> processInstanceIds, ProcessInstanceQuery processInstanceQuery, String deleteReason) {
    return deleteProcessInstancesAsync(processInstanceIds, processInstanceQuery, deleteReason, false);
  }

  @Override
  public Batch deleteProcessInstancesAsync(List<String> processInstanceIds, String deleteReason) {
    return deleteProcessInstancesAsync(processInstanceIds, null, deleteReason, false);
  }

  @Override
  public Batch deleteProcessInstancesAsync(ProcessInstanceQuery processInstanceQuery, String deleteReason) {
    return deleteProcessInstancesAsync(null, processInstanceQuery, deleteReason, false);
  }

  @Override
  public Batch deleteProcessInstancesAsync(List<String> processInstanceIds, ProcessInstanceQuery processInstanceQuery, String deleteReason, boolean skipCustomListeners) {
    return deleteProcessInstancesAsync(processInstanceIds, processInstanceQuery, deleteReason, skipCustomListeners, false);
  }

  @Override
  public Batch deleteProcessInstancesAsync(List<String> processInstanceIds, ProcessInstanceQuery processInstanceQuery, String deleteReason, boolean skipCustomListeners, boolean skipSubprocesses) {
    return commandExecutor.execute(new DeleteProcessInstanceBatchCmd(processInstanceIds, processInstanceQuery, deleteReason, skipCustomListeners, skipSubprocesses));
  }

  @Override
  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners) {
    deleteProcessInstance(processInstanceId,deleteReason,skipCustomListeners,false);
  }

  @Override
  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated) {
    deleteProcessInstance(processInstanceId, deleteReason, skipCustomListeners, externallyTerminated, false);
  }

  @Override
  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated, boolean skipIoMappings) {
    deleteProcessInstance(processInstanceId, deleteReason, skipCustomListeners, externallyTerminated, skipIoMappings, false);
  }

  @Override
  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated, boolean skipIoMappings, boolean skipSubprocesses) {
    commandExecutor.execute(new DeleteProcessInstanceCmd(processInstanceId, deleteReason, skipCustomListeners, externallyTerminated, skipIoMappings, skipSubprocesses));
  }

  @Override
  public void deleteProcessInstances(List<String> processInstanceIds, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated){
    deleteProcessInstances(processInstanceIds, deleteReason, skipCustomListeners, externallyTerminated, false);
  }

  @Override
  public void deleteProcessInstances(List<String> processInstanceIds, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated, boolean skipSubprocesses){
    commandExecutor.execute(new DeleteProcessInstancesCmd(processInstanceIds, deleteReason, skipCustomListeners, externallyTerminated, skipSubprocesses));
  }

  @Override
  public ExecutionQuery createExecutionQuery() {
    return new ExecutionQueryImpl(commandExecutor);
  }

  @Override
  public NativeExecutionQuery createNativeExecutionQuery() {
    return new NativeExecutionQueryImpl(commandExecutor);
  }

  @Override
  public NativeProcessInstanceQuery createNativeProcessInstanceQuery() {
    return new NativeProcessInstanceQueryImpl(commandExecutor);
  }

  @Override
  public IncidentQuery createIncidentQuery() {
    return new IncidentQueryImpl(commandExecutor);
  }


  @Override
  public EventSubscriptionQuery createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(commandExecutor);
  }

  @Override
  public VariableInstanceQuery createVariableInstanceQuery() {
    return new VariableInstanceQueryImpl(commandExecutor);
  }

  @Override
  public VariableMap getVariables(String executionId) {
    return getVariablesTyped(executionId);
  }

  @Override
  public VariableMap getVariablesTyped(String executionId) {
    return getVariablesTyped(executionId, true);
  }

  @Override
  public VariableMap getVariablesTyped(String executionId, boolean deserializeObjectValues) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, null, false, deserializeObjectValues));
  }

  @Override
  public VariableMap getVariablesLocal(String executionId) {
    return getVariablesLocalTyped(executionId);
  }

  @Override
  public VariableMap getVariablesLocalTyped(String executionId) {
    return getVariablesLocalTyped(executionId, true);
  }

  @Override
  public VariableMap getVariablesLocalTyped(String executionId, boolean deserializeObjectValues) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, null, true, deserializeObjectValues));
  }

  @Override
  public VariableMap getVariables(String executionId, Collection<String> variableNames) {
    return getVariablesTyped(executionId, variableNames, true);
  }

  @Override
  public VariableMap getVariablesTyped(String executionId, Collection<String> variableNames, boolean deserializeObjectValues) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, variableNames, false, deserializeObjectValues));
  }

  @Override
  public VariableMap getVariablesLocal(String executionId, Collection<String> variableNames) {
    return getVariablesLocalTyped(executionId, variableNames, true);
  }

  @Override
  public VariableMap getVariablesLocalTyped(String executionId, Collection<String> variableNames, boolean deserializeObjectValues) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId, variableNames, true, deserializeObjectValues));
  }

  @Override
  public Object getVariable(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName, false));
  }

  @Override
  public <T extends TypedValue> T getVariableTyped(String executionId, String variableName) {
    return getVariableTyped(executionId, variableName, true);
  }

  @Override
  public <T extends TypedValue> T getVariableTyped(String executionId, String variableName, boolean deserializeObjectValue) {
    return commandExecutor.execute(new GetExecutionVariableTypedCmd<T>(executionId, variableName, false, deserializeObjectValue));
  }

  @Override
  public <T extends TypedValue> T getVariableLocalTyped(String executionId, String variableName) {
    return getVariableLocalTyped(executionId, variableName, true);
  }

  @Override
  public <T extends TypedValue> T getVariableLocalTyped(String executionId, String variableName, boolean deserializeObjectValue) {
    return commandExecutor.execute(new GetExecutionVariableTypedCmd<T>(executionId, variableName, true, deserializeObjectValue));
  }

  @Override
  public Object getVariableLocal(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName, true));
  }

  @Override
  public void setVariable(String executionId, String variableName, Object value) {
    ensureNotNull("variableName", variableName);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    setVariables(executionId, variables);
  }

  @Override
  public void setVariableLocal(String executionId, String variableName, Object value) {
    ensureNotNull("variableName", variableName);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    setVariablesLocal(executionId, variables);
  }

  @Override
  public void setVariables(String executionId, Map<String, ? extends Object> variables) {
    setVariables(executionId, variables, false);
  }

  @Override
  public void setVariablesLocal(String executionId, Map<String, ? extends Object> variables) {
    setVariables(executionId, variables, true);
  }

  protected void setVariables(String executionId, Map<String, ? extends Object> variables, boolean local) {
    try {
      commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables, local));
    } catch (ProcessEngineException ex) {
      if (ExceptionUtil.checkValueTooLongException(ex)) {
        throw new BadUserRequestException("Variable value is too long", ex);
      }
      throw ex;
    }
  }

  @Override
  public void removeVariable(String executionId, String variableName) {
    Collection<String> variableNames = new ArrayList<String>();
    variableNames.add(variableName);
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, false));
  }

  @Override
  public void removeVariableLocal(String executionId, String variableName) {
    Collection<String> variableNames = new ArrayList<String>();
    variableNames.add(variableName);
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, true));

  }

  @Override
  public void removeVariables(String executionId, Collection<String> variableNames) {
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, false));
  }

  @Override
  public void removeVariablesLocal(String executionId, Collection<String> variableNames) {
    commandExecutor.execute(new RemoveExecutionVariablesCmd(executionId, variableNames, true));
  }

  public void updateVariables(String executionId, Map<String, ? extends Object> modifications, Collection<String> deletions) {
    updateVariables(executionId, modifications, deletions, false);
  }

  public void updateVariablesLocal(String executionId, Map<String, ? extends Object> modifications, Collection<String> deletions) {
    updateVariables(executionId, modifications, deletions, true);
  }

  protected void updateVariables(String executionId, Map<String, ? extends Object> modifications, Collection<String> deletions, boolean local) {
    try {
      commandExecutor.execute(new PatchExecutionVariablesCmd(executionId, modifications, deletions, local));
    } catch (ProcessEngineException ex) {
      if (ExceptionUtil.checkValueTooLongException(ex)) {
        throw new BadUserRequestException("Variable value is too long", ex);
      }
      throw ex;
    }
  }



  @Override
  public void signal(String executionId) {
    commandExecutor.execute(new SignalCmd(executionId, null, null, null));
  }

  @Override
  public void signal(String executionId, String signalName, Object signalData, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalCmd(executionId, signalName, signalData, processVariables));
  }

  @Override
  public void signal(String executionId, Map<String, Object> processVariables) {
    commandExecutor.execute(new SignalCmd(executionId, null, null, processVariables));
  }

  @Override
  public ProcessInstanceQuery createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl(commandExecutor);
  }

  @Override
  public List<String> getActiveActivityIds(String executionId) {
    return commandExecutor.execute(new FindActiveActivityIdsCmd(executionId));
  }

  @Override
  public ActivityInstance getActivityInstance(String processInstanceId) {
    return commandExecutor.execute(new GetActivityInstanceCmd(processInstanceId));
  }

  public FormData getFormInstanceById(String processDefinitionId) {
    return commandExecutor.execute(new GetStartFormCmd(processDefinitionId));
  }

  @Override
  public void suspendProcessInstanceById(String processInstanceId) {
    updateProcessInstanceSuspensionState()
      .byProcessInstanceId(processInstanceId)
      .suspend();
  }

  @Override
  public void suspendProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    updateProcessInstanceSuspensionState()
      .byProcessDefinitionId(processDefinitionId)
      .suspend();
  }

  @Override
  public void suspendProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
    updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(processDefinitionKey)
      .suspend();
  }

  @Override
  public void activateProcessInstanceById(String processInstanceId) {
    updateProcessInstanceSuspensionState()
      .byProcessInstanceId(processInstanceId)
      .activate();
  }

  @Override
  public void activateProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    updateProcessInstanceSuspensionState()
      .byProcessDefinitionId(processDefinitionId)
      .activate();
  }

  @Override
  public void activateProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
    updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(processDefinitionKey)
      .activate();
  }

  @Override
  public UpdateProcessInstanceSuspensionStateSelectBuilder updateProcessInstanceSuspensionState() {
    return new UpdateProcessInstanceSuspensionStateBuilderImpl(commandExecutor);
  }

  @Override
  public ProcessInstance startProcessInstanceByMessage(String messageName) {
    return createMessageCorrelation(messageName).correlateStartMessage();
  }

  @Override
  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey) {
    return createMessageCorrelation(messageName)
        .processInstanceBusinessKey(businessKey)
        .correlateStartMessage();
  }

  @Override
  public ProcessInstance startProcessInstanceByMessage(String messageName, Map<String, Object> processVariables) {
    return createMessageCorrelation(messageName)
        .setVariables(processVariables)
        .correlateStartMessage();
  }

  @Override
  public ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
    return createMessageCorrelation(messageName)
        .processInstanceBusinessKey(businessKey)
        .setVariables(processVariables)
        .correlateStartMessage();
  }

  @Override
  public ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId) {
    return createMessageCorrelation(messageName)
        .processDefinitionId(processDefinitionId)
        .correlateStartMessage();
  }

  @Override
  public ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId, String businessKey) {
    return createMessageCorrelation(messageName)
        .processDefinitionId(processDefinitionId)
        .processInstanceBusinessKey(businessKey)
        .correlateStartMessage();
  }

  @Override
  public ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId, Map<String, Object> processVariables) {
    return createMessageCorrelation(messageName)
        .processDefinitionId(processDefinitionId)
        .setVariables(processVariables)
        .correlateStartMessage();
  }

  @Override
  public ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId, String businessKey, Map<String, Object> processVariables) {
    return createMessageCorrelation(messageName)
        .processDefinitionId(processDefinitionId)
        .processInstanceBusinessKey(businessKey)
        .setVariables(processVariables)
        .correlateStartMessage();
  }

  @Override
  public void signalEventReceived(String signalName) {
    createSignalEvent(signalName).send();
  }

  @Override
  public void signalEventReceived(String signalName, Map<String, Object> processVariables) {
    createSignalEvent(signalName).setVariables(processVariables).send();
  }

  @Override
  public void signalEventReceived(String signalName, String executionId) {
    createSignalEvent(signalName).executionId(executionId).send();
  }

  @Override
  public void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables) {
    createSignalEvent(signalName).executionId(executionId).setVariables(processVariables).send();
  }

  @Override
  public SignalEventReceivedBuilder createSignalEvent(String signalName) {
    return new SignalEventReceivedBuilderImpl(commandExecutor, signalName);
  }

  @Override
  public void messageEventReceived(String messageName, String executionId) {
    ensureNotNull("messageName", messageName);
    commandExecutor.execute(new MessageEventReceivedCmd(messageName, executionId, null));
  }

  @Override
  public void messageEventReceived(String messageName, String executionId, Map<String, Object> processVariables) {
    ensureNotNull("messageName", messageName);
    commandExecutor.execute(new MessageEventReceivedCmd(messageName, executionId, processVariables));
  }

  @Override
  public MessageCorrelationBuilder createMessageCorrelation(String messageName) {
    return new MessageCorrelationBuilderImpl(commandExecutor, messageName);
  }

  @Override
  public void correlateMessage(String messageName, Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
    createMessageCorrelation(messageName)
      .processInstanceVariablesEqual(correlationKeys)
      .setVariables(processVariables)
      .correlate();
  }

  @Override
  public void correlateMessage(String messageName, String businessKey,
      Map<String, Object> correlationKeys, Map<String, Object> processVariables) {

    createMessageCorrelation(messageName)
      .processInstanceVariablesEqual(correlationKeys)
      .processInstanceBusinessKey(businessKey)
      .setVariables(processVariables)
      .correlate();
  }

  @Override
  public void correlateMessage(String messageName) {
    createMessageCorrelation(messageName).correlate();
  }

  @Override
  public void correlateMessage(String messageName, String businessKey) {
    createMessageCorrelation(messageName)
      .processInstanceBusinessKey(businessKey)
      .correlate();
  }

  @Override
  public void correlateMessage(String messageName, Map<String, Object> correlationKeys) {
    createMessageCorrelation(messageName)
      .processInstanceVariablesEqual(correlationKeys)
      .correlate();
  }

  @Override
  public void correlateMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
    createMessageCorrelation(messageName)
      .processInstanceBusinessKey(businessKey)
      .setVariables(processVariables)
      .correlate();
  }

  @Override
  public ProcessInstanceModificationBuilder createProcessInstanceModification(String processInstanceId) {
    return new ProcessInstanceModificationBuilderImpl(commandExecutor, processInstanceId);
  }

  @Override
  public ProcessInstantiationBuilder createProcessInstanceById(String processDefinitionId) {
    return ProcessInstantiationBuilderImpl.createProcessInstanceById(commandExecutor, processDefinitionId);
  }

  @Override
  public ProcessInstantiationBuilder createProcessInstanceByKey(String processDefinitionKey) {
    return ProcessInstantiationBuilderImpl.createProcessInstanceByKey(commandExecutor, processDefinitionKey);
  }

  @Override
  public MigrationPlanBuilder createMigrationPlan(String sourceProcessDefinitionId, String targetProcessDefinitionId) {
    return new MigrationPlanBuilderImpl(commandExecutor, sourceProcessDefinitionId, targetProcessDefinitionId);
  }

  @Override
  public MigrationPlanExecutionBuilder newMigration(MigrationPlan migrationPlan) {
    return new MigrationPlanExecutionBuilderImpl(commandExecutor, migrationPlan);
  }

  @Override
  public ModificationBuilder createModification(String processDefinitionId) {
    return new ModificationBuilderImpl(commandExecutor, processDefinitionId);
  }

  @Override
  public RestartProcessInstanceBuilder restartProcessInstances(String processDefinitionId) {
    return new RestartProcessInstanceBuilderImpl(commandExecutor, processDefinitionId);
  }

  public Incident createIncident(String incidentType, String executionId, String configuration) {
    return createIncident(incidentType, executionId, configuration, null);
  }

  public Incident createIncident(String incidentType, String executionId, String configuration, String message) {
    return commandExecutor.execute(new CreateIncidentCmd(incidentType, executionId, configuration, message));
  }

  public void resolveIncident(String incidentId) {
    commandExecutor.execute(new ResolveIncidentCmd(incidentId));
  }

  @Override
  public ConditionEvaluationBuilder createConditionEvaluation() {
    return new ConditionEvaluationBuilderImpl(commandExecutor);
  }
}