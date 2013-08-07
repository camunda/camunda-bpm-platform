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

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;

import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;

/**
 * <p>Deployment operation step that stops ALL process engines registered inside the container.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class StopProcessEnginesStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Stopping process engines";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    Set<ObjectName> serviceNames = serviceContainer.getServiceNames(ServiceTypes.PROCESS_ENGINE);
    
    for (ObjectName serviceName : serviceNames) {
      stopProcessEngine(serviceName, serviceContainer);      
    }
    
  }

  /**
   * Stops a process engine, failures are logged but no exceptions are thrown. 
   * 
   */
  private void stopProcessEngine(ObjectName serviceName, MBeanServiceContainer serviceContainer) {
    
    try {
      serviceContainer.stopService(serviceName);
      
    }catch(Exception e) {
      LOGGER.log(Level.FINE, "Could not stop managed process engine "+serviceName.toString(), e);
    }
    
  }

}
