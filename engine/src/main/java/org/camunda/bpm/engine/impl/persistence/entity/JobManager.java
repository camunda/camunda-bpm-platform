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

import java.util.*;
import org.camunda.bpm.engine.impl.JobQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.jobexecutor.*;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class JobManager extends AbstractManager {

  public void send(MessageEntity message) {
    message.insert();
    hintJobExecutor(message);
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
      transactionListener = new ExclusiveJobAddedNotification(job.getId());
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
    return (JobEntity) getDbSqlSession().selectOne("selectJob", jobId);
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
    return getDbSqlSession().selectList("selectNextJobsToExecute", params, page);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectJobsByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findExclusiveJobsToExecute(String processInstanceId) {
    Map<String,Object> params = new HashMap<String, Object>();
    params.put("pid", processInstanceId);
    params.put("now",ClockUtil.getCurrentTime());
    return getDbSqlSession().selectList("selectExclusiveJobsToExecute", params);
  }


  @SuppressWarnings("unchecked")
  public List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, Page page) {
    final String query = "selectUnlockedTimersByDuedate";
    return getDbSqlSession().selectList(query, duedate, page);
  }

  @SuppressWarnings("unchecked")
  public List<TimerEntity> findTimersByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectTimersByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    final String query = "selectJobByQueryCriteria";
    return getDbSqlSession().selectList(query, jobQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByConfiguration(String jobHandlerType, String jobHandlerConfiguration) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("handlerType", jobHandlerType);
    params.put("handlerConfiguration", jobHandlerConfiguration);
    return getDbSqlSession().selectList("selectJobsByConfiguration", params);
  }

  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectJobCountByQueryCriteria", jobQuery);
  }

  public void updateJobSuspensionStateById(String jobId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("jobId", jobId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateJobSuspensionStateByJobDefinitionId(String jobDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("jobDefinitionId", jobDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateJobSuspensionStateByProcessInstanceId(String processInstanceId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateJobSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateStartTimerJobSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    parameters.put("handlerType", TimerStartEventJobHandler.TYPE);
    getDbSqlSession().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateJobSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateStartTimerJobSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("suspensionState", suspensionState.getStateCode());
    parameters.put("handlerType", TimerStartEventJobHandler.TYPE);
    getDbSqlSession().update(JobEntity.class, "updateJobSuspensionStateByParameters", parameters);
  }

  public void updateFailedJobRetriesByJobDefinitionId(String jobDefinitionId, int retries) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("jobDefinitionId", jobDefinitionId);
    parameters.put("retries", retries);
    getDbSqlSession().update(JobEntity.class, "updateFailedJobRetriesByParameters", parameters);
  }

}
