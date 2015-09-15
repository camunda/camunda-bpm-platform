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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.variable.type.ValueType;

/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceQueryImpl extends AbstractQuery<HistoricVariableInstanceQuery, HistoricVariableInstance> implements
        HistoricVariableInstanceQuery {

  private final static Logger LOGGER = Logger.getLogger(HistoricVariableInstanceQueryImpl.class.getName());

  private static final long serialVersionUID = 1L;
  protected String variableId;
  protected String processInstanceId;
  protected String caseInstanceId;
  protected String variableName;
  protected String variableNameLike;
  protected QueryVariableValue queryVariableValue;
  protected String[] taskIds;
  protected String[] executionIds;
  protected String[] caseExecutionIds;
  protected String[] activityInstanceIds;

  protected boolean isByteArrayFetchingEnabled = true;
  protected boolean isCustomObjectDeserializationEnabled = true;

  public HistoricVariableInstanceQueryImpl() {
  }

  public HistoricVariableInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricVariableInstanceQuery variableId(String id) {
    ensureNotNull("variableId", id);
    this.variableId = id;
    return this;
  }

  public HistoricVariableInstanceQueryImpl processInstanceId(String processInstanceId) {
    ensureNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricVariableInstanceQuery caseInstanceId(String caseInstanceId) {
    ensureNotNull("caseInstanceId", caseInstanceId);
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public HistoricVariableInstanceQuery taskIdIn(String... taskIds) {
    ensureNotNull("Task Ids", (Object[]) taskIds);
    this.taskIds = taskIds;
    return this;
  }

  public HistoricVariableInstanceQuery executionIdIn(String... executionIds) {
    ensureNotNull("Execution Ids", (Object[]) executionIds);
    this.executionIds = executionIds;
    return this;
  }

  public HistoricVariableInstanceQuery caseExecutionIdIn(String... caseExecutionIds) {
    ensureNotNull("Case execution ids", (Object[]) caseExecutionIds);
    this.caseExecutionIds = caseExecutionIds;
    return this;
  }

  public HistoricVariableInstanceQuery activityInstanceIdIn(String... activityInstanceIds) {
    ensureNotNull("Activity Instance Ids", (Object[]) activityInstanceIds);
    this.activityInstanceIds = activityInstanceIds;
    return this;
  }

  public HistoricVariableInstanceQuery variableName(String variableName) {
    ensureNotNull("variableName", variableName);
    this.variableName = variableName;
    return this;
  }

  public HistoricVariableInstanceQuery variableValueEquals(String variableName, Object variableValue) {
    ensureNotNull("variableName", variableName);
    ensureNotNull("variableValue", variableValue);
    this.variableName = variableName;
    queryVariableValue = new QueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true);
    return this;
  }

  public HistoricVariableInstanceQuery variableNameLike(String variableNameLike) {
    ensureNotNull("variableNameLike", variableNameLike);
    this.variableNameLike = variableNameLike;
    return this;
  }

  protected void ensureVariablesInitialized() {
    if (this.queryVariableValue != null) {
      VariableSerializers variableSerializers = Context.getProcessEngineConfiguration().getVariableSerializers();
      queryVariableValue.initialize(variableSerializers);
    }
  }

  public HistoricVariableInstanceQuery disableBinaryFetching() {
    isByteArrayFetchingEnabled = false;
    return this;
  }

  public HistoricVariableInstanceQuery disableCustomObjectDeserialization() {
    this.isCustomObjectDeserializationEnabled = false;
    return this;
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext.getHistoricVariableInstanceManager().findHistoricVariableInstanceCountByQueryCriteria(this);
  }

  public List<HistoricVariableInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    List<HistoricVariableInstance> historicVariableInstances = commandContext
            .getHistoricVariableInstanceManager()
            .findHistoricVariableInstancesByQueryCriteria(this, page);

    if (historicVariableInstances!=null) {
      for (HistoricVariableInstance historicVariableInstance: historicVariableInstances) {

        HistoricVariableInstanceEntity variableInstanceEntity = (HistoricVariableInstanceEntity) historicVariableInstance;
        if (shouldFetchValue(variableInstanceEntity)) {
          try {
            variableInstanceEntity.getTypedValue(isCustomObjectDeserializationEnabled);

          } catch(Exception t) {
            // do not fail if one of the variables fails to load
            LOGGER.log(Level.FINE, "Exception while getting value for variable", t);
          }
        }

      }
    }
    return historicVariableInstances;
  }

  protected boolean shouldFetchValue(HistoricVariableInstanceEntity entity) {
    // do not fetch values for byte arrays eagerly (unless requested by the user)
    return isByteArrayFetchingEnabled || !ValueType.BYTES.equals(entity.getSerializer().getType());
  }

  // order by /////////////////////////////////////////////////////////////////

  public HistoricVariableInstanceQuery orderByProcessInstanceId() {
    orderBy(HistoricVariableInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricVariableInstanceQuery orderByVariableName() {
    orderBy(HistoricVariableInstanceQueryProperty.VARIABLE_NAME);
    return this;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String[] getActivityInstanceIds() {
    return activityInstanceIds;
  }

  public String[] getTaskIds() {
    return taskIds;
  }

  public String[] getExecutionIds() {
    return executionIds;
  }

  public String[] getCaseExecutionIds() {
    return caseExecutionIds;
  }

  public String getVariableName() {
    return variableName;
  }

  public String getVariableNameLike() {
    return variableNameLike;
  }

  public QueryVariableValue getQueryVariableValue() {
    return queryVariableValue;
  }

}
