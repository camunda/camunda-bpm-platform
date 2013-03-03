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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.deployment.util.ClassPathScanner;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;


/**
 * <p>Deployment operation step responsible for deploying a process archive</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class DeployProcessArchiveStep extends MBeanDeploymentOperationStep {
  
  private final static Logger LOGGER = Logger.getLogger(DeployProcessArchiveStep.class.getName());      
  
  protected final ProcessArchiveXml processArchive;
  protected URL metaFileUrl;
  protected Deployment deployment;
  protected ProcessApplicationRegistration registration;
  
  public DeployProcessArchiveStep(ProcessArchiveXml parsedProcessArchive, URL url) {
    processArchive = parsedProcessArchive;
    this.metaFileUrl = url;
  }

  public String getName() {
    return "Deployment of process archive '"+processArchive.getName();
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    final AbstractProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);
    final ClassLoader processApplicationClassloader = processApplication.getProcessApplicationClassloader();            
    
    ProcessEngine processEngine = getProcessEngine(serviceContainer);
    
    if(processEngine == null) {
      String processEngineName = processArchive.getProcessEngineName();
      throw new ActivitiException("Cannot deploy process archive '" + processArchive.getName() + "' to process engine '" + processEngineName
          + "' no such process engine exists.");
    }

    // start building deployment map    
    Map<String, byte[]> deploymentMap = new HashMap<String, byte[]>();
    
    // add all processes listed in the processes.xml
    List<String> listedProcessResources = processArchive.getProcessResourceNames();
    for (String processResource : listedProcessResources) {
      InputStream resourceAsStream = null;
      try {
        resourceAsStream = processApplicationClassloader.getResourceAsStream(processResource);
        byte[] bytes = IoUtil.readInputStream(resourceAsStream, processResource);
        deploymentMap.put(processResource, bytes);
      } finally {
        IoUtil.closeSilently(resourceAsStream);
      }
    }
    
    // scan for additional process definitions if not turned off
    if(PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS, true)) {
      ClassPathScanner scanner = new ClassPathScanner();
      String paResourceRoot = processArchive.getProperties().get(ProcessArchiveXml.PROP_RESOURCE_ROOT_PATH);
      deploymentMap.putAll(scanner.findResources(processApplicationClassloader, paResourceRoot, metaFileUrl));
    }
    
    logDeploymentSummary(deploymentMap);
    
    // perform process engine deployment
    RepositoryService repositoryService = processEngine.getRepositoryService();
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
    
    // set the name for the deployment
    deploymentBuilder.name(processArchive.getName());
    // enable duplicate filtering
    deploymentBuilder.enableDuplicateFiltering();
    
    // add all resources obtaines through the processes.xml and through scanning
    for (Entry<String, byte[]> deploymentResource : deploymentMap.entrySet()) {
      deploymentBuilder.addInputStream(deploymentResource.getKey(), new ByteArrayInputStream(deploymentResource.getValue()));    
    }
    
    // allow the process application to add additional resources to the deployment
    processApplication.createDeployment(processArchive.getName(), deploymentBuilder);
    
    // perform the process engine deployment
    deployment = deploymentBuilder.deploy();
    
    // register the deployment 
    // TODO: think about turning the registration into a separate step.
    registration = processEngine.getManagementService().registerProcessApplication(deployment.getId(), processApplication.getReference());
    
    // add attachment
    Map<String, ProcessApplicationRegistration> processArchiveDeploymentMap = operationContext.getAttachment(Attachments.PROCESS_ARCHIVE_DEPLOYMENT_MAP);
    if(processArchiveDeploymentMap == null) {
      processArchiveDeploymentMap = new HashMap<String, ProcessApplicationRegistration>();
      operationContext.addAttachment(Attachments.PROCESS_ARCHIVE_DEPLOYMENT_MAP, processArchiveDeploymentMap);
    }    
    processArchiveDeploymentMap.put(processArchive.getName(), registration);    
  }

  protected void logDeploymentSummary(Map<String, byte[]> deploymentMap) {
    StringBuilder builder = new StringBuilder();
    builder.append("Deployment summary for process archive '"+processArchive.getName()+"': \n");
    builder.append("\n");
    for (String resourceName : deploymentMap.keySet()) {
      builder.append("        "+resourceName);
      builder.append("\n");
    }
    builder.append("\n");
    
    LOGGER.log(Level.INFO, builder.toString());
  }

  public void cancelOperationStep(MBeanDeploymentOperation operationContext) {   
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();    

    // if a registration was performed, remove it.
    if(registration != null) {
      registration.unregister();
    }
    
    // delete deployment if we were able to create one AND if isDeleteUponUndeploy is set.
    if(deployment != null && PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_DELETE_UPON_UNDEPLOY, false)) {      
      ProcessEngine processEngine = getProcessEngine(serviceContainer);      
      if(processEngine != null) {
        processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
      }
    }
    
  }

  protected ProcessEngine getProcessEngine(final MBeanServiceContainer serviceContainer) {
    String processEngineName = processArchive.getProcessEngineName();
    ProcessEngine processEngine = serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, processEngineName);
    return processEngine;
  }

}
