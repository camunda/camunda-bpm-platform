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
package org.camunda.bpm.engine.application;

import org.activiti.engine.repository.Deployment;


/**
 * 
 * @author Daniel Meyer
 *
 */
public interface ProcessApplicationRegistration {
  
  /**
   * The process application must call this method in order to unregister with the process engine.
   */
  public void unregister();
  
  /**
   * @return the id of the {@link Deployment} for which the registration was created
   */
  public String getDeploymentId(); 
  
  
}
