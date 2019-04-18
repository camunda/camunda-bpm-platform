/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jboss.deployment.processor;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.container.impl.jboss.config.ManagedProcessEngineMetadata;
import org.camunda.bpm.container.impl.jboss.deployment.marker.ProcessApplicationAttachments;
import org.camunda.bpm.container.impl.jboss.service.MscManagedProcessEngineController;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.container.impl.jboss.util.ProcessesXmlWrapper;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.engine.ProcessEngine;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;


/**
 * <p>Deployment Unit Processor that creates process engine services for each 
 * process engine configured in a <code>processes.xml</code> file</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ProcessEngineStartProcessor implements DeploymentUnitProcessor {
  
  // this can happen at the beginning of the phase
  public static final int PRIORITY = 0x0000; 

  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
    
    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
    
    if(!ProcessApplicationAttachments.isProcessApplication(deploymentUnit)) {
      return;
    }
    
    List<ProcessesXmlWrapper> processesXmls = ProcessApplicationAttachments.getProcessesXmls(deploymentUnit);
    for (ProcessesXmlWrapper wrapper : processesXmls) {            
      for (ProcessEngineXml processEngineXml : wrapper.getProcessesXml().getProcessEngines()) {
        startProcessEngine(processEngineXml, phaseContext);      
      }
    }
    
  }

  protected void startProcessEngine(ProcessEngineXml processEngineXml, DeploymentPhaseContext phaseContext) {
    
    final ServiceTarget serviceTarget = phaseContext.getServiceTarget();
    
    // transform configuration
    ManagedProcessEngineMetadata configuration = transformConfiguration(processEngineXml);
    
    // validate the configuration
    configuration.validate();
    
    // create service instance
    MscManagedProcessEngineController service = new MscManagedProcessEngineController(configuration);
    
    // get the service name for the process engine
    ServiceName serviceName = ServiceNames.forManagedProcessEngine(processEngineXml.getName());
    
    // get service builder
    ServiceBuilder<ProcessEngine> serviceBuilder = serviceTarget.addService(serviceName, service);
    
    // make this service depend on the current phase -> makes sure it is removed with the phase service at undeployment
    serviceBuilder.addDependency(phaseContext.getPhaseServiceName());
    
    // add Service dependencies
    MscManagedProcessEngineController.initializeServiceBuilder(configuration, service, serviceBuilder, processEngineXml.getJobAcquisitionName());

    // install the service
    serviceBuilder.install();
    
  }

  /** transforms the configuration as provided via the {@link ProcessEngineXml} 
   * into a {@link ManagedProcessEngineMetadata} */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected ManagedProcessEngineMetadata transformConfiguration(ProcessEngineXml processEngineXml) {
    return new ManagedProcessEngineMetadata(
        processEngineXml.getName().equals("default"),
        processEngineXml.getName(),
        processEngineXml.getDatasource(),
        processEngineXml.getProperties().get("history"),
        processEngineXml.getConfigurationClass(),
        (Map) processEngineXml.getProperties(),
        processEngineXml.getPlugins());
  }

  public void undeploy(DeploymentUnit deploymentUnit) {
        
  }

}
