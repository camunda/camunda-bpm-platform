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
package org.camunda.bpm.engine.test.bpmn.multiinstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;

/**
 * @author Thorben Lindhauer
 *
 */
public class DelegateEvent implements DelegateExecution {

  protected static final List<DelegateEvent> RECORDED_EVENTS = new ArrayList<DelegateEvent>();

  protected String activityInstanceId;
  protected String businessKey;
  protected String currentActivityId;
  protected String currentActivityName;
  protected String currentTransitionId;
  protected String eventName;
  protected String id;
  protected String parentActivityInstanceId;
  protected String parentId;
  protected String processBusinessKey;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String tenantId;
  protected String variableScopeKey;

  public static DelegateEvent fromExecution(DelegateExecution delegateExecution) {
    DelegateEvent event = new DelegateEvent();

    event.activityInstanceId = delegateExecution.getActivityInstanceId();
    event.businessKey = delegateExecution.getBusinessKey();
    event.currentActivityId = delegateExecution.getCurrentActivityId();
    event.currentActivityName = delegateExecution.getCurrentActivityName();
    event.currentTransitionId = delegateExecution.getCurrentTransitionId();
    event.eventName = delegateExecution.getEventName();
    event.id = delegateExecution.getId();
    event.parentActivityInstanceId = delegateExecution.getParentActivityInstanceId();
    event.parentId = delegateExecution.getParentId();
    event.processBusinessKey = delegateExecution.getProcessBusinessKey();
    event.processDefinitionId = delegateExecution.getProcessDefinitionId();
    event.processInstanceId = delegateExecution.getProcessInstanceId();
    event.tenantId = delegateExecution.getTenantId();
    event.variableScopeKey = delegateExecution.getVariableScopeKey();

    return event;
  }

  public static void clearEvents() {
    RECORDED_EVENTS.clear();
  }

  public static void recordEventFor(DelegateExecution execution) {
    RECORDED_EVENTS.add(DelegateEvent.fromExecution(execution));
  }

  public static List<DelegateEvent> getEvents() {
    return RECORDED_EVENTS;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getEventName() {
    return eventName;
  }

  @Override
  public String getBusinessKey() {
    return businessKey;
  }

  @Override
  public String getVariableScopeKey() {
    return variableScopeKey;
  }

  protected RuntimeException notYetImplemented() {
    return new RuntimeException("Recording this method is not implemented");
  }

  protected RuntimeException cannotModifyState() {
    return new RuntimeException("This event is read-only; cannot modify state");
  }

  @Override
  public Map<String, Object> getVariables() {
    throw notYetImplemented();
  }

  @Override
  public VariableMap getVariablesTyped() {
    throw notYetImplemented();
  }

  @Override
  public VariableMap getVariablesTyped(boolean deserializeValues) {
    throw notYetImplemented();
  }

  @Override
  public Map<String, Object> getVariablesLocal() {
    throw notYetImplemented();
  }

  @Override
  public VariableMap getVariablesLocalTyped() {
    throw notYetImplemented();
  }

  @Override
  public VariableMap getVariablesLocalTyped(boolean deserializeValues) {
    throw notYetImplemented();
  }

  @Override
  public Object getVariable(String variableName) {
    throw notYetImplemented();
  }

  @Override
  public Object getVariableLocal(String variableName) {
    throw notYetImplemented();
  }

  @Override
  public <T extends TypedValue> T getVariableTyped(String variableName) {
    throw notYetImplemented();
  }

  @Override
  public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeValue) {
    throw notYetImplemented();
  }

  @Override
  public <T extends TypedValue> T getVariableLocalTyped(String variableName) {
    throw notYetImplemented();
  }

  @Override
  public <T extends TypedValue> T getVariableLocalTyped(String variableName, boolean deserializeValue) {
    throw notYetImplemented();
  }

  @Override
  public Set<String> getVariableNames() {
    throw notYetImplemented();
  }

  @Override
  public Set<String> getVariableNamesLocal() {
    throw notYetImplemented();
  }

  @Override
  public void setVariable(String variableName, Object value) {
    throw cannotModifyState();
  }

  @Override
  public void setVariableLocal(String variableName, Object value) {
    throw cannotModifyState();
  }

  @Override
  public void setVariables(Map<String, ? extends Object> variables) {
    throw cannotModifyState();
  }

  @Override
  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    throw cannotModifyState();
  }

  @Override
  public boolean hasVariables() {
    throw notYetImplemented();
  }

  @Override
  public boolean hasVariablesLocal() {
    throw notYetImplemented();
  }

  @Override
  public boolean hasVariable(String variableName) {
    throw notYetImplemented();
  }

  @Override
  public boolean hasVariableLocal(String variableName) {
    throw notYetImplemented();
  }

  @Override
  public void removeVariable(String variableName) {
    throw cannotModifyState();
  }

  @Override
  public void removeVariableLocal(String variableName) {
    throw cannotModifyState();
  }

  @Override
  public void removeVariables(Collection<String> variableNames) {
    throw cannotModifyState();
  }

  @Override
  public void removeVariablesLocal(Collection<String> variableNames) {
    throw cannotModifyState();
  }

  @Override
  public void removeVariables() {
    throw cannotModifyState();
  }

  @Override
  public void removeVariablesLocal() {
    throw cannotModifyState();
  }

  @Override
  public BpmnModelInstance getBpmnModelInstance() {
    throw notYetImplemented();
  }

  @Override
  public FlowElement getBpmnModelElementInstance() {
    throw notYetImplemented();
  }

  @Override
  public ProcessEngineServices getProcessEngineServices() {
    throw notYetImplemented();
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public String getCurrentActivityId() {
    return currentActivityId;
  }

  public String getCurrentActivityName() {
    return currentActivityName;
  }

  public String getCurrentTransitionId() {
    return currentTransitionId;
  }

  public String getParentActivityInstanceId() {
    return parentActivityInstanceId;
  }

  public String getParentId() {
    return parentId;
  }

  public String getProcessBusinessKey() {
    return processBusinessKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }


  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  @Override
  public void setVariable(String variableName, Object value, String activityId) {
    this.cannotModifyState();
  }

  @Override
  public DelegateExecution getProcessInstance() {
    throw notYetImplemented();
  }

  @Override
  public DelegateExecution getSuperExecution() {
    throw notYetImplemented();
  }

  @Override
  public boolean isCanceled() {
    throw notYetImplemented();
  }

  @Override
  public Incident createIncident(String incidentType, String configuration) {
    throw notYetImplemented();
  }

  @Override
  public void resolveIncident(String incidentId) {
    throw notYetImplemented();
  }

  @Override
  public Incident createIncident(String incidentType, String configuration, String message) {
    throw notYetImplemented();
  }
}