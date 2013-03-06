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

import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.engine.ProcessEngine;

/**
 * <p>Deployment operation step that stops all process engines.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class StopProcessEnginesStep extends MBeanDeploymentOperationStep {
  
  public final static Logger LOGGER = Logger.getLogger(StopProcessEnginesStep.class.getName());

  public String getName() {
    return "Stopping process engines";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    List<ProcessEngine> processEngines = serviceContainer.getServiceValuesByType(ServiceTypes.PROCESS_ENGINE);
    
    for (ProcessEngine processEngine : processEngines) {
      stopProcessEngine(processEngine);      
    }
    
  }

  /**
   * Stops  a process engine, failures are logged but no exceptions are thrown. 
   * 
   * @param processEngine
   */
  protected void stopProcessEngine(ProcessEngine processEngine) {
    
    try {
      
      // closing the process eninge makes sure it unregristers with the service container.
      processEngine.close();
      
    } catch(Throwable t) {
      LOGGER.log(Level.WARNING, "Exception while stopping process engine ", t);      
    }
    
  }

}
