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
package com.camunda.fox.platform.subsystem.impl.deployment.processor;

import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;

import com.camunda.fox.platform.subsystem.impl.deployment.marker.ProcessApplicationAttachments;
import com.camunda.fox.platform.subsystem.impl.service.ManagedProcessEngineConfiguration;
import com.camunda.fox.platform.subsystem.impl.service.ManagedProcessEngineController;

/**
 * <p>Deployment Unit Processor that creates process engine services for each 
 * process engine configured in the <code>processes.xml</code> file</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ProcessEngineStartProcessor implements DeploymentUnitProcessor {

  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
    
    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
    
    if(!ProcessApplicationAttachments.isProcessApplication(deploymentUnit)) {
      return;
    }
    
    ProcessesXml processesXml = ProcessApplicationAttachments.getProcessesXml(deploymentUnit).getProcessesXml();
    
    for (ProcessEngineXml processEngineXml : processesXml.getProcessEngines()) {
      startProcessEngine(processEngineXml, phaseContext);      
    }
    
  }

  protected void startProcessEngine(ProcessEngineXml processEngineXml, DeploymentPhaseContext phaseContext) {
    
    final ServiceTarget serviceTarget = phaseContext.getServiceTarget();
    
    // transform configuration
    ManagedProcessEngineConfiguration configuration = transformConfiguration(processEngineXml);
    
    // create service instance
    ManagedProcessEngineController service = new ManagedProcessEngineController(configuration);
    
    // get the service name for the process engine
    ServiceName serviceName = ManagedProcessEngineController.createServiceName(processEngineXml.getName());
    
    // get service builder
    ServiceBuilder<ProcessEngine> serviceBuilder = serviceTarget.addService(serviceName, service);
    
    // add Service dependencies
    ManagedProcessEngineController.initializeServiceBuilder(configuration, service, serviceBuilder);
    
    // install the service
    serviceBuilder.install();
    
  }

  /** transforms the configuration as provided via the {@link ProcessEngineXml} 
   * into a {@link ManagedProcessEngineConfiguration} */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected ManagedProcessEngineConfiguration transformConfiguration(ProcessEngineXml processEngineXml) {
    
    boolean isDefault = processEngineXml.getName().equals("default");
    String engineName = processEngineXml.getName();
    String datasourceJndiName = processEngineXml.getDatasource();
    String historyLevel = processEngineXml.getProperties().get("history");
    
    return new ManagedProcessEngineConfiguration(isDefault, engineName, datasourceJndiName, historyLevel, (Map) processEngineXml.getProperties());
        
  }

  public void undeploy(DeploymentUnit deploymentUnit) {
    
    // remove all process engines listed in the processes.xml
        
    if(!ProcessApplicationAttachments.isProcessApplication(deploymentUnit)) {
      return;
    }
    
    ProcessesXml processesXml = ProcessApplicationAttachments.getProcessesXml(deploymentUnit).getProcessesXml();
    
    for (ProcessEngineXml processEngineXml : processesXml.getProcessEngines()) {
      stopProcessEngine(processEngineXml, deploymentUnit);      
    }
    
  }

  protected void stopProcessEngine(ProcessEngineXml processEngineXml, DeploymentUnit deploymentUnit) {

    final ServiceRegistry serviceRegistry = deploymentUnit.getServiceRegistry();
    
    // get the service name for the process engine
    ServiceName serviceName = ManagedProcessEngineController.createServiceName(processEngineXml.getName());
    
    // find the service
    ServiceController<?> service = serviceRegistry.getService(serviceName);
    
    if(service != null) {
      // remoce the service
      service.setMode(Mode.REMOVE);
    }
  }

}
