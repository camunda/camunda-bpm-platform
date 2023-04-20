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

import static org.jboss.as.server.deployment.Attachments.MODULE;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.deployment.scanning.VfsProcessApplicationScanner;
import org.camunda.bpm.container.impl.jboss.deployment.marker.ProcessApplicationAttachments;
import org.camunda.bpm.container.impl.jboss.service.MscManagedProcessApplication;
import org.camunda.bpm.container.impl.jboss.service.ProcessApplicationDeploymentService;
import org.camunda.bpm.container.impl.jboss.service.ProcessApplicationStartService;
import org.camunda.bpm.container.impl.jboss.service.ProcessApplicationStopService;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.container.impl.jboss.util.JBossCompatibilityExtension;
import org.camunda.bpm.container.impl.jboss.util.ProcessesXmlWrapper;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugins;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.ee.component.ComponentView;
import org.jboss.as.ee.component.ViewDescription;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.vfs.VirtualFile;


/**
 * <p>This processor installs the process application into the container.</p>
 *
 * <p>First, we initialize the deployments for all process archives declared by the process application.
 * It then registers a {@link ProcessApplicationDeploymentService} for each process archive to be deployed.
 * Finally it registers the {@link MscManagedProcessApplication} service which depends on all the deployment services
 * to have completed deployment</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationDeploymentProcessor implements DeploymentUnitProcessor {

  public static final int PRIORITY = 0x0001; // this can happen early in the phase

  @Override
  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

    if(!ProcessApplicationAttachments.isProcessApplication(deploymentUnit)) {
      return;
    }

    final ComponentDescription paComponent = getProcessApplicationComponent(deploymentUnit);
    final ServiceName paViewServiceName = getProcessApplicationViewServiceName(paComponent);

    Module module = deploymentUnit.getAttachment(Attachments.MODULE);
    final String moduleName = module.getIdentifier().toString();
    final ServiceName paStartServiceName = ServiceNames.forProcessApplicationStartService(moduleName);
    final ServiceName paStopServiceName = ServiceNames.forProcessApplicationStopService(moduleName);
    final ServiceName noViewStartService = ServiceNames.forNoViewProcessApplicationStartService(moduleName);

    List<ServiceName> deploymentServiceNames = new ArrayList<ServiceName>();

    ProcessApplicationStopService paStopService = new ProcessApplicationStopService();
    ServiceBuilder<ProcessApplicationStopService> stopServiceBuilder = phaseContext.getServiceTarget().addService(paStopServiceName, paStopService)
      .addDependency(phaseContext.getPhaseServiceName())
      .addDependency(ServiceNames.forBpmPlatformPlugins(), BpmPlatformPlugins.class, paStopService.getPlatformPluginsInjector())
      .setInitialMode(Mode.ACTIVE);

    if(paViewServiceName != null) {
      stopServiceBuilder.addDependency(paViewServiceName, ComponentView.class, paStopService.getPaComponentViewInjector());
    } else {
      stopServiceBuilder.addDependency(noViewStartService, ProcessApplicationInterface.class, paStopService.getNoViewProcessApplication());
    }

    stopServiceBuilder.install();

    // deploy all process archives
    List<ProcessesXmlWrapper> processesXmlWrappers = ProcessApplicationAttachments.getProcessesXmls(deploymentUnit);
    for (ProcessesXmlWrapper processesXmlWrapper : processesXmlWrappers) {

      ProcessesXml processesXml = processesXmlWrapper.getProcessesXml();
      for (ProcessArchiveXml processArchive : processesXml.getProcessArchives()) {

        ServiceName processEngineServiceName = getProcessEngineServiceName(processArchive);
        Map<String, byte[]> deploymentResources = getDeploymentResources(processArchive, deploymentUnit, processesXmlWrapper.getProcessesXmlFile());

        // add the deployment service for each process archive we deploy.
        ProcessApplicationDeploymentService deploymentService = new ProcessApplicationDeploymentService(deploymentResources, processArchive, module);
        String processArachiveName = processArchive.getName();
        if(processArachiveName == null) {
          // use random name for deployment service if name is null (we cannot ask the process application yet since the component might not be up.
          processArachiveName = UUID.randomUUID().toString();
        }
        ServiceName deploymentServiceName = ServiceNames.forProcessApplicationDeploymentService(deploymentUnit.getName(), processArachiveName);
        ServiceBuilder<ProcessApplicationDeploymentService> serviceBuilder = phaseContext.getServiceTarget().addService(deploymentServiceName, deploymentService)
          .addDependency(phaseContext.getPhaseServiceName())
          .addDependency(paStopServiceName)
          .addDependency(processEngineServiceName, ProcessEngine.class, deploymentService.getProcessEngineInjector())
          .setInitialMode(Mode.ACTIVE);

        if(paViewServiceName != null) {
          // add a dependency on the component start service to make sure we are started after the pa-component (Singleton EJB) has started
          serviceBuilder.addDependency(paComponent.getStartServiceName());
          serviceBuilder.addDependency(paViewServiceName, ComponentView.class, deploymentService.getPaComponentViewInjector());
        } else {
          serviceBuilder.addDependency(noViewStartService, ProcessApplicationInterface.class, deploymentService.getNoViewProcessApplication());
        }

        JBossCompatibilityExtension.addServerExecutorDependency(serviceBuilder, deploymentService.getExecutorInjector(), false);

        serviceBuilder.install();

        deploymentServiceNames.add(deploymentServiceName);

      }
    }

    AnnotationInstance postDeploy = ProcessApplicationAttachments.getPostDeployDescription(deploymentUnit);
    AnnotationInstance preUndeploy = ProcessApplicationAttachments.getPreUndeployDescription(deploymentUnit);

    // register the managed process application start service
    ProcessApplicationStartService paStartService = new ProcessApplicationStartService(deploymentServiceNames, postDeploy, preUndeploy, module);
    ServiceBuilder<ProcessApplicationStartService> serviceBuilder = phaseContext.getServiceTarget().addService(paStartServiceName, paStartService)
      .addDependency(phaseContext.getPhaseServiceName())
      .addDependency(ServiceNames.forBpmPlatformPlugins(), BpmPlatformPlugins.class, paStartService.getPlatformPluginsInjector())
      .addDependencies(deploymentServiceNames)
      .setInitialMode(Mode.ACTIVE);

    if (phaseContext.getServiceRegistry().getService(ServiceNames.forDefaultProcessEngine()) != null) {
      serviceBuilder.addDependency(ServiceNames.forDefaultProcessEngine(), ProcessEngine.class, paStartService.getDefaultProcessEngineInjector());
    }
    if(paViewServiceName != null) {
      serviceBuilder.addDependency(paViewServiceName, ComponentView.class, paStartService.getPaComponentViewInjector());
    } else {
      serviceBuilder.addDependency(noViewStartService, ProcessApplicationInterface.class, paStartService.getNoViewProcessApplication());
    }

    serviceBuilder.install();
  }

  @Override
  public void undeploy(DeploymentUnit deploymentUnit) {

  }

  protected ServiceName getProcessApplicationViewServiceName(ComponentDescription paComponent) {
    Set<ViewDescription> views = paComponent.getViews();
    if(views == null || views.isEmpty()) {
      return null;
    } else {
      ViewDescription next = views.iterator().next();
      return next.getServiceName();
    }
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

    final Module module = deploymentUnit.getAttachment(MODULE);

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

      //always use VFS scanner on JBoss
      final VfsProcessApplicationScanner scanner = new VfsProcessApplicationScanner();

      String resourceRootPath = processArchive.getProperties().get(ProcessArchiveXml.PROP_RESOURCE_ROOT_PATH);
      String[] additionalResourceSuffixes = StringUtil.split(processArchive.getProperties().get(ProcessArchiveXml.PROP_ADDITIONAL_RESOURCE_SUFFIXES), ProcessArchiveXml.PROP_ADDITIONAL_RESOURCE_SUFFIXES_SEPARATOR);
      URL processesXmlUrl = vfsFileAsUrl(processesXmlFile);
      resources.putAll(scanner.findResources(classLoader, resourceRootPath, processesXmlUrl, additionalResourceSuffixes));
    }

    return resources;
  }

  protected URL vfsFileAsUrl(VirtualFile processesXmlFile)  {
    try {
      return processesXmlFile.toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

}
