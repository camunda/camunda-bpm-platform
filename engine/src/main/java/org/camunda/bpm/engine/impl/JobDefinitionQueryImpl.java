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

import java.io.Serializable;
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;

/**
 * @author roman.smirnov
 */
public class JobDefinitionQueryImpl extends AbstractQuery<JobDefinitionQuery, JobDefinition> implements JobDefinitionQuery, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String[] activityIds;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String jobType;
  protected String jobConfiguration;
  protected SuspensionState suspensionState;

  public JobDefinitionQueryImpl() {
  }

  public JobDefinitionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public JobDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public JobDefinitionQuery jobDefinitionId(String jobDefinitionId) {
    assertParamNotNull("Job definition id", jobDefinitionId);
    this.id = jobDefinitionId;
    return this;
  }

  public JobDefinitionQuery activityIdIn(String... activityIds) {
    assertParamNotNull("Activity ids", activityIds);
    this.activityIds = activityIds;
    return this;
  }

  public JobDefinitionQuery processDefinitionId(String processDefinitionId) {
    assertParamNotNull("Process definition id", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public JobDefinitionQuery processDefinitionKey(String processDefinitionKey) {
    assertParamNotNull("Process definition key", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public JobDefinitionQuery jobType(String jobType) {
    assertParamNotNull("Job type", jobType);
    this.jobType = jobType;
    return this;
  }

  public JobDefinitionQuery jobConfiguration(String jobConfiguration) {
    assertParamNotNull("Job configuration", jobConfiguration);
    this.jobConfiguration = jobConfiguration;
    return this;
  }

  public JobDefinitionQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  public JobDefinitionQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  // order by ///////////////////////////////////////////

  public JobDefinitionQuery orderByJobDefinitionId() {
    return orderBy(JobDefinitionQueryProperty.JOB_DEFINITION_ID);
  }

  public JobDefinitionQuery orderByActivityId() {
    return orderBy(JobDefinitionQueryProperty.ACTIVITY_ID);
  }

  public JobDefinitionQuery orderByProcessDefinitionId() {
    return orderBy(JobDefinitionQueryProperty.PROCESS_DEFINITION_ID);
  }

  public JobDefinitionQuery orderByProcessDefinitionKey() {
    return orderBy(JobDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
  }

  public JobDefinitionQuery orderByJobType() {
    return orderBy(JobDefinitionQueryProperty.JOB_TYPE);
  }

  public JobDefinitionQuery orderByJobConfiguration() {
    return orderBy(JobDefinitionQueryProperty.JOB_CONFIGURATION);
  }

  // results ////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getJobDefinitionManager()
      .findJobDefinitionCountByQueryCriteria(this);
  }

  public List<JobDefinition> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getJobDefinitionManager()
      .findJobDefnitionByQueryCriteria(this, page);
  }

  // getters /////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public String[] getActivityIds() {
    return activityIds;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getJobType() {
    return jobType;
  }

  public String getJobConfiguration() {
    return jobConfiguration;
  }

  public SuspensionState getSuspensionState() {
    return suspensionState;
  }

}
