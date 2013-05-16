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
package org.camunda.bpm.cockpit.plugin.base.persistence.entity;

public class ProcessDefinitionDto {
  
  private String id;
  private String name;
  private String key;
  private String deploymentId;
  private long suspensionState;
  private long failedJobs;
  private long version;
  
  public ProcessDefinitionDto() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public long getSuspensionState() {
    return suspensionState;
  }

  public void setSuspensionState(long suspensionState) {
    this.suspensionState = suspensionState;
  }

  public long getFailedJobs() {
    return failedJobs;
  }

  public void setFailedJobs(long failedJobs) {
    this.failedJobs = failedJobs;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
  
}
