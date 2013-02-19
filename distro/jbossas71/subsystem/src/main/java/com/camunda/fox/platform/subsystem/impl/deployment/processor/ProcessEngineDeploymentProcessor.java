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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.DeploymentBuilder;
import org.camunda.bpm.application.impl.deployment.metadata.PropertyHelper;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.deployment.parser.ProcessesXmlParser;
import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.ee.component.ComponentView;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;

import com.camunda.fox.platform.subsystem.impl.deployment.marker.ProcessApplicationAttachments;
import com.camunda.fox.platform.subsystem.impl.deployment.scanner.VfsProcessApplicationResourceScanner;
import com.camunda.fox.platform.subsystem.impl.service.ContainerProcessEngineController;
import com.camunda.fox.platform.subsystem.impl.service.ProcessApplicationRegistrationService;

/**
 * <p>This processor
 * <ul>
 *  <li>scans the process application for process resources (BPMN 2.0 files)</li>
 *  <li>constructs or resumes a process engine deployment</li>
 *  <li>registers the process application with the process engine</li>
 * </ul>
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessEngineDeploymentProcessor implements DeploymentUnitProcessor {
  
  private final static Logger log = Logger.getLogger(ProcessEngineDeploymentProcessor.class.getName());
  public static final int PRIORITY = 0x2051;

  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
    
    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
    
    if(!ProcessApplicationAttachments.isProcessApplication(deploymentUnit)) {
      return;
    }
    
    final Map<ProcessArchiveXml, String> deploymentMap = new HashMap<ProcessArchiveXml, String>();        

    // deploy all process archives
    for (ProcessArchiveXml processArchive : ProcessApplicationAttachments.getProcessesXml(deploymentUnit).getProcessArchives()) {

      ServiceName processEngineServiceName = getProcessEngineServiceName(processArchive);
      ProcessEngine processEngine = getProcessEngineForArchive(processEngineServiceName, phaseContext.getServiceRegistry());
      Map<String, byte[]> deploymentResources = getDeploymentResources(processArchive, deploymentUnit);
      
      if(deploymentResources.isEmpty()) {
        
        log.log(
            Level.INFO,
            "Process archive does not contain any deployment resources.");
        
      } else {
        
        String engineDeploymentId = performEngineDeployment(processEngine, deploymentResources, processArchive, deploymentUnit);
        
        // add registration service
        ProcessApplicationRegistrationService registrationService = new ProcessApplicationRegistrationService(engineDeploymentId);
        phaseContext.getServiceTarget().addService(registrationService.getServiceName(), registrationService)
        .addDependency(processEngineServiceName, ContainerProcessEngineController.class, registrationService.getProcessEngineInjector())
        .addDependency(getProcessApplicationViewServiceName(deploymentUnit), ComponentView.class, registrationService.getProcessApplicationInjector())
        .setInitialMode(Mode.ACTIVE)
        .install();
        
        deploymentMap.put(processArchive, engineDeploymentId);
        
      }
    }
    
    // attach the deployment map
    ProcessApplicationAttachments.attachDeploymentMap(deploymentUnit, deploymentMap);    
    
  }

  public void undeploy(DeploymentUnit deploymentUnit) {
    
    if(!ProcessApplicationAttachments.isProcessApplication(deploymentUnit)) {
      return;
    }
    
    Map<ProcessArchiveXml, String> deploymentMap = ProcessApplicationAttachments.getDeploymentMap(deploymentUnit);
    if(deploymentMap != null) {
      for (Entry<ProcessArchiveXml, String> deployment : deploymentMap.entrySet()) {
        
        ProcessArchiveXml processArchive = deployment.getKey();
        
        // delete the deployment
        if(PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_DELETE_UPON_UNDEPLOY, false)) {
          
          ProcessEngine processEngine = getProcessEngineForArchive(getProcessEngineServiceName(processArchive), deploymentUnit.getServiceRegistry());
          try {
            processEngine.getRepositoryService().deleteDeployment(deployment.getValue(), true);
          } catch (Exception e) {
            log.log(Level.WARNING, "Exception while deleting process engine deployment", e);
          }
          
        }
        
      }
    }
  }

  protected ServiceName getProcessApplicationViewServiceName(DeploymentUnit deploymentUnit) {
    return getProcessApplicationComponent(deploymentUnit).getViews().iterator().next().getServiceName();  
  }
    
  protected ComponentDescription getProcessApplicationComponent(DeploymentUnit deploymentUnit) {
    ComponentDescription paComponentDescription = ProcessApplicationAttachments.getProcessApplicationComponent(deploymentUnit);
    return paComponentDescription;
  }

  protected String performEngineDeployment(ProcessEngine processEngine, Map<String, byte[]> deploymentResources, ProcessArchiveXml processArchive, DeploymentUnit deploymentUnit) {
    
    StringBuilder builder = new StringBuilder();
    builder.append("Deployment summary for process archive '"+processArchive.getName()+"': \n");
    builder.append("\n");
    for (String resourceName : deploymentResources.keySet()) {
      builder.append("        "+resourceName);          
    }
    builder.append("\n");
    
    log.log(Level.INFO, builder.toString());
    
    final RepositoryService repositoryService = processEngine.getRepositoryService();
    
    DeploymentBuilder deployment = repositoryService.createDeployment();

    // enable duplicate filtering
    deployment.enableDuplicateFiltering();

    // set deployment name    
    deployment.name(processArchive.getName()); 
    
    // add deployment resources    
    for (Entry<String, byte[]> resource : deploymentResources.entrySet()) {
      deployment.addInputStream(resource.getKey(), new ByteArrayInputStream(resource.getValue()));
    }
    
    // perform deployment    
    String id = deployment.deploy().getId();
    log.log(Level.INFO, "Process engine deploymentId for process archive '"+processArchive.getName()+"' is '"+id+"'");
    
    return id;
  }

  @SuppressWarnings("unchecked")
  protected ProcessEngine getProcessEngineForArchive(ServiceName serviceName, ServiceRegistry serviceRegistry) {
    ServiceController<ContainerProcessEngineController> processEngineServiceController = (ServiceController<ContainerProcessEngineController>) serviceRegistry.getRequiredService(serviceName);
    return processEngineServiceController.getValue().getProcessEngine();
  }

  protected ServiceName getProcessEngineServiceName(ProcessArchiveXml processArchive) {
    ServiceName serviceName = null;
    if(processArchive.getProcessEngineName() == null || processArchive.getProcessEngineName().length() == 0) {
      serviceName = ContainerProcessEngineController.createServiceNameForDefaultEngine();
    } else {
      serviceName = ContainerProcessEngineController.createServiceName(processArchive.getProcessEngineName());
    }
    return serviceName;
  }

  protected Map<String, byte[]> getDeploymentResources(ProcessArchiveXml processArchive, DeploymentUnit deploymentUnit) {

    final Module module = deploymentUnit.getAttachment(Attachments.MODULE);
    
    Map<String, byte[]> resources = new HashMap<String, byte[]>();

    // first, add all resources listed in the processe.xml
    List<String> process = processArchive.getProcessResourceNames();
    for (String resource : process) {      
      InputStream inputStream = null;
      try {
        inputStream = module.getClassLoader().getResourceAsStream(resource);
        resources.put(resource, IoUtil.readInputStream(inputStream, resource));
      } finally {
        IoUtil.closeSilently(inputStream);
      }
    }
    
    // scan for process definitions
    if(PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS, process.isEmpty())) {
      final VfsProcessApplicationResourceScanner scanner = new VfsProcessApplicationResourceScanner();
      resources.putAll(scanner.findResources(processArchive, deploymentUnit));
    }
    
    return resources;  
  }

}
