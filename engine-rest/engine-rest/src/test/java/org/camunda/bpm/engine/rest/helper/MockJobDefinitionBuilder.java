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
package org.camunda.bpm.engine.rest.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.management.JobDefinition;

public class MockJobDefinitionBuilder {

  protected String id;
  protected String activityId;
  protected String jobConfiguration;
  protected String jobType;
  protected Long jobPriority;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected boolean suspended;
  protected String tenantId;

  public MockJobDefinitionBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockJobDefinitionBuilder activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public MockJobDefinitionBuilder jobConfiguration(String jobConfiguration) {
    this.jobConfiguration = jobConfiguration;
    return this;
  }

  public MockJobDefinitionBuilder jobType(String jobType) {
    this.jobType = jobType;
    return this;
  }

  public MockJobDefinitionBuilder jobPriority(Long priority) {
    this.jobPriority = priority;
    return this;
  }

  public MockJobDefinitionBuilder processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public MockJobDefinitionBuilder processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public MockJobDefinitionBuilder suspended(boolean suspended) {
    this.suspended = suspended;
    return this;
  }

  public MockJobDefinitionBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public JobDefinition build() {
    JobDefinition mockJobDefinition = mock(JobDefinition.class);
    when(mockJobDefinition.getId()).thenReturn(id);
    when(mockJobDefinition.getActivityId()).thenReturn(activityId);
    when(mockJobDefinition.getJobConfiguration()).thenReturn(jobConfiguration);
    when(mockJobDefinition.getOverridingJobPriority()).thenReturn(jobPriority);
    when(mockJobDefinition.getJobType()).thenReturn(jobType);
    when(mockJobDefinition.getProcessDefinitionId()).thenReturn(processDefinitionId);
    when(mockJobDefinition.getProcessDefinitionKey()).thenReturn(processDefinitionKey);
    when(mockJobDefinition.isSuspended()).thenReturn(suspended);
    when(mockJobDefinition.getTenantId()).thenReturn(tenantId);
    return mockJobDefinition;
  }

}
