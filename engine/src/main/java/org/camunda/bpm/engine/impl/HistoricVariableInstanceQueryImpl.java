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

import java.util.List;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.variable.VariableTypes;

/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceQueryImpl extends AbstractQuery<HistoricVariableInstanceQuery, HistoricVariableInstance> implements
        HistoricVariableInstanceQuery {

  private static final long serialVersionUID = 1L;
  protected String processInstanceId;
  protected String variableName;
  protected String variableNameLike;
  protected QueryVariableValue queryVariableValue;
  protected String[] taskIds;
  protected String[] executionIds;
  protected String[] activityInstanceIds;

  public HistoricVariableInstanceQueryImpl() {
  }

  public HistoricVariableInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public HistoricVariableInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricVariableInstanceQueryImpl processInstanceId(String processInstanceId) {
    assertParamNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricVariableInstanceQuery taskIdIn(String... taskIds) {
    assertParamNotNull("Task Ids", taskIds);
    this.taskIds = taskIds;
    return this;
  }

  public HistoricVariableInstanceQuery executionIdIn(String... executionIds) {
    assertParamNotNull("Execution Ids", executionIds);
    this.executionIds = executionIds;
    return this;
  }

  public HistoricVariableInstanceQuery activityInstanceIdIn(String... activityInstanceIds) {
    assertParamNotNull("Activity Instance Ids", activityInstanceIds);
    this.activityInstanceIds = activityInstanceIds;
    return this;
  }

  public HistoricVariableInstanceQuery variableName(String variableName) {
    assertParamNotNull("variableName", variableName);
    this.variableName = variableName;
    return this;
  }

  public HistoricVariableInstanceQuery variableValueEquals(String variableName, Object variableValue) {
    assertParamNotNull("variableName", variableName);
    assertParamNotNull("variableValue", variableValue);
    this.variableName = variableName;
    queryVariableValue = new QueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true);
    return this;
  }

  public HistoricVariableInstanceQuery variableNameLike(String variableNameLike) {
    assertParamNotNull("variableNameLike", variableNameLike);
    this.variableNameLike = variableNameLike;
    return this;
  }

  protected void ensureVariablesInitialized() {
    if (this.queryVariableValue != null) {
      VariableTypes variableTypes = Context.getProcessEngineConfiguration().getVariableTypes();
      queryVariableValue.initialize(variableTypes);
    }
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
        if (historicVariableInstance instanceof HistoricVariableInstanceEntity) {
          ((HistoricVariableInstanceEntity)historicVariableInstance).getByteArrayValue();
        }
      }
    }
    return historicVariableInstances;
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

  public String[] getActivityInstanceIds() {
    return activityInstanceIds;
  }

  public String[] getTaskIds() {
    return taskIds;
  }

  public String[] getExecutionIds() {
    return executionIds;
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
