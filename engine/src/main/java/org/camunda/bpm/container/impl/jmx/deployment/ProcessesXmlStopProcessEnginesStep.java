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

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessApplication;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>Deployment operation responsible for stopping all process engines started by the deployment.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ProcessesXmlStopProcessEnginesStep extends MBeanDeploymentOperationStep {
  
  public String getName() {
    return "Stopping process engines";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    final AbstractProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);    
    final JmxManagedProcessApplication deployedProcessApplication = serviceContainer.getService(ServiceTypes.PROCESS_APPLICATION, processApplication.getName());
    
    if(deployedProcessApplication == null) {
      throw new ProcessEngineException("Cannot find process application with name "+processApplication.getName()+".");
    }
    
    List<ProcessesXml> processesXmls = deployedProcessApplication.getProcessesXmls();
    for (ProcessesXml processesXml : processesXmls) {
      stopProcessEngines(processesXml.getProcessEngines(), operationContext);            
    }

  }

  protected void stopProcessEngines(List<ProcessEngineXml> processEngine, MBeanDeploymentOperation operationContext) {    
    for (ProcessEngineXml parsedProcessEngine : processEngine) {
      stopProcessEngine(parsedProcessEngine.getName(), operationContext);      
    }        
  }

  protected void stopProcessEngine(String processEngineName, MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    try {
      serviceContainer.stopService(ServiceTypes.PROCESS_ENGINE.getServiceName(processEngineName));
      
    } catch(Exception e) {      
      LOGGER.log(Level.WARNING, "Could not stop managed process engine: "+e.getMessage(), e);
      
    }
    
  }

}
