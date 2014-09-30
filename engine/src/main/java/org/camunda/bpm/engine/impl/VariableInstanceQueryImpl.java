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

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.camunda.bpm.engine.delegate.ProcessEngineVariableType;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;

/**
 * @author roman.smirnov
 */
public class VariableInstanceQueryImpl extends AbstractVariableQueryImpl<VariableInstanceQuery, VariableInstance> implements VariableInstanceQuery, Serializable {

  private final static Logger LOGGER = Logger.getLogger(VariableInstanceQuery.class.getName());

  private static final long serialVersionUID = 1L;

  protected String variableId;
  protected String variableName;
  protected String[] variableNames;
  protected String variableNameLike;
  protected String[] executionIds;
  protected String[] processInstanceIds;
  protected String[] caseExecutionIds;
  protected String[] caseInstanceIds;
  protected String[] taskIds;
  protected String[] variableScopeIds;
  protected String[] activityInstanceIds;

  protected boolean isByteArrayFetchingEnabled = true;
  protected boolean isCustomObjectDeserializationEnabled = true;

  public VariableInstanceQueryImpl() { }

  public VariableInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public VariableInstanceQuery variableId(String id) {
    ensureNotNull("id", id);
    this.variableId = id;
    return this;
  }

  public VariableInstanceQuery variableName(String variableName) {
    this.variableName = variableName;
    return this;
  }

  public VariableInstanceQuery variableNameIn(String... variableNames) {
    this.variableNames = variableNames;
    return this;
  }

  public VariableInstanceQuery variableNameLike(String variableNameLike) {
    this.variableNameLike = variableNameLike;
    return this;
  }

  public VariableInstanceQuery executionIdIn(String... executionIds) {
    this.executionIds = executionIds;
    return this;
  }

  public VariableInstanceQuery processInstanceIdIn(String... processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  public VariableInstanceQuery caseExecutionIdIn(String... caseExecutionIds) {
    this.caseExecutionIds = caseExecutionIds;
    return this;
  }

  public VariableInstanceQuery caseInstanceIdIn(String... caseInstanceIds) {
    this.caseInstanceIds = caseInstanceIds;
    return this;
  }

  public VariableInstanceQuery taskIdIn(String... taskIds) {
    this.taskIds = taskIds;
    return this;
  }

  public VariableInstanceQuery variableScopeIdIn(String... variableScopeIds) {
    this.variableScopeIds = variableScopeIds;
    return this;
  }

  public VariableInstanceQuery activityInstanceIdIn(String... activityInstanceIds) {
    this.activityInstanceIds = activityInstanceIds;
    return this;
  }

  public VariableInstanceQuery disableBinaryFetching() {
    this.isByteArrayFetchingEnabled = false;
    return this;
  }

  public VariableInstanceQuery disableCustomObjectDeserialization() {
    this.isCustomObjectDeserializationEnabled = false;
    return this;
  }

  // ordering ////////////////////////////////////////////////////

  public VariableInstanceQuery orderByVariableName() {
    orderBy(VariableInstanceQueryProperty.VARIABLE_NAME);
    return this;
  }

  public VariableInstanceQuery orderByVariableType() {
    orderBy(VariableInstanceQueryProperty.VARIABLE_TYPE);
    return this;
  }

  public VariableInstanceQuery orderByActivityInstanceId() {
    orderBy(VariableInstanceQueryProperty.ACTIVITY_INSTANCE_ID);
    return this;
  }

  // results ////////////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getVariableInstanceManager()
      .findVariableInstanceCountByQueryCriteria(this);
  }

  public List<VariableInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    List<VariableInstance> result = commandContext
      .getVariableInstanceManager()
      .findVariableInstanceByQueryCriteria(this, page);

    if (result == null) {
      return result;
    }

    // iterate over the result array to initialize the value and serialized value of the variable
    for (VariableInstance variableInstance : result) {
      VariableInstanceEntity variableInstanceEntity = (VariableInstanceEntity) variableInstance;

      if (shouldFetchSerializedValueFor(variableInstanceEntity)) {
        try {
          variableInstanceEntity.getSerializedValue();

          if (shouldFetchValueFor(variableInstanceEntity)) {
            variableInstanceEntity.getValue();
          }

        } catch(Exception t) {
          // do not fail if one of the variables fails to load
          LOGGER.log(Level.FINE, "Exception while getting value for variable", t);
        }
      }
    }

    return result;
  }

  /**
   * eagerly fetch the variable's value unless the serialized value should not be fetched
   * or custom object fetching is disabled
   */
  protected boolean shouldFetchValueFor(VariableInstanceEntity variableInstance) {
    boolean shouldFetchCustomObjects = !variableInstance.storesCustomObjects() || isCustomObjectDeserializationEnabled;

    return shouldFetchSerializedValueFor(variableInstance) && shouldFetchCustomObjects;
  }

  /**
   * Eagerly fetch the variable's serialized value unless the type is "bytes" and
   * binary fetching disabled
   */
  protected boolean shouldFetchSerializedValueFor(VariableInstanceEntity variableInstance) {
    boolean shouldFetchBytes = !ProcessEngineVariableType.BYTES.getName().equals(variableInstance.getType().getTypeName())
        || isByteArrayFetchingEnabled;

    return shouldFetchBytes;
  }

  // getters ////////////////////////////////////////////////////

  public String getVariableId() {
    return variableId;
  }

  public String getVariableName() {
    return variableName;
  }

  public String[] getVariableNames() {
    return variableNames;
  }

  public String getVariableNameLike() {
    return variableNameLike;
  }

  public String[] getExecutionIds() {
    return executionIds;
  }

  public String[] getProcessInstanceIds() {
    return processInstanceIds;
  }

  public String[] getCaseExecutionIds() {
    return caseExecutionIds;
  }

  public String[] getCaseInstanceIds() {
    return caseInstanceIds;
  }

  public String[] getTaskIds() {
    return taskIds;
  }

  public String[] getVariableScopeIds() {
    return variableScopeIds;
  }

  public String[] getActivityInstanceIds() {
    return activityInstanceIds;
  }

}
