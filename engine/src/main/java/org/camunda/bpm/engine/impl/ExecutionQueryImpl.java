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
import org.camunda.bpm.engine.impl.event.EventType;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;


/**
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Daniel Meyer
 */
public class ExecutionQueryImpl extends AbstractVariableQueryImpl<ExecutionQuery, Execution>
  implements ExecutionQuery {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String businessKey;
  protected String activityId;
  protected String executionId;
  protected String processInstanceId;
  protected List<EventSubscriptionQueryValue> eventSubscriptions;
  protected SuspensionState suspensionState;
  protected String incidentType;
  protected String incidentId;
  protected String incidentMessage;
  protected String incidentMessageLike;

  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;

  public ExecutionQueryImpl() {
  }

  public ExecutionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public ExecutionQueryImpl processDefinitionId(String processDefinitionId) {
    ensureNotNull("Process definition id", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public ExecutionQueryImpl processDefinitionKey(String processDefinitionKey) {
    ensureNotNull("Process definition key", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public ExecutionQueryImpl processInstanceId(String processInstanceId) {
    ensureNotNull("Process instance id", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public ExecutionQuery processInstanceBusinessKey(String businessKey) {
    ensureNotNull("Business key", businessKey);
    this.businessKey = businessKey;
    return this;
  }

  public ExecutionQueryImpl executionId(String executionId) {
    ensureNotNull("Execution id", executionId);
    this.executionId = executionId;
    return this;
  }

  public ExecutionQueryImpl activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public ExecutionQuery signalEventSubscription(String signalName) {
    return eventSubscription(EventType.SIGNAL, signalName);
  }

  public ExecutionQuery signalEventSubscriptionName(String signalName) {
    return eventSubscription(EventType.SIGNAL, signalName);
  }

  public ExecutionQuery messageEventSubscriptionName(String messageName) {
    return eventSubscription(EventType.MESSAGE, messageName);
  }

  public ExecutionQuery messageEventSubscription() {
    return eventSubscription(EventType.MESSAGE, null);
  }

  public ExecutionQuery eventSubscription(EventType eventType, String eventName) {
    ensureNotNull("event type", eventType);
    if (!EventType.MESSAGE.equals(eventType)) {
      // event name is optional for message events
      ensureNotNull("event name", eventName);
    }
    if(eventSubscriptions == null) {
      eventSubscriptions = new ArrayList<EventSubscriptionQueryValue>();
    }
    eventSubscriptions.add(new EventSubscriptionQueryValue(eventName, eventType.name()));
    return this;
  }

  public ExecutionQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  public ExecutionQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  public ExecutionQuery processVariableValueEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.EQUALS, false);
    return this;
  }

  public ExecutionQuery processVariableValueNotEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_EQUALS, false);
    return this;
  }

  public ExecutionQuery incidentType(String incidentType) {
    ensureNotNull("incident type", incidentType);
    this.incidentType = incidentType;
    return this;
  }

  public ExecutionQuery incidentId(String incidentId) {
    ensureNotNull("incident id", incidentId);
    this.incidentId = incidentId;
    return this;
  }

  public ExecutionQuery incidentMessage(String incidentMessage) {
    ensureNotNull("incident message", incidentMessage);
    this.incidentMessage = incidentMessage;
    return this;
  }

  public ExecutionQuery incidentMessageLike(String incidentMessageLike) {
    ensureNotNull("incident messageLike", incidentMessageLike);
    this.incidentMessageLike = incidentMessageLike;
    return this;
  }

  public ExecutionQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public ExecutionQuery withoutTenantId() {
    this.tenantIds = null;
    isTenantIdSet = true;
    return this;
  }

  //ordering ////////////////////////////////////////////////////

  public ExecutionQueryImpl orderByProcessInstanceId() {
    orderBy(ExecutionQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public ExecutionQueryImpl orderByProcessDefinitionId() {
    orderBy(new QueryOrderingProperty(QueryOrderingProperty.RELATION_PROCESS_DEFINITION, ExecutionQueryProperty.PROCESS_DEFINITION_ID));
    return this;
  }

  public ExecutionQueryImpl orderByProcessDefinitionKey() {
    orderBy(new QueryOrderingProperty(QueryOrderingProperty.RELATION_PROCESS_DEFINITION, ExecutionQueryProperty.PROCESS_DEFINITION_KEY));
    return this;
  }

  public ExecutionQuery orderByTenantId() {
    orderBy(ExecutionQueryProperty.TENANT_ID);
    return this;
  }

  //results ////////////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getExecutionManager()
      .findExecutionCountByQueryCriteria(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Execution> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return (List) commandContext
      .getExecutionManager()
      .findExecutionsByQueryCriteria(this, page);
  }

  //getters ////////////////////////////////////////////////////

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessInstanceIds() {
    return null;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getExecutionId() {
    return executionId;
  }

  public SuspensionState getSuspensionState() {
    return suspensionState;
  }

  public void setSuspensionState(SuspensionState suspensionState) {
    this.suspensionState = suspensionState;
  }

  public List<EventSubscriptionQueryValue> getEventSubscriptions() {
    return eventSubscriptions;
  }

  public void setEventSubscriptions(List<EventSubscriptionQueryValue> eventSubscriptions) {
    this.eventSubscriptions = eventSubscriptions;
  }

  public String getIncidentId() {
    return incidentId;
  }

  public String getIncidentType() {
    return incidentType;
  }

  public String getIncidentMessage() {
    return incidentMessage;
  }

  public String getIncidentMessageLike() {
    return incidentMessageLike;
  }

}
