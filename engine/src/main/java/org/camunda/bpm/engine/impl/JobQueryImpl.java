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
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.CompareUtil;
import org.camunda.bpm.engine.impl.util.ImmutablePair;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;


/**
 * @author Joram Barrez
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class JobQueryImpl extends AbstractQuery<JobQuery, Job> implements JobQuery, Serializable {

  private static final long serialVersionUID = 1L;
  protected String activityId;
  protected String id;
  protected Set<String> ids;
  protected String jobDefinitionId;
  protected String processInstanceId;
  protected Set<String> processInstanceIds;
  protected String executionId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected boolean retriesLeft;
  protected boolean executable;
  protected boolean onlyTimers;
  protected boolean onlyMessages;
  protected Date duedateHigherThan;
  protected Date duedateLowerThan;
  protected Date duedateHigherThanOrEqual;
  protected Date duedateLowerThanOrEqual;
  protected Date createdBefore;
  protected Date createdAfter;
  protected Long priorityHigherThanOrEqual;
  protected Long priorityLowerThanOrEqual;
  protected boolean withException;
  protected String exceptionMessage;
  protected String failedActivityId;
  protected boolean noRetriesLeft;
  protected SuspensionState suspensionState;

  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;
  protected boolean includeJobsWithoutTenantId = false;

  public JobQueryImpl() {
  }

  public JobQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public JobQuery jobId(String jobId) {
    ensureNotNull("Provided job id", jobId);
    this.id = jobId;
    return this;
  }

  public JobQuery jobIds(Set<String> ids) {
    ensureNotEmpty("Set of job ids", ids);
    this.ids = ids;
    return this;
  }

  public JobQuery jobDefinitionId(String jobDefinitionId) {
    ensureNotNull("Provided job definition id", jobDefinitionId);
    this.jobDefinitionId = jobDefinitionId;
    return this;
  }

  public JobQueryImpl processInstanceId(String processInstanceId) {
    ensureNotNull("Provided process instance id", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public JobQuery processInstanceIds(Set<String> processInstanceIds) {
    ensureNotEmpty("Set of process instance ids", processInstanceIds);
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  public JobQueryImpl executionId(String executionId) {
    ensureNotNull("Provided execution id", executionId);
    this.executionId = executionId;
    return this;
  }

  public JobQuery processDefinitionId(String processDefinitionId) {
    ensureNotNull("Provided process definition id", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public JobQuery processDefinitionKey(String processDefinitionKey) {
    ensureNotNull("Provided process instance key", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public JobQuery activityId(String activityId){
    ensureNotNull("Provided activity id", activityId);
    this.activityId = activityId;
    return this;
  }

  public JobQuery withRetriesLeft() {
    retriesLeft = true;
    return this;
  }

  public JobQuery executable() {
    executable = true;
    return this;
  }

  public JobQuery timers() {
    if (onlyMessages) {
      throw new ProcessEngineException("Cannot combine onlyTimers() with onlyMessages() in the same query");
    }
    this.onlyTimers = true;
    return this;
  }

  public JobQuery messages() {
    if (onlyTimers) {
      throw new ProcessEngineException("Cannot combine onlyTimers() with onlyMessages() in the same query");
    }
    this.onlyMessages = true;
    return this;
  }

  public JobQuery duedateHigherThan(Date date) {
    ensureNotNull("Provided date", date);
    this.duedateHigherThan = date;
    return this;
  }

  public JobQuery duedateLowerThan(Date date) {
    ensureNotNull("Provided date", date);
    this.duedateLowerThan = date;
    return this;
  }

  public JobQuery duedateHigherThen(Date date) {
    return duedateHigherThan(date);
  }

  public JobQuery duedateHigherThenOrEquals(Date date) {
    ensureNotNull("Provided date", date);
    this.duedateHigherThanOrEqual = date;
    return this;
  }

  public JobQuery duedateLowerThen(Date date) {
    return duedateLowerThan(date);
  }

  public JobQuery duedateLowerThenOrEquals(Date date) {
    ensureNotNull("Provided date", date);
    this.duedateLowerThanOrEqual = date;
    return this;
  }

  @Override
  public JobQuery createdBefore(Date date) {
    ensureNotNull("Provided date", date);
    this.createdBefore = date;
    return this;
  }

  @Override
  public JobQuery createdAfter(Date date) {
    ensureNotNull("Provided date", date);
    this.createdAfter = date;
    return this;
  }

    public JobQuery priorityHigherThanOrEquals(long priority) {
    this.priorityHigherThanOrEqual = priority;
    return this;
  }

  public JobQuery priorityLowerThanOrEquals(long priority) {
    this.priorityLowerThanOrEqual = priority;
    return this;
  }

  public JobQuery withException() {
    this.withException = true;
    return this;
  }

  public JobQuery exceptionMessage(String exceptionMessage) {
    ensureNotNull("Provided exception message", exceptionMessage);
    this.exceptionMessage = exceptionMessage;
    return this;
  }

  public JobQuery failedActivityId(String activityId){
    ensureNotNull("Provided activity id", activityId);
    this.failedActivityId = activityId;
    return this;
  }

  public JobQuery noRetriesLeft() {
    noRetriesLeft = true;
    return this;
  }

  public JobQuery active() {
    suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  public JobQuery suspended() {
    suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions()
      || CompareUtil.areNotInAscendingOrder(priorityHigherThanOrEqual, priorityLowerThanOrEqual)
      || hasExcludingDueDateParameters()
      || CompareUtil.areNotInAscendingOrder(createdAfter, createdBefore);
  }

  private boolean hasExcludingDueDateParameters() {
    List<Date> dueDates = new ArrayList<>();
    if (duedateHigherThan != null && duedateHigherThanOrEqual != null) {
      dueDates.add(CompareUtil.min(duedateHigherThan, duedateHigherThanOrEqual));
      dueDates.add(CompareUtil.max(duedateHigherThan, duedateHigherThanOrEqual));
    } else if (duedateHigherThan != null) {
      dueDates.add(duedateHigherThan);
    } else if (duedateHigherThanOrEqual != null) {
      dueDates.add(duedateHigherThanOrEqual);
    }

    if (duedateLowerThan != null && duedateLowerThanOrEqual != null) {
      dueDates.add(CompareUtil.min(duedateLowerThan, duedateLowerThanOrEqual));
      dueDates.add(CompareUtil.max(duedateLowerThan, duedateLowerThanOrEqual));
    } else if (duedateLowerThan != null) {
      dueDates.add(duedateLowerThan);
    } else if (duedateLowerThanOrEqual != null) {
      dueDates.add(duedateLowerThanOrEqual);
    }

    return CompareUtil.areNotInAscendingOrder(dueDates);
  }

  public JobQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public JobQuery withoutTenantId() {
    isTenantIdSet = true;
    this.tenantIds = null;
    return this;
  }

  public JobQuery includeJobsWithoutTenantId() {
    this.includeJobsWithoutTenantId = true;
    return this;
  }

  //sorting //////////////////////////////////////////

  public JobQuery orderByJobDuedate() {
    return orderBy(JobQueryProperty.DUEDATE);
  }

  public JobQuery orderByExecutionId() {
    return orderBy(JobQueryProperty.EXECUTION_ID);
  }

  public JobQuery orderByJobId() {
    return orderBy(JobQueryProperty.JOB_ID);
  }

  public JobQuery orderByProcessInstanceId() {
    return orderBy(JobQueryProperty.PROCESS_INSTANCE_ID);
  }

  public JobQuery orderByProcessDefinitionId() {
    return orderBy(JobQueryProperty.PROCESS_DEFINITION_ID);
  }

  public JobQuery orderByProcessDefinitionKey() {
    return orderBy(JobQueryProperty.PROCESS_DEFINITION_KEY);
  }

  public JobQuery orderByJobRetries() {
    return orderBy(JobQueryProperty.RETRIES);
  }

  public JobQuery orderByJobPriority() {
    return orderBy(JobQueryProperty.PRIORITY);
  }

  public JobQuery orderByTenantId() {
    return orderBy(JobQueryProperty.TENANT_ID);
  }

  //results //////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getJobManager()
      .findJobCountByQueryCriteria(this);
  }

  @Override
  public List<Job> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getJobManager()
      .findJobsByQueryCriteria(this, page);
  }

  @Override
  public List<ImmutablePair<String, String>> executeDeploymentIdMappingsList(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getJobManager()
      .findDeploymentIdMappingsByQueryCriteria(this);
  }

  //getters //////////////////////////////////////////

  public Set<String> getIds() {
    return ids;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public Set<String> getProcessInstanceIds() {
    return processInstanceIds;
  }
  public String getExecutionId() {
    return executionId;
  }
  public boolean getRetriesLeft() {
    return retriesLeft;
  }
  public boolean getExecutable() {
    return executable;
  }
  public Date getNow() {
    return ClockUtil.getCurrentTime();
  }
  public boolean isWithException() {
    return withException;
  }
  public String getExceptionMessage() {
    return exceptionMessage;
  }

}
