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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;

/**
 * @author Daniel Meyer
 * 
 */
public class ProcessApplicationManager {
  
  private Logger LOGGER = Logger.getLogger(ProcessApplicationManager.class.getName());

  protected Map<String, DefaultProcessApplicationRegistration> registrationsByDeploymentId = new HashMap<String, DefaultProcessApplicationRegistration>();

  public ProcessApplicationReference getProcessApplicationForDeployment(String deploymentId) {
    DefaultProcessApplicationRegistration registration = registrationsByDeploymentId.get(deploymentId);
    if (registration != null) {
      return registration.getReference();
    } else {
      return null;
    }
  }

  /**
   * Register a deployment for a given {@link ProcessApplicationReference}.
   * 
   * @param deploymentId
   * @param reference
   * @return
   */
  public ProcessApplicationRegistration registerProcessApplicationForDeployment(String deploymentId, ProcessApplicationReference reference) {

    DefaultProcessApplicationRegistration registration = registrationsByDeploymentId.get(deploymentId);
    
    if(registration == null) {
      String processEngineName = Context.getProcessEngineConfiguration().getProcessEngineName();
      registration = new DefaultProcessApplicationRegistration(reference, deploymentId, processEngineName);
      registrationsByDeploymentId.put(deploymentId, registration);
      return registration;
      
    } else {
      throw new ActivitiException("Cannot register process application for deploymentId '" + deploymentId
          + "' there already is a registration for the same deployment.");

    }
  }

  /**
   * @return the IDs of all deployments that are currently associated with a
   *         process application
   */
  public String[] getActiveDeploymentIds() {
    return registrationsByDeploymentId.keySet().toArray(new String[0]);
  }

  public boolean unregisterProcessApplicationForDeployment(String deploymentId, boolean removeProcessesFromCache) {
    
    if(removeProcessesFromCache) {
      try {
        
        Context.getProcessEngineConfiguration()
          .getDeploymentCache()
          .removeDeployment(deploymentId);
        
      } catch(Exception e) {
        LOGGER.log(Level.WARNING, "unregistering process application for deployment but could not remove process definitions from deployment cache. ", e);
      }
    }
    
    // always remove the reference.
    return registrationsByDeploymentId.remove(deploymentId) != null;
  }

}
