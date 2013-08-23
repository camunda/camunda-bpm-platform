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

import java.util.Date;

import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationDeploymentImpl implements ProcessApplicationDeployment {

  protected Deployment deployment;
  protected ProcessApplicationRegistration registration;

  public ProcessApplicationDeploymentImpl(Deployment deployment, ProcessApplicationRegistration registration) {
    this.deployment = deployment;
    this.registration = registration;
  }

  public String getId() {
    return deployment.getId();
  }

  public String getName() {
    return deployment.getName();
  }

  public Date getDeploymentTime() {
    return deployment.getDeploymentTime();
  }

  public ProcessApplicationRegistration getProcessApplicationRegistration() {
    return registration;
  }

}
