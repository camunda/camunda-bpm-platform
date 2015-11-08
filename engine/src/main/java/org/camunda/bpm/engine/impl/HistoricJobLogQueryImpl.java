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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsEmptyString;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.history.JobState;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.impl.util.CompareUtil;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricJobLogQueryImpl extends AbstractQuery<HistoricJobLogQuery, HistoricJobLog> implements HistoricJobLogQuery {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String jobId;
  protected String jobExceptionMessage;
  protected String jobDefinitionId;
  protected String jobDefinitionType;
  protected String jobDefinitionConfiguration;
  protected String[] activityIds;
  protected String[] executionIds;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String deploymentId;
  protected JobState state;
  protected Long jobPriorityHigherThanOrEqual;
  protected Long jobPriorityLowerThanOrEqual;

  public HistoricJobLogQueryImpl() {
  }

  public HistoricJobLogQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  // query parameter ////////////////////////////////////////////

  public HistoricJobLogQuery logId(String historicJobLogId) {
    ensureNotNull(NotValidException.class, "historicJobLogId", historicJobLogId);
    this.id = historicJobLogId;
    return this;
  }

  public HistoricJobLogQuery jobId(String jobId) {
    ensureNotNull(NotValidException.class, "jobId", jobId);
    this.jobId = jobId;
    return this;
  }

  public HistoricJobLogQuery jobExceptionMessage(String jobExceptionMessage) {
    ensureNotNull(NotValidException.class, "jobExceptionMessage", jobExceptionMessage);
    this.jobExceptionMessage = jobExceptionMessage;
    return this;
  }

  public HistoricJobLogQuery jobDefinitionId(String jobDefinitionId) {
    ensureNotNull(NotValidException.class, "jobDefinitionId", jobDefinitionId);
    this.jobDefinitionId = jobDefinitionId;
    return this;
  }

  public HistoricJobLogQuery jobDefinitionType(String jobDefinitionType) {
    ensureNotNull(NotValidException.class, "jobDefinitionType", jobDefinitionType);
    this.jobDefinitionType = jobDefinitionType;
    return this;
  }

  public HistoricJobLogQuery jobDefinitionConfiguration(String jobDefinitionConfiguration) {
    ensureNotNull(NotValidException.class, "jobDefinitionConfiguration", jobDefinitionConfiguration);
    this.jobDefinitionConfiguration = jobDefinitionConfiguration;
    return this;
  }

  public HistoricJobLogQuery activityIdIn(String... activityIds) {
    List<String> activityIdList = CollectionUtil.asArrayList(activityIds);
    ensureNotContainsNull("activityIds", activityIdList);
    ensureNotContainsEmptyString("activityIds", activityIdList);
    this.activityIds = activityIds;
    return this;
  }

  public HistoricJobLogQuery executionIdIn(String... executionIds) {
    List<String> executionIdList = CollectionUtil.asArrayList(executionIds);
    ensureNotContainsNull("executionIds", executionIdList);
    ensureNotContainsEmptyString("executionIds", executionIdList);
    this.executionIds = executionIds;
    return this;
  }

  public HistoricJobLogQuery processInstanceId(String processInstanceId) {
    ensureNotNull(NotValidException.class, "processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricJobLogQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull(NotValidException.class, "processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public HistoricJobLogQuery processDefinitionKey(String processDefinitionKey) {
    ensureNotNull(NotValidException.class, "processDefinitionKey", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public HistoricJobLogQuery deploymentId(String deploymentId) {
    ensureNotNull(NotValidException.class, "deploymentId", deploymentId);
    this.deploymentId = deploymentId;
    return this;
  }

  public HistoricJobLogQuery jobPriorityHigherThanOrEquals(long priority) {
    this.jobPriorityHigherThanOrEqual = priority;
    return this;
  }

  public HistoricJobLogQuery jobPriorityLowerThanOrEquals(long priority) {
    this.jobPriorityLowerThanOrEqual = priority;
    return this;
  }

  public HistoricJobLogQuery creationLog() {
    setState(JobState.CREATED);
    return this;
  }

  public HistoricJobLogQuery failureLog() {
    setState(JobState.FAILED);
    return this;
  }

  public HistoricJobLogQuery successLog() {
    setState(JobState.SUCCESSFUL);
    return this;
  }

  public HistoricJobLogQuery deletionLog() {
    setState(JobState.DELETED);
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions()
      || CompareUtil.areNotInAscendingOrder(jobPriorityHigherThanOrEqual, jobPriorityLowerThanOrEqual);
  }

  // order by //////////////////////////////////////////////

  public HistoricJobLogQuery orderByTimestamp() {
    orderBy(HistoricJobLogQueryProperty.TIMESTAMP);
    return this;
  }

  public HistoricJobLogQuery orderByJobId() {
    orderBy(HistoricJobLogQueryProperty.JOB_ID);
    return this;
  }

  public HistoricJobLogQuery orderByJobDueDate() {
    orderBy(HistoricJobLogQueryProperty.DUEDATE);
    return this;
  }

  public HistoricJobLogQuery orderByJobRetries() {
    orderBy(HistoricJobLogQueryProperty.RETRIES);
    return this;
  }

  public HistoricJobLogQuery orderByJobPriority() {
    orderBy(HistoricJobLogQueryProperty.PRIORITY);
    return this;
  }

  public HistoricJobLogQuery orderByJobDefinitionId() {
    orderBy(HistoricJobLogQueryProperty.JOB_DEFINITION_ID);
    return this;
  }

  public HistoricJobLogQuery orderByActivityId() {
    orderBy(HistoricJobLogQueryProperty.ACTIVITY_ID);
    return this;
  }

  public HistoricJobLogQuery orderByExecutionId() {
    orderBy(HistoricJobLogQueryProperty.EXECUTION_ID);
    return this;
  }

  public HistoricJobLogQuery orderByProcessInstanceId() {
    orderBy(HistoricJobLogQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricJobLogQuery orderByProcessDefinitionId() {
    orderBy(HistoricJobLogQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricJobLogQuery orderByProcessDefinitionKey() {
    orderBy(HistoricJobLogQueryProperty.PROCESS_DEFINITION_KEY);
    return this;
  }

  public HistoricJobLogQuery orderByDeploymentId() {
    orderBy(HistoricJobLogQueryProperty.DEPLOYMENT_ID);
    return this;
  }

  public HistoricJobLogQuery orderPartiallyByOccurrence() {
    orderBy(HistoricJobLogQueryProperty.SEQUENCE_COUNTER);
    return this;
  }

  // results //////////////////////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricJobLogManager()
      .findHistoricJobLogsCountByQueryCriteria(this);
  }

  public List<HistoricJobLog> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
        .getHistoricJobLogManager()
        .findHistoricJobLogsByQueryCriteria(this, page);
  }

  // getter //////////////////////////////////

  public String getJobId() {
    return jobId;
  }

  public String getJobExceptionMessage() {
    return jobExceptionMessage;
  }

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public String getJobDefinitionType() {
    return jobDefinitionType;
  }

  public String getJobDefinitionConfiguration() {
    return jobDefinitionConfiguration;
  }

  public String[] getActivityIds() {
    return activityIds;
  }

  public String[] getExecutionIds() {
    return executionIds;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public JobState getState() {
    return state;
  }

  // setter //////////////////////////////////

  protected void setState(JobState state) {
    this.state = state;
  }

}
