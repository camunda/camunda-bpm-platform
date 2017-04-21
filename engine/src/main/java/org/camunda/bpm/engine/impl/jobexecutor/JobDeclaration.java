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
package org.camunda.bpm.engine.impl.jobexecutor;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Date;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * <p>A job declaration is associated with an activity in the process definition graph.
 * It provides data about jobs which are to be created when executing this activity.
 * It also acts as a factory for new Job Instances.</p>
 *
 * <p>Jobs are of a type T and are created in the context of type S (e.g. an execution or an event subscription).
 * An instance of the context class is handed in when a job is created.</p>
 *
 * @author Daniel Meyer
 *
 */
public abstract class JobDeclaration<S, T extends JobEntity> implements Serializable {

  private static final long serialVersionUID = 1L;

  /** the id of the associated persistent jobDefinitionId */
  protected String jobDefinitionId;

  protected String jobHandlerType;
  protected JobHandlerConfiguration jobHandlerConfiguration;
  protected String jobConfiguration;

  protected boolean exclusive = JobEntity.DEFAULT_EXCLUSIVE;

  protected ActivityImpl activity;

  protected ParameterValueProvider jobPriorityProvider;

  public JobDeclaration(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }

  // Job instance factory //////////////////////////////////////////

  /**
   *
   * @return the created Job instances
   */
  public T createJobInstance(S context) {

    T job = newJobInstance(context);

    // set job definition id
    String jobDefinitionId = resolveJobDefinitionId(context);
    job.setJobDefinitionId(jobDefinitionId);

    if(jobDefinitionId != null) {

      JobDefinitionEntity jobDefinition = Context.getCommandContext()
        .getJobDefinitionManager()
        .findById(jobDefinitionId);

      if(jobDefinition != null) {
        // if job definition is suspended while creating a job instance,
        // suspend the job instance right away:
        job.setSuspensionState(jobDefinition.getSuspensionState());
        job.setProcessDefinitionKey(jobDefinition.getProcessDefinitionKey());
        job.setProcessDefinitionId(jobDefinition.getProcessDefinitionId());
        job.setTenantId(jobDefinition.getTenantId());
      }

    }

    job.setJobHandlerConfiguration(resolveJobHandlerConfiguration(context));
    job.setJobHandlerType(resolveJobHandlerType(context));
    job.setExclusive(resolveExclusive(context));
    job.setRetries(resolveRetries(context));
    job.setDuedate(resolveDueDate(context));


    // contentExecution can be null in case of a timer start event or
    // and batch jobs unrelated to executions
    ExecutionEntity contextExecution = resolveExecution(context);

    if (Context.getProcessEngineConfiguration().isProducePrioritizedJobs()) {
      long priority = Context
          .getProcessEngineConfiguration()
          .getJobPriorityProvider()
          .determinePriority(contextExecution, this, jobDefinitionId);

      job.setPriority(priority);
    }

    if (contextExecution != null) {
      // in case of shared process definitions, the job definitions have no tenant id.
      // To distinguish jobs between tenants and enable the tenant check for the job executor,
      // use the tenant id from the execution.
      job.setTenantId(contextExecution.getTenantId());
    }

    postInitialize(context, job);

    return job;
  }

  /**
   * Re-initialize configuration part.
    */
  public T reconfigure(S context, T job) {
    return job;
  }

  /**
   * general callback to override any configuration after the defaults have been applied
   */
  protected void postInitialize(S context, T job) {
  }

  /**
   * Returns the execution in which context the job is created. The execution
   * is used to determine the job's priority based on a BPMN activity
   * the execution is currently executing. May be null.
   */
  protected abstract ExecutionEntity resolveExecution(S context);

  protected abstract T newJobInstance(S context);

  // Getter / Setters //////////////////////////////////////////

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  protected String resolveJobDefinitionId(S context) {
    return jobDefinitionId;
  }

  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  public String getJobHandlerType() {
    return jobHandlerType;
  }

  protected JobHandler resolveJobHandler() {
     JobHandler jobHandler = Context.getProcessEngineConfiguration().getJobHandlers().get(jobHandlerType);
     ensureNotNull("Cannot find job handler '" + jobHandlerType + "' from job '" + this + "'", "jobHandler", jobHandler);

     return jobHandler;
  }

  protected String resolveJobHandlerType(S context) {
    return jobHandlerType;
  }

  protected abstract JobHandlerConfiguration resolveJobHandlerConfiguration(S context);

  protected boolean resolveExclusive(S context) {
    return exclusive;
  }

  protected int resolveRetries(S context) {
    return Context.getProcessEngineConfiguration().getDefaultNumberOfRetries();
  }

  public Date resolveDueDate(S context) {
    ProcessEngineConfiguration processEngineConfiguration = Context.getProcessEngineConfiguration();
    if (processEngineConfiguration != null && processEngineConfiguration.isJobExecutorAcquireByDueDate()) {
      return ClockUtil.getCurrentTime();
    }
    else {
      return null;
    }
  }

  public boolean isExclusive() {
    return exclusive;
  }

  public void setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
  }

  public String getActivityId() {
    if (activity != null) {
      return activity.getId();
    }
    else {
      return null;
    }
  }

  public ActivityImpl getActivity() {
    return activity;
  }

  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
  }

  public ProcessDefinitionImpl getProcessDefinition() {
    if (activity != null) {
      return activity.getProcessDefinition();
    }
    else {
      return null;
    }
  }

  public String getJobConfiguration() {
    return jobConfiguration;
  }

  public void setJobConfiguration(String jobConfiguration) {
    this.jobConfiguration = jobConfiguration;
  }

  public ParameterValueProvider getJobPriorityProvider() {
    return jobPriorityProvider;
  }

  public void setJobPriorityProvider(ParameterValueProvider jobPriorityProvider) {
    this.jobPriorityProvider = jobPriorityProvider;
  }
}
