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

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailQueryImpl extends AbstractQuery<HistoricDetailQuery, HistoricDetail> implements HistoricDetailQuery {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  private static final long serialVersionUID = 1L;
  protected String detailId;
  protected String taskId;
  protected String processInstanceId;
  protected String caseInstanceId;
  protected String executionId;
  protected String caseExecutionId;
  protected String activityId;
  protected String activityInstanceId;
  protected String type;
  protected String variableInstanceId;
  protected String[] variableTypes;
  protected String[] tenantIds;
  protected String[] processInstanceIds;
  protected String userOperationId;
  protected Long sequenceCounter;
  protected Date occurredBefore;
  protected Date occurredAfter;

  protected boolean excludeTaskRelated = false;
  protected boolean isByteArrayFetchingEnabled = true;
  protected boolean isCustomObjectDeserializationEnabled = true;

  public HistoricDetailQueryImpl() {
  }

  public HistoricDetailQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricDetailQuery detailId(String id) {
    ensureNotNull("detailId", id);
    this.detailId = id;
    return this;
  }

  public HistoricDetailQuery variableInstanceId(String variableInstanceId) {
    ensureNotNull("variableInstanceId", variableInstanceId);
    this.variableInstanceId = variableInstanceId;
    return this;
  }

  public HistoricDetailQuery variableTypeIn(String... variableTypes) {
    ensureNotNull("Variable types", (Object[]) variableTypes);
    this.variableTypes = lowerCase(variableTypes);
    return this;
  }

  private String[] lowerCase(String... variableTypes) {
    for (int i = 0; i < variableTypes.length; i++) {
      variableTypes[i] = variableTypes[i].toLowerCase();
    }
    return variableTypes;
  }

  public HistoricDetailQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricDetailQuery caseInstanceId(String caseInstanceId) {
    ensureNotNull("Case instance id", caseInstanceId);
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public HistoricDetailQuery executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public HistoricDetailQuery caseExecutionId(String caseExecutionId) {
    ensureNotNull("Case execution id", caseExecutionId);
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  public HistoricDetailQuery activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public HistoricDetailQuery activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public HistoricDetailQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  public HistoricDetailQuery formProperties() {
    this.type = "FormProperty";
    return this;
  }

  public HistoricDetailQuery formFields() {
    this.type = "FormProperty";
    return this;
  }

  public HistoricDetailQuery variableUpdates() {
    this.type = "VariableUpdate";
    return this;
  }

  public HistoricDetailQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    return this;
  }

  public HistoricDetailQuery processInstanceIdIn(String... processInstanceIds) {
    ensureNotNull("Process Instance Ids", (Object[]) processInstanceIds);
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  public HistoricDetailQuery userOperationId(String userOperationId) {
    ensureNotNull("userOperationId", userOperationId);
    this.userOperationId = userOperationId;
    return this;
  }

  public HistoricDetailQueryImpl sequenceCounter(long sequenceCounter) {
    this.sequenceCounter = sequenceCounter;
    return this;
  }

  public HistoricDetailQuery excludeTaskDetails() {
    this.excludeTaskRelated = true;
    return this;
  }

  public HistoricDetailQuery occurredBefore(Date date) {
    ensureNotNull("occurred before", date);
    occurredBefore = date;
    return this;
  }

  public HistoricDetailQuery occurredAfter(Date date) {
    ensureNotNull("occurred after", date);
    occurredAfter = date;
    return this;
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricDetailManager()
      .findHistoricDetailCountByQueryCriteria(this);
  }

  public HistoricDetailQuery disableBinaryFetching() {
    this.isByteArrayFetchingEnabled = false;
    return this;
  }

  public HistoricDetailQuery disableCustomObjectDeserialization() {
    this.isCustomObjectDeserializationEnabled = false;
    return this;
  }

  public List<HistoricDetail> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    List<HistoricDetail> historicDetails = commandContext
      .getHistoricDetailManager()
      .findHistoricDetailsByQueryCriteria(this, page);
    if (historicDetails!=null) {
      for (HistoricDetail historicDetail: historicDetails) {
        if (historicDetail instanceof HistoricDetailVariableInstanceUpdateEntity) {
          HistoricDetailVariableInstanceUpdateEntity entity = (HistoricDetailVariableInstanceUpdateEntity) historicDetail;
          if (shouldFetchValue(entity)) {
            try {
              entity.getTypedValue(isCustomObjectDeserializationEnabled);

            } catch(Exception t) {
              // do not fail if one of the variables fails to load
              LOG.exceptionWhileGettingValueForVariable(t);
            }
          }

        }
      }
    }
    return historicDetails;
  }

  protected boolean shouldFetchValue(HistoricDetailVariableInstanceUpdateEntity entity) {
    // do not fetch values for byte arrays eagerly (unless requested by the user)
    return isByteArrayFetchingEnabled
        || !AbstractTypedValueSerializer.BINARY_VALUE_TYPES.contains(entity.getSerializer().getType().getName());
  }

  // order by /////////////////////////////////////////////////////////////////

  public HistoricDetailQuery orderByProcessInstanceId() {
    orderBy(HistoricDetailQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricDetailQuery orderByTime() {
    orderBy(HistoricDetailQueryProperty.TIME);
    return this;
  }

  public HistoricDetailQuery orderByVariableName() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_NAME);
    return this;
  }

  public HistoricDetailQuery orderByFormPropertyId() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_NAME);
    return this;
  }

  public HistoricDetailQuery orderByVariableRevision() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_REVISION);
    return this;
  }

  public HistoricDetailQuery orderByVariableType() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_TYPE);
    return this;
  }

  public HistoricDetailQuery orderPartiallyByOccurrence() {
    orderBy(HistoricDetailQueryProperty.SEQUENCE_COUNTER);
    return this;
  }

  public HistoricDetailQuery orderByTenantId() {
    return orderBy(HistoricDetailQueryProperty.TENANT_ID);
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getType() {
    return type;
  }

  public boolean getExcludeTaskRelated() {
    return excludeTaskRelated;
  }

  public String getDetailId() {
    return detailId;
  }

  public String[] getProcessInstanceIds() {
    return processInstanceIds;
  }

  public Date getOccurredBefore() {
    return occurredBefore;
  }

  public Date getOccurredAfter() {
    return occurredAfter;
  }
}
