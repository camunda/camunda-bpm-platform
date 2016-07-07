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
package org.camunda.bpm.engine.impl.history.event;
import java.util.Date;

import org.camunda.bpm.engine.history.JobState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricJobLogEvent extends HistoryEvent {

  private static final long serialVersionUID = 1L;

  protected Date timestamp;

  protected String jobId;

  protected Date jobDueDate;

  protected int jobRetries;

  protected long jobPriority;

  protected String jobExceptionMessage;

  protected String exceptionByteArrayId;

  protected String jobDefinitionId;

  protected String jobDefinitionType;

  protected String jobDefinitionConfiguration;

  protected String activityId;

  protected String deploymentId;

  protected int state;

  protected String tenantId;

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public Date getJobDueDate() {
    return jobDueDate;
  }

  public void setJobDueDate(Date jobDueDate) {
    this.jobDueDate = jobDueDate;
  }

  public int getJobRetries() {
    return jobRetries;
  }

  public void setJobRetries(int jobRetries) {
    this.jobRetries = jobRetries;
  }

  public long getJobPriority() {
    return jobPriority;
  }

  public void setJobPriority(long jobPriority) {
    this.jobPriority = jobPriority;
  }

  public String getJobExceptionMessage() {
    return jobExceptionMessage;
  }

  public void setJobExceptionMessage(String jobExceptionMessage) {
    // note: it is not a clean way to truncate where the history event is produced, since truncation is only
    //   relevant for relational history databases that follow our schema restrictions;
    //   a similar problem exists in JobEntity#setExceptionMessage where truncation may not be required for custom
    //   persistence implementations
    if(jobExceptionMessage != null && jobExceptionMessage.length() > JobEntity.MAX_EXCEPTION_MESSAGE_LENGTH) {
      this.jobExceptionMessage = jobExceptionMessage.substring(0, JobEntity.MAX_EXCEPTION_MESSAGE_LENGTH);
    } else {
      this.jobExceptionMessage = jobExceptionMessage;
    }
  }

  public String getExceptionByteArrayId() {
    return exceptionByteArrayId;
  }

  public void setExceptionByteArrayId(String exceptionByteArrayId) {
    this.exceptionByteArrayId = exceptionByteArrayId;
  }

  public String getExceptionStacktrace() {
    ByteArrayEntity byteArray = getExceptionByteArray();
    return ExceptionUtil.getExceptionStacktrace(byteArray);
  }

  protected ByteArrayEntity getExceptionByteArray() {
    if (exceptionByteArrayId != null) {
      return Context
        .getCommandContext()
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, exceptionByteArrayId);
    }

    return null;
  }

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  public String getJobDefinitionType() {
    return jobDefinitionType;
  }

  public void setJobDefinitionType(String jobDefinitionType) {
    this.jobDefinitionType = jobDefinitionType;
  }

  public String getJobDefinitionConfiguration() {
    return jobDefinitionConfiguration;
  }

  public void setJobDefinitionConfiguration(String jobDefinitionConfiguration) {
    this.jobDefinitionConfiguration = jobDefinitionConfiguration;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public boolean isCreationLog() {
    return state == JobState.CREATED.getStateCode();
  }

  public boolean isFailureLog() {
    return state == JobState.FAILED.getStateCode();
  }

  public boolean isSuccessLog() {
    return state == JobState.SUCCESSFUL.getStateCode();
  }

  public boolean isDeletionLog() {
    return state == JobState.DELETED.getStateCode();
  }

}
