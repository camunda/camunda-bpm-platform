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

import java.io.Serializable;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

/**
 * <p>A job declaration is associated with an activity in the process definition graph.
 * It provides data about Jobs which are to be created when executing this activity.
 * It also acts as a factory for new Job Instances.</p>
 *
 * @author Daniel Meyer
 *
 */
public abstract class JobDeclaration<T extends JobEntity> implements Serializable {

  private static final long serialVersionUID = 1L;

  /** the id of the associated persistent jobDefinitionId */
  protected String jobDefinitionId;

  protected String jobHandlerType;
  protected String jobHandlerConfiguration;
  protected String jobConfiguration;

  protected boolean exclusive = JobEntity.DEFAULT_EXCLUSIVE;
  protected int retries = JobEntity.DEFAULT_RETRIES;

  protected String activityId;

  public JobDeclaration(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }

  // Job instance factory //////////////////////////////////////////

  /**
   *
   * @param execution can be null in case of a timer start event.
   * @return the created Job instances
   */
  public T createJobInstance(ExecutionEntity execution) {

    T job = newJobInstance(execution);

    // set job definition id
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
      }

    }

    job.setJobHandlerType(jobHandlerType);
    job.setJobHandlerConfiguration(jobHandlerConfiguration);
    job.setExclusive(exclusive);
    job.setRetries(retries);

    return job;
  }

  protected abstract T newJobInstance(ExecutionEntity execution);

  // Getter / Setters //////////////////////////////////////////

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  public String getJobHandlerType() {
    return jobHandlerType;
  }

  public String getJobHandlerConfiguration() {
    return jobHandlerConfiguration;
  }

  public void setJobHandlerConfiguration(String jobHandlerConfiguration) {
    this.jobHandlerConfiguration = jobHandlerConfiguration;
  }

  public boolean isExclusive() {
    return exclusive;
  }

  public void setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
  }

  public int getRetries() {
    return retries;
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }

  public void setJobHandlerType(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getJobConfiguration() {
    return jobConfiguration;
  }

  public void setJobConfiguration(String jobConfiguration) {
    this.jobConfiguration = jobConfiguration;
  }

}
