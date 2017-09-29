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

package org.camunda.bpm.engine.impl.incident;

import org.camunda.bpm.engine.runtime.Incident;

/**
 * The context of an {@link Incident}.
 */
public class IncidentContext {

  protected String processDefinitionId;
  protected String activityId;
  protected String executionId;
  protected String configuration;
  protected String tenantId;
  protected String jobDefinitionId;

  public IncidentContext() {}

  public IncidentContext(Incident incident) {
    this.processDefinitionId = incident.getProcessDefinitionId();
    this.activityId = incident.getActivityId();
    this.executionId = incident.getExecutionId();
    this.configuration = incident.getConfiguration();
    this.tenantId = incident.getTenantId();
    this.jobDefinitionId = incident.getJobDefinitionId();
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

}
