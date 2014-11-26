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
package org.camunda.bpm.container.impl.deployment.util;

import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;

/**
 * @author Daniel Meyer
 *
 */
public class DeployedProcessArchive {

  protected String primaryDeploymentId;

  /** if old deployments were resumed */
  protected Set<String> allDeploymentIds;

  protected String processEngineName;

  public DeployedProcessArchive(ProcessApplicationDeployment deployment) {
    primaryDeploymentId = deployment.getId();
    ProcessApplicationRegistration registration = deployment.getProcessApplicationRegistration();
    allDeploymentIds = registration.getDeploymentIds();
    processEngineName = registration.getProcessEngineName();
  }

  public String getPrimaryDeploymentId() {
    return primaryDeploymentId;
  }

  public void setPrimaryDeploymentId(String primaryDeploymentId) {
    this.primaryDeploymentId = primaryDeploymentId;
  }

  public Set<String> getAllDeploymentIds() {
    return allDeploymentIds;
  }

  public void setAllDeploymentIds(Set<String> allDeploymentIds) {
    this.allDeploymentIds = allDeploymentIds;
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }

}
