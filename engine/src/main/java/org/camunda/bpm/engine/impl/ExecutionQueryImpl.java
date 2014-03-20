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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
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
  protected String activityId;
  protected String executionId;
  protected String processInstanceId;
  protected List<EventSubscriptionQueryValue> eventSubscriptions;
  protected SuspensionState suspensionState;
  protected String incidentType;
  protected String incidentId;
  protected String incidentMessage;
  protected String incidentMessageLike;

  // Not used by end-users, but needed for dynamic ibatis query
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  private String businessKey;

  public ExecutionQueryImpl() {
  }

  public ExecutionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public ExecutionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public boolean isProcessInstancesOnly() {
    return false; // see dynamic query
  }

  public ExecutionQueryImpl processDefinitionId(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ProcessEngineException("Process definition id is null");
    }
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public ExecutionQueryImpl processDefinitionKey(String processDefinitionKey) {
    if (processDefinitionKey == null) {
      throw new ProcessEngineException("Process definition key is null");
    }
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public ExecutionQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ProcessEngineException("Process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }

  public ExecutionQuery processInstanceBusinessKey(String businessKey) {
    if (businessKey == null) {
      throw new ProcessEngineException("Business key is null");
    }
    this.businessKey = businessKey;
    return this;
  }

  public ExecutionQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ProcessEngineException("Execution id is null");
    }
    this.executionId = executionId;
    return this;
  }

  public ExecutionQueryImpl activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public ExecutionQuery signalEventSubscription(String signalName) {
    return eventSubscription("signal", signalName);
  }

  public ExecutionQuery signalEventSubscriptionName(String signalName) {
    return eventSubscription("signal", signalName);
  }

  public ExecutionQuery messageEventSubscriptionName(String messageName) {
    return eventSubscription("message", messageName);
  }

  public ExecutionQuery eventSubscription(String eventType, String eventName) {
    if(eventName == null) {
      throw new ProcessEngineException("event name is null");
    }
    if(eventType == null) {
      throw new ProcessEngineException("event type is null");
    }
    if(eventSubscriptions == null) {
      eventSubscriptions = new ArrayList<EventSubscriptionQueryValue>();
    }
    eventSubscriptions.add(new EventSubscriptionQueryValue(eventName, eventType));
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
    assertParamNotNull("incident type", incidentType);
    this.incidentType = incidentType;
    return this;
  }

  public ExecutionQuery incidentId(String incidentId) {
    assertParamNotNull("incident id", incidentId);
    this.incidentId = incidentId;
    return this;
  }

  public ExecutionQuery incidentMessage(String incidentMessage) {
    assertParamNotNull("incident message", incidentMessage);
    this.incidentMessage = incidentMessage;
    return this;
  }

  public ExecutionQuery incidentMessageLike(String incidentMessageLike) {
    assertParamNotNull("incident messageLike", incidentMessageLike);
    this.incidentMessageLike = incidentMessageLike;
    return this;
  }

  //ordering ////////////////////////////////////////////////////

  public ExecutionQueryImpl orderByProcessInstanceId() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_INSTANCE_ID;
    return this;
  }

  public ExecutionQueryImpl orderByProcessDefinitionId() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_DEFINITION_ID;
    return this;
  }

  public ExecutionQueryImpl orderByProcessDefinitionKey() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_DEFINITION_KEY;
    return this;
  }

  //results ////////////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getExecutionManager()
      .findExecutionCountByQueryCriteria(this);
  }

  @SuppressWarnings("unchecked")
  public List<Execution> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return (List) commandContext
      .getExecutionManager()
      .findExecutionsByQueryCriteria(this, page);
  }

  //getters ////////////////////////////////////////////////////

  public boolean getOnlyProcessInstances() {
    return false;
  }
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
  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }
  public String getSubProcessInstanceId() {
    return subProcessInstanceId;
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
