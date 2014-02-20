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


/**
 * @author Danny Gräf
 */
public class UserOperationLogQueryImpl extends AbstractQuery<UserOperationLogQuery, UserOperationLogEntry> implements UserOperationLogQuery {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String executionId;
  protected String taskId;
  protected String userId;
  protected String operationId;
  protected String operationType;
  protected String property;
  protected String entityType;
  protected Date timestampAfter;
  protected Date timestampBefore;

  public UserOperationLogQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public UserOperationLogQuery processDefinitionId(String processDefinitionId) {
    assertParamNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public UserOperationLogQuery processInstanceId(String processInstanceId) {
    assertParamNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public UserOperationLogQuery executionId(String executionId) {
    assertParamNotNull("executionId", executionId);
    this.executionId = executionId;
    return this;
  }

  public UserOperationLogQuery taskId(String taskId) {
    assertParamNotNull("taskId", taskId);
    this.taskId = taskId;
    return this;
  }

  public UserOperationLogQuery userId(String userId) {
    assertParamNotNull("userId", userId);
    this.userId = userId;
    return this;
  }

  public UserOperationLogQuery operationId(String operationId) {
    assertParamNotNull("operationId", operationId);
    this.operationId = operationId;
    return this;
  }

  public UserOperationLogQuery operationType(String operationType) {
    assertParamNotNull("operationType", operationType);
    this.operationType = operationType;
    return this;
  }

  public UserOperationLogQuery property(String property) {
    assertParamNotNull("property", property);
    this.property = property;
    return this;
  }

  public UserOperationLogQuery entityType(String entityType) {
    assertParamNotNull("entityType", entityType);
    this.entityType = entityType;
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
}
