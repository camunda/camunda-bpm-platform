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
package org.camunda.bpm.container.impl.jmx.deployment.jobexecutor;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;

import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;

/**
 * <p>Deployment operation step responsible for stopping all job acquisitions</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class StopJobExecutorStep extends MBeanDeploymentOperationStep {
  
  private final static Logger LOGGER = Logger.getLogger(StopJobExecutorStep.class.getName());

  public String getName() {
    return "Stop managed job acquisitions";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    
    Set<ObjectName> jobExecutorServiceNames = serviceContainer.getServiceNames(ServiceTypes.JOB_EXECUTOR);
       
    for (ObjectName serviceName : jobExecutorServiceNames) {
      try {
        serviceContainer.stopService(serviceName);
      } catch(Exception e) {
        LOGGER.log(Level.WARNING, "Exception while stopping job executor service: "+e.getMessage(), e);
      }
    }
    
  }

}
