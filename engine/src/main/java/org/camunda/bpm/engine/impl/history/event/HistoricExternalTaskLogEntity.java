/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.history.event;

import org.camunda.bpm.engine.history.ExternalTaskState;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.repository.ResourceTypes;

import java.util.Date;

import static org.camunda.bpm.engine.impl.util.ExceptionUtil.createExceptionByteArray;
import static org.camunda.bpm.engine.impl.util.StringUtil.toByteArray;

public class HistoricExternalTaskLogEntity extends HistoryEvent implements HistoricExternalTaskLog {

  private static final long serialVersionUID = 1L;
  private static final String EXCEPTION_NAME = "historicExternalTaskLog.exceptionByteArray";

  protected Date timestamp;

  protected String externalTaskId;

  protected String topicName;
  protected String workerId;
  protected long priority;
  protected Integer retries;

  protected String errorMessage;

  protected String errorDetailsByteArrayId;
  protected String activityId;

  protected String activityInstanceId;
  protected String tenantId;

  protected int state;

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getExternalTaskId() {
    return externalTaskId;
  }

  public void setExternalTaskId(String externalTaskId) {
    this.externalTaskId = externalTaskId;
  }

  public String getTopicName() {
    return topicName;
  }

  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

  public String getWorkerId() {
    return workerId;
  }

  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }

  public Integer getRetries() {
    return retries;
  }

  public void setRetries(Integer retries) {
    this.retries = retries;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    // note: it is not a clean way to truncate where the history event is produced, since truncation is only
    //   relevant for relational history databases that follow our schema restrictions;
    //   a similar problem exists in ExternalTaskEntity#setErrorMessage where truncation may not be required for custom
    //   persistence implementations
    if(errorMessage != null && errorMessage.length() > ExternalTaskEntity.MAX_EXCEPTION_MESSAGE_LENGTH) {
      this.errorMessage = errorMessage.substring(0, ExternalTaskEntity.MAX_EXCEPTION_MESSAGE_LENGTH);
    } else {
      this.errorMessage = errorMessage;
    }
  }

  public String getErrorDetailsByteArrayId() {
    return errorDetailsByteArrayId;
  }

  public void setErrorDetailsByteArrayId(String errorDetailsByteArrayId) {
    this.errorDetailsByteArrayId = errorDetailsByteArrayId;
  }

  public String getErrorDetails() {
    ByteArrayEntity byteArray = getErrorByteArray();
    return ExceptionUtil.getExceptionStacktrace(byteArray);
  }

  public void setErrorDetails(String exception) {
    EnsureUtil.ensureNotNull("exception", exception);

    byte[] exceptionBytes = toByteArray(exception);
    ByteArrayEntity byteArray = createExceptionByteArray(EXCEPTION_NAME, exceptionBytes, ResourceTypes.HISTORY);
    byteArray.setRootProcessInstanceId(rootProcessInstanceId);
    byteArray.setRemovalTime(removalTime);

    errorDetailsByteArrayId = byteArray.getId();
  }

  protected ByteArrayEntity getErrorByteArray() {
    if (errorDetailsByteArrayId != null) {
      return Context
          .getCommandContext()
          .getDbEntityManager()
          .selectById(ByteArrayEntity.class, errorDetailsByteArrayId);
    }
    return null;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public long getPriority() {
    return priority;
  }

  public void setPriority(long priority) {
    this.priority = priority;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  @Override
  public boolean isCreationLog() {
    return state == ExternalTaskState.CREATED.getStateCode();
  }

  @Override
  public boolean isFailureLog() {
    return state == ExternalTaskState.FAILED.getStateCode();
  }

  @Override
  public boolean isSuccessLog() {
    return state == ExternalTaskState.SUCCESSFUL.getStateCode();
  }

  @Override
  public boolean isDeletionLog() {
    return state == ExternalTaskState.DELETED.getStateCode();
  }

  @Override
  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
  }

}
