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
package org.camunda.bpm.engine.impl.application;

import org.camunda.bpm.application.spi.ProcessApplicationReference;
import org.camunda.bpm.engine.application.ProcessApplicationRegistration;

/**
 * @author Daniel Meyer
 *
 */
public class DefaultProcessApplicationRegistration implements ProcessApplicationRegistration {

  protected ProcessApplicationManager processApplicationManager;
  protected ProcessApplicationReference processApplicationReference;
  protected String deploymentId;

  /**
   * @param processApplicationManager
   */
  public DefaultProcessApplicationRegistration(ProcessApplicationManager processApplicationManager, ProcessApplicationReference processApplicationReference, String deploymentId) {
    this.processApplicationManager = processApplicationManager;
    this.processApplicationReference = processApplicationReference;
    this.deploymentId = deploymentId;
  }

  // called by the pa
  public void unregister() {
    processApplicationManager.removeProcessApplication(deploymentId);
  }
  
  public ProcessApplicationReference getProcessApplicationReference() {
    return processApplicationReference;
  }

}
