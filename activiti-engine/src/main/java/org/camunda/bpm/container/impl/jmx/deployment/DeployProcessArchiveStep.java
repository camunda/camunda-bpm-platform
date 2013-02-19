package org.camunda.bpm.container.impl.jmx.deployment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.deployment.parser.PropertyHelper;
import org.camunda.bpm.application.impl.deployment.spi.ProcessArchiveXml;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.deployment.util.ClassPathScanner;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.engine.application.ProcessApplicationRegistration;


/**
 * <p>Deployment operation step responsible for deploying a process archive</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class DeployProcessArchiveStep extends MBeanDeploymentOperationStep {
  
  protected final ProcessArchiveXml processArchive;
  protected URL metaFileUrl;
  protected Deployment deployment;
  
  public DeployProcessArchiveStep(ProcessArchiveXml parsedProcessArchive, URL url) {
    processArchive = parsedProcessArchive;
    this.metaFileUrl = url;
  }

  public String getName() {
    return "Deployment of process archive '"+processArchive.getName();
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    final ProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);
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
    ProcessApplicationRegistration registration = processEngine.getManagementService().activateDeploymentForApplication(deployment.getId(), processApplication.getReference());
    
    // add attachment
    Map<String, ProcessApplicationRegistration> processArchiveDeploymentMap = operationContext.getAttachment(Attachments.PROCESS_ARCHIVE_DEPLOYMENT_MAP);
    if(processArchiveDeploymentMap == null) {
      processArchiveDeploymentMap = new HashMap<String, ProcessApplicationRegistration>();
      operationContext.addAttachment(Attachments.PROCESS_ARCHIVE_DEPLOYMENT_MAP, processArchiveDeploymentMap);
    }    
    processArchiveDeploymentMap.put(processArchive.getName(), registration);    
  }

  public void cancelOperationStep(MBeanDeploymentOperation operationContext) {   
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();    
    
    // delete deployment if we were able to create one.
    if(deployment != null) {      
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
