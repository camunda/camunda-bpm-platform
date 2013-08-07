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
package org.camunda.bpm.container.impl.jmx.deployment;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessApplication;

/**
 * <p>Deployment operation step that is responsible for stopping all process applications</p>
 * 
 * @author Daniel Meyer
 *
 */
public class StopProcessApplicationsStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Stopping process applications";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    List<JmxManagedProcessApplication> processApplicationsReferences = serviceContainer.getServiceValuesByType(ServiceTypes.PROCESS_APPLICATION);
    
    for (JmxManagedProcessApplication processApplication : processApplicationsReferences) {
      stopProcessApplication(processApplication.getProcessApplicationReference());      
    }

  }

  /**
   * <p> Stops a process application. Exceptions are logged but not re-thrown).
   * 
   * @param processApplicationReference
   */
  protected void stopProcessApplication(ProcessApplicationReference processApplicationReference) {
    
    try {      
      // unless the user has overridden the stop behavior, 
      // this causes the process application to remove its services 
      // (triggers nested undeployment operation)
      ProcessApplicationInterface processApplication = processApplicationReference.getProcessApplication();
      processApplication.undeploy();
      
    } catch(Throwable t) {
      LOGGER.log(Level.WARNING, "Exception while stopping ProcessApplication ", t);
      
    }
            
  }

}
