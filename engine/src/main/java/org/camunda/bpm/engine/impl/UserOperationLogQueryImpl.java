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

import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.CompareUtil;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Danny Gräf
 */
public class UserOperationLogQueryImpl extends AbstractQuery<UserOperationLogQuery, UserOperationLogEntry> implements UserOperationLogQuery {

  private static final long serialVersionUID = 1L;
  protected String deploymentId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processInstanceId;
  protected String executionId;
  protected String caseDefinitionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String taskId;
  protected String jobId;
  protected String jobDefinitionId;
  protected String batchId;
  protected String userId;
  protected String operationId;
  protected String operationType;
  protected String property;
  protected String entityType;
  protected Date timestampAfter;
  protected Date timestampBefore;

  protected String[] entityTypes;

  public UserOperationLogQueryImpl() {
  }

  public UserOperationLogQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public UserOperationLogQuery deploymentId(String deploymentId) {
    ensureNotNull("deploymentId", deploymentId);
    this.deploymentId = deploymentId;
    return this;
  }

  public UserOperationLogQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public UserOperationLogQuery processDefinitionKey(String processDefinitionKey) {
    ensureNotNull("processDefinitionKey", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public UserOperationLogQuery processInstanceId(String processInstanceId) {
    ensureNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public UserOperationLogQuery executionId(String executionId) {
    ensureNotNull("executionId", executionId);
    this.executionId = executionId;
    return this;
  }

  public UserOperationLogQuery caseDefinitionId(String caseDefinitionId) {
    ensureNotNull("caseDefinitionId", caseDefinitionId);
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  public UserOperationLogQuery caseInstanceId(String caseInstanceId) {
    ensureNotNull("caseInstanceId", caseInstanceId);
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public UserOperationLogQuery caseExecutionId(String caseExecutionId) {
    ensureNotNull("caseExecutionId", caseExecutionId);
    this.caseExecutionId = caseExecutionId;
    return this;
  }


  public UserOperationLogQuery taskId(String taskId) {
    ensureNotNull("taskId", taskId);
    this.taskId = taskId;
    return this;
  }

  public UserOperationLogQuery jobId(String jobId) {
    ensureNotNull("jobId", jobId);
    this.jobId = jobId;
    return this;
  }

  public UserOperationLogQuery jobDefinitionId(String jobDefinitionId) {
    ensureNotNull("jobDefinitionId", jobDefinitionId);
    this.jobDefinitionId = jobDefinitionId;
    return this;
  }

  public UserOperationLogQuery batchId(String batchId) {
    ensureNotNull("batchId", batchId);
    this.batchId = batchId;
    return this;
  }

  public UserOperationLogQuery userId(String userId) {
    ensureNotNull("userId", userId);
    this.userId = userId;
    return this;
  }

  public UserOperationLogQuery operationId(String operationId) {
    ensureNotNull("operationId", operationId);
    this.operationId = operationId;
    return this;
  }

  public UserOperationLogQuery operationType(String operationType) {
    ensureNotNull("operationType", operationType);
    this.operationType = operationType;
    return this;
  }

  public UserOperationLogQuery property(String property) {
    ensureNotNull("property", property);
    this.property = property;
    return this;
  }

  public UserOperationLogQuery entityType(String entityType) {
    ensureNotNull("entityType", entityType);
    this.entityType = entityType;
    return this;
  }

  public UserOperationLogQuery entityTypeIn(String... entityTypes) {
    ensureNotNull("entity types", (Object[]) entityTypes);
    this.entityTypes = entityTypes;
    return this;
  }

  public UserOperationLogQuery afterTimestamp(Date after) {
    this.timestampAfter = after;
    return this;
  }

  public UserOperationLogQuery beforeTimestamp(Date before) {
    this.timestampBefore = before;
    return this;
  }

  public UserOperationLogQuery orderByTimestamp() {
    return orderBy(OperationLogQueryProperty.TIMESTAMP);
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getOperationLogManager()
      .findOperationLogEntryCountByQueryCriteria(this);
  }

  public List<UserOperationLogEntry> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
        .getOperationLogManager()
        .findOperationLogEntriesByQueryCriteria(this, page);
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions() || CompareUtil.areNotInAscendingOrder(timestampAfter, timestampBefore);
  }
}
