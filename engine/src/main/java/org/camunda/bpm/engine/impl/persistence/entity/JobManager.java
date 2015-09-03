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

package org.camunda.bpm.engine.impl.persistence.entity;

import static org.camunda.bpm.engine.impl.jobexecutor.TimerEventJobHandler.JOB_HANDLER_CONFIG_PROPERTY_DELIMITER;
import static org.camunda.bpm.engine.impl.jobexecutor.TimerEventJobHandler.JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_JOB_CREATED;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.JobQueryImpl;
import org.camunda.bpm.engine.impl.JobQueryProperty;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.jobexecutor.ExclusiveJobAddedNotification;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorContext;
import org.camunda.bpm.engine.impl.jobexecutor.MessageAddedNotification;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class JobManager extends AbstractManager {

  public static QueryOrderingProperty JOB_PRIORITY_ORDERING_PROPERTY = new QueryOrderingProperty(null, JobQueryProperty.PRIORITY);
  public static QueryOrderingProperty JOB_TYPE_ORDERING_PROPERTY = new QueryOrderingProperty(null, JobQueryProperty.TYPE);
  public static QueryOrderingProperty JOB_DUEDATE_ORDERING_PROPERTY = new QueryOrderingProperty(null, JobQueryProperty.DUEDATE);

  static {
    JOB_PRIORITY_ORDERING_PROPERTY.setDirection(Direction.DESCENDING);
    JOB_TYPE_ORDERING_PROPERTY.setDirection(Direction.DESCENDING);
    JOB_DUEDATE_ORDERING_PROPERTY.setDirection(Direction.ASCENDING);
  }

  public void insertJob(JobEntity job) {
    getDbEntityManager().insert(job);
    getHistoricJobLogManager().fireJobCreatedEvent(job);
  }

  public void deleteJob(JobEntity job) {
    deleteJob(job, true);
  }

  public void deleteJob(JobEntity job, boolean fireDeleteEvent) {
    getDbEntityManager().delete(job);

    if (fireDeleteEvent) {
      getHistoricJobLogManager().fireJobDeletedEvent(job);
    }

  }

  public void send(MessageEntity message) {
    message.insert();
    if (Context.getProcessEngineConfiguration().isHintJobExecutor()) {
      hintJobExecutor(message);
    }
  }

  public void schedule(TimerEntity timer) {
    Date duedate = timer.getDuedate();
    ensureNotNull("duedate", duedate);

    timer.insert();

    // Check if this timer fires before the next time the job executor will check for new timers to fire.
    // This is highly unlikely because normally waitTimeInMillis is 5000 (5 seconds)
    // and timers are usually set further in the future

    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    int waitTimeInMillis = jobExecutor.getWaitTimeInMillis();
    if (duedate.getTime() < (ClockUtil.getCurrentTime().getTime() + waitTimeInMillis)) {
      hintJobExecutor(timer);
    }
  }

  protected void hintJobExecutor(JobEntity job) {
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    JobExecutorContext jobExecutorContext = Context.getJobExecutorContext();
    TransactionListener transactionListener = null;
    if(!job.isSuspended()
            && job.isExclusive()
            && jobExecutorContext != null
            && jobExecutorContext.isExecutingExclusiveJob()) {
      // lock job & add to the queue of the current processor
      Date currentTime = ClockUtil.getCurrentTime();
      job.setLockExpirationTime(new Date(currentTime.getTime() + jobExecutor.getLockTimeInMillis()));
      job.setLockOwner(jobExecutor.getLockOwner());
      transactionListener = new ExclusiveJobAddedNotification(job.getId(), jobExecutorContext);
    } else {
      // notify job executor:
      transactionListener = new MessageAddedNotification(jobExecutor);
    }
    Context.getCommandContext()
    .getTransactionContext()
    .addTransactionListener(TransactionState.COMMITTED, transactionListener);
  }

  public void cancelTimers(ExecutionEntity execution) {
    List<TimerEntity> timers = Context
      .getCommandContext()
      .getJobManager()
      .findTimersByExecutionId(execution.getId());

    for (TimerEntity timer: timers) {
      timer.delete();
    }
  }

  public JobEntity findJobById(String jobId) {
    return (JobEntity) getDbEntityManager().selectOne("selectJob", jobId);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findNextJobsToExecute(Page page) {
    Map<String,Object> params = new HashMap<String, Object>();
    Date now = ClockUtil.getCurrentTime();
    params.put("now", now);
    params.put("deploymentAware", Context.getProcessEngineConfiguration().isJobExecutorDeploymentAware());
    if (Context.getProcessEngineConfiguration().isJobExecutorDeploymentAware()) {
      Set<String> registeredDeployments = Context.getProcessEngineConfiguration().getRegisteredDeployments();
      if (!registeredDeployments.isEmpty()) {
        params.put("deploymentIds", registeredDeployments);
      }
    }

    List<QueryOrderingProperty> orderingProperties = new ArrayList<QueryOrderingProperty>();
    if (Context.getProcessEngineConfiguration().isJobExecutorAcquireByPriority()) {
      orderingProperties.add(JOB_PRIORITY_ORDERING_PROPERTY);
    }
    if (Context.getProcessEngineConfiguration().isJobExecutorPreferTimerJobs()) {
      orderingProperties.add(JOB_TYPE_ORDERING_PROPERTY);
    }
    if (Context.getProcessEngineConfiguration().isJobExecutorAcquireByDueDate()) {
      orderingProperties.add(JOB_DUEDATE_ORDERING_PROPERTY);
    }

    params.put("orderingProperties", orderingProperties);
    // don't apply default sorting
    params.put("applyOrdering", !orderingProperties.isEmpty());

    return getDbEntityManager().selectList("selectNextJobsToExecute", params, page);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByExecutionId(String executionId) {
    return getDbEntityManager().selectList("selectJobsByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByProcessInstanceId(String processInstanceId) {
    return getDbEntityManager().selectList("selectJobsByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findExclusiveJobsToExecute(String processInstanceId) {
    Map<String,Object> params = new HashMap<String, Object>();
    params.put("pid", processInstanceId);
    params.put("now",ClockUtil.getCurrentTime());
    return getDbEntityManager().selectList("selectExclusiveJobsToExecute", params);
  }


  @SuppressWarnings("unchecked")
  public List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, Page page) {
    final String query = "selectUnlockedTimersByDuedate";
    return getDbEntityManager().selectList(query, duedate, page);
  }

  @SuppressWarnings("unchecked")
  public List<TimerEntity> findTimersByExecutionId(String executionId) {
    return getDbEntityManager().selectList("selectTimersByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    getAuthorizationManager().configureJobQuery(jobQuery);
    return getDbEntityManager().selectList("selectJobByQueryCriteria", jobQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByConfiguration(String jobHandlerType, String jobHandlerConfiguration) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("handlerType", jobHandlerType);
    params.put("handlerConfiguration", jobHandlerConfiguration);

    if (TimerCatchIntermediateEventJobHandler.TYPE.equals(jobHandlerType)
      || TimerExecuteNestedActivityJobHandler.TYPE.equals(jobHandlerType)
      || TimerStartEventJobHandler.TYPE.equals(jobHandlerType)
      || TimerStartEventSubprocessJobHandler.TYPE.equals(jobHandlerType)) {

      String queryValue = jobHandlerConfiguration + JOB_HANDLER_CONFIG_PROPERTY_DELIMITER + JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_JOB_CREATED;
      params.put("handlerConfigurationWithFollowUpJobCreatedProperty", queryValue);
    }

    return getDbEntityManager().selectList("selectJobsByConfiguration", params);
  }

  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    getAuthorizationManager().configureJobQuery(jobQuery);
    return (Long) getDbEntityManager().selectOne("selectJobCountByQueryCriteria", jobQuery);
  }

  public void updateJobSuspensionStateById(String jobId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("jobId", jobId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateJobSuspensionStateByJobDefinitionId(String jobDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("jobDefinitionId", jobDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateJobSuspensionStateByProcessInstanceId(String processInstanceId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateJobSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateStartTimerJobSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    parameters.put("handlerType", TimerStartEventJobHandler.TYPE);
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateJobSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateStartTimerJobSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("suspensionState", suspensionState.getStateCode());
    parameters.put("handlerType", TimerStartEventJobHandler.TYPE);
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateFailedJobRetriesByJobDefinitionId(String jobDefinitionId, int retries) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("jobDefinitionId", jobDefinitionId);
    parameters.put("retries", retries);
    getDbEntityManager().update(JobEntity.class, "updateFailedJobRetriesByParameters", parameters);
  }

  public void updateJobPriorityByDefinitionId(String jobDefinitionId, long priority) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("jobDefinitionId", jobDefinitionId);
    parameters.put("priority", priority);
    getDbEntityManager().update(JobEntity.class, "updateJobPriorityByDefinitionId", parameters);
  }

}
