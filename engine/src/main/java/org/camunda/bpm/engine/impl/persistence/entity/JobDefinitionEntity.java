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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.management.JobDefinition;

/**
 *
 * @author Daniel Meyer
 *
 */
public class JobDefinitionEntity implements JobDefinition, HasDbRevision, HasDbReferences, DbEntity, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected String processDefinitionId;
  protected String processDefinitionKey;

  /* Note: this is the id of the activity which is the cause that a Job is created.
   * If the Job corresponds to an event scope, it may or may not correspond to the
   * activity which defines the event scope.
   *
   * Example:
   * user task with attached timer event:
   * - timer event scope = user task
   * - activity which causes the job to be created = timer event.
   * => Job definition activityId will be activityId of the timer event, not the activityId of the user task.
   */
  protected String activityId;

  /** timer, message, ... */
  protected String jobType;
  protected String jobConfiguration;

  // job definition is active by default
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();

  protected Long jobPriority;

  protected String tenantId;

  public JobDefinitionEntity() {
  }

  public JobDefinitionEntity(JobDeclaration<?, ?> jobDeclaration) {
    this.activityId = jobDeclaration.getActivityId();
    this.jobConfiguration = jobDeclaration.getJobConfiguration();
    this.jobType = jobDeclaration.getJobHandlerType();
  }

  public Object getPersistentState() {
    HashMap<String, Object> state = new HashMap<String, Object>();
    state.put("processDefinitionId", processDefinitionId);
    state.put("processDefinitionKey", processDefinitionKey);
    state.put("activityId", activityId);
    state.put("jobType", jobType);
    state.put("jobConfiguration", jobConfiguration);
    state.put("suspensionState", suspensionState);
    state.put("jobPriority", jobPriority);
    state.put("tenantId", tenantId);
    return state;
  }

  // getters / setters /////////////////////////////////

  public int getRevisionNext() {
    return revision + 1;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public boolean isSuspended() {
    return SuspensionState.SUSPENDED.getStateCode() == suspensionState;
  }

  @Override
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @Override
  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  @Override
  public String getJobType() {
    return jobType;
  }

  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  @Override
  public String getJobConfiguration() {
    return jobConfiguration;
  }

  public void setJobConfiguration(String jobConfiguration) {
    this.jobConfiguration = jobConfiguration;
  }

  @Override
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public int getSuspensionState() {
    return suspensionState;
  }

  public void setSuspensionState(int state) {
    this.suspensionState = state;
  }

  public Long getOverridingJobPriority() {
    return jobPriority;
  }

  public void setJobPriority(Long jobPriority) {
    this.jobPriority = jobPriority;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public Set<String> getReferencedEntityIds() {
    Set<String> referencedEntityIds = new HashSet<String>();
    return referencedEntityIds;
  }

  @Override
  public Map<String, Class> getReferencedEntitiesIdAndClass() {
    Map<String, Class> referenceIdAndClass = new HashMap<String, Class>();
    return referenceIdAndClass;
  }
}
