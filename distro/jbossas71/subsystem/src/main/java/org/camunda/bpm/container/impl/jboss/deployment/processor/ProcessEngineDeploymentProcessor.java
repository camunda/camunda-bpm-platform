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
package org.camunda.bpm.container.impl.jboss.deployment.processor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jboss.deployment.marker.ProcessApplicationAttachments;
import org.camunda.bpm.container.impl.jboss.deployment.scanner.VfsProcessApplicationResourceScanner;
import org.camunda.bpm.container.impl.jboss.service.MscManagedProcessApplication;
import org.camunda.bpm.container.impl.jboss.service.ProcessApplicationDeploymentService;
import org.camunda.bpm.container.impl.jboss.service.ProcessApplicationStartService;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.container.impl.jboss.util.ProcessesXmlWrapper;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.ee.component.ComponentView;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.vfs.VirtualFile;


/**
 * <p>This processor installs the process application into the container.</p> 
 *  
 * <p>First, we initialize the deployments for all process archives declared by the process application. 
 * It then registers a {@link MscProcessApplicationDeploymentService} for each process archive to be deployed.
 * Finally it registers the {@link MscManagedProcessApplication} service which depends on all the deployment services 
 * to have completed deployment</p> 
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessEngineDeploymentProcessor implements DeploymentUnitProcessor {
  
  public static final int PRIORITY = 0x0000; // this can happen at the beginning of the phase

  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
    
    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
    
    if(!ProcessApplicationAttachments.isProcessApplication(deploymentUnit)) {
      return;
    }

    // the service name of the process application component's view
    ServiceName paComponentViewServiceName = getProcessApplicationViewServiceName(deploymentUnit);
          
    List<ServiceName> deploymentServiceNames = new ArrayList<ServiceName>();

    // deploy all process archives
    List<ProcessesXmlWrapper> processesXmlWrappers = ProcessApplicationAttachments.getProcessesXmls(deploymentUnit);
    for (ProcessesXmlWrapper processesXmlWrapper : processesXmlWrappers) {   
      
      ProcessesXml processesXml = processesXmlWrapper.getProcessesXml();
      for (ProcessArchiveXml processArchive : processesXml.getProcessArchives()) {
  
        ServiceName processEngineServiceName = getProcessEngineServiceName(processArchive);
        Map<String, byte[]> deploymentResources = getDeploymentResources(processArchive, deploymentUnit, processesXmlWrapper.getProcessesXmlFile());
          
        // add the deployment service for each process archive we deploy.
        ServiceName deploymentServiceName = ServiceNames.forProcessApplicationDeploymentService(deploymentUnit.getName(), processArchive.getName());        
        ProcessApplicationDeploymentService deploymentService = new ProcessApplicationDeploymentService(deploymentResources, processArchive);
        phaseContext.getServiceTarget().addService(deploymentServiceName, deploymentService)
          .addDependency(phaseContext.getPhaseServiceName())
          .addDependency(processEngineServiceName, ProcessEngine.class, deploymentService.getProcessEngineInjector())
          .addDependency(paComponentViewServiceName, ComponentView.class, deploymentService.getProcessApplicationInjector())
          .setInitialMode(Mode.ACTIVE)
          .install();
        
        deploymentServiceNames.add(deploymentServiceName);
        
      }
    }
    
    // register the managed process application start service    
    ProcessApplicationStartService paStartService = new ProcessApplicationStartService(deploymentServiceNames);
    ServiceName paStartServiceName = ServiceNames.forProcessApplicationStartService(deploymentUnit.getName());
    phaseContext.getServiceTarget().addService(paStartServiceName, paStartService)
      .addDependency(phaseContext.getPhaseServiceName())
      .addDependency(paComponentViewServiceName, ComponentView.class, paStartService.getProcessApplicationInjector())
      .addDependencies(deploymentServiceNames)
      .setInitialMode(Mode.ACTIVE)
      .install();
        
  }

  public void undeploy(DeploymentUnit deploymentUnit) {
    
  }

  protected ServiceName getProcessApplicationViewServiceName(DeploymentUnit deploymentUnit) {
    return getProcessApplicationComponent(deploymentUnit).getViews().iterator().next().getServiceName();  
  }
    
  protected ComponentDescription getProcessApplicationComponent(DeploymentUnit deploymentUnit) {
    ComponentDescription paComponentDescription = ProcessApplicationAttachments.getProcessApplicationComponent(deploymentUnit);
    return paComponentDescription;
  }

  @SuppressWarnings("unchecked")
  protected ProcessEngine getProcessEngineForArchive(ServiceName serviceName, ServiceRegistry serviceRegistry) {
    ServiceController<ProcessEngine> processEngineServiceController = (ServiceController<ProcessEngine>) serviceRegistry.getRequiredService(serviceName);
    return processEngineServiceController.getValue();
  }

  protected ServiceName getProcessEngineServiceName(ProcessArchiveXml processArchive) {
    ServiceName serviceName = null;
    if(processArchive.getProcessEngineName() == null || processArchive.getProcessEngineName().length() == 0) {
      serviceName = ServiceNames.forDefaultProcessEngine();
    } else {
      serviceName = ServiceNames.forManagedProcessEngine(processArchive.getProcessEngineName());
    }
    return serviceName;
  }

  protected Map<String, byte[]> getDeploymentResources(ProcessArchiveXml processArchive, DeploymentUnit deploymentUnit, VirtualFile processesXmlFile) {

    final Module module = deploymentUnit.getAttachment(Attachments.MODULE);
    
    Map<String, byte[]> resources = new HashMap<String, byte[]>();

    // first, add all resources listed in the processe.xml
    List<String> process = processArchive.getProcessResourceNames();
    ModuleClassLoader classLoader = module.getClassLoader();
    
    for (String resource : process) {      
      InputStream inputStream = null;
      try {
        inputStream = classLoader.getResourceAsStream(resource);
        resources.put(resource, IoUtil.readInputStream(inputStream, resource));
      } finally {
        IoUtil.closeSilently(inputStream);
      }
    }
    
    // scan for process definitions
    if(PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS, process.isEmpty())) {
      final VfsProcessApplicationResourceScanner scanner = new VfsProcessApplicationResourceScanner();
      
      String resourceRootPath = processArchive.getProperties().get(ProcessArchiveXml.PROP_RESOURCE_ROOT_PATH);
      resources.putAll(scanner.findResources(classLoader, resourceRootPath, processesXmlFile));
      
    }
    
    return resources;  
  }

}
