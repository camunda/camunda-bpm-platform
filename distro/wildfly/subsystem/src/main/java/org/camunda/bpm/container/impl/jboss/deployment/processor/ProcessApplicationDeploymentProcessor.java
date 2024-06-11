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
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
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
    final String moduleName = module.getName();
    final ServiceName paStartServiceName = ServiceNames.forProcessApplicationStartService(moduleName);
    final ServiceName paStopServiceName = ServiceNames.forProcessApplicationStopService(moduleName);
    final ServiceName noViewStartService = ServiceNames.forNoViewProcessApplicationStartService(moduleName);

    List<ServiceName> deploymentServiceNames = new ArrayList<>();

    ServiceBuilder<?> stopServiceBuilder = phaseContext.getServiceTarget().addService(paStopServiceName);
    Consumer<ProcessApplicationStopService> paStopProvider = stopServiceBuilder.provides(paStopServiceName);
    stopServiceBuilder.requires(phaseContext.getPhaseServiceName());
    Supplier<BpmPlatformPlugins> platformPluginsSupplier = stopServiceBuilder.requires(ServiceNames.forBpmPlatformPlugins());

    Supplier<ComponentView> paComponentViewSupplierStopService = null;
    Supplier<ProcessApplicationInterface> noViewApplicationSupplierStopService = null;
    if (paViewServiceName != null) {
      paComponentViewSupplierStopService = stopServiceBuilder.requires(paViewServiceName);
    } else {
      noViewApplicationSupplierStopService = stopServiceBuilder.requires(noViewStartService);
    }

    ProcessApplicationStopService paStopService = new ProcessApplicationStopService(
        paComponentViewSupplierStopService,
        noViewApplicationSupplierStopService,
        platformPluginsSupplier,
        paStopProvider);
    stopServiceBuilder.setInitialMode(Mode.ACTIVE);
    stopServiceBuilder.setInstance(paStopService);
    stopServiceBuilder.install();

    // deploy all process archives
    List<ProcessesXmlWrapper> processesXmlWrappers = ProcessApplicationAttachments.getProcessesXmls(deploymentUnit);
    for (ProcessesXmlWrapper processesXmlWrapper : processesXmlWrappers) {

      ProcessesXml processesXml = processesXmlWrapper.getProcessesXml();
      for (ProcessArchiveXml processArchive : processesXml.getProcessArchives()) {

        ServiceName processEngineServiceName = getProcessEngineServiceName(processArchive);
        Map<String, byte[]> deploymentResources = getDeploymentResources(processArchive, deploymentUnit, processesXmlWrapper.getProcessesXmlFile());

        // add the deployment service for each process archive we deploy.
        String processArchiveName = processArchive.getName();
        if(processArchiveName == null) {
          // use random name for deployment service if name is null (we cannot ask the process application yet since the component might not be up.
          processArchiveName = UUID.randomUUID().toString();
        }
        ServiceName deploymentServiceName = ServiceNames.forProcessApplicationDeploymentService(deploymentUnit.getName(), processArchiveName);
        ServiceBuilder<?> deploymentServiceBuilder = phaseContext.getServiceTarget().addService(deploymentServiceName);

        Consumer<ProcessApplicationDeploymentService> paDeploymentProvider = deploymentServiceBuilder.provides(deploymentServiceName);
        deploymentServiceBuilder.requires(phaseContext.getPhaseServiceName());
        deploymentServiceBuilder.requires(paStopServiceName);
        Supplier<ProcessEngine> processEngineServiceSupplier = deploymentServiceBuilder.requires(processEngineServiceName);

        deploymentServiceBuilder.setInitialMode(Mode.ACTIVE);

        Supplier<ComponentView> paComponentViewSupplier = null;
        Supplier<ProcessApplicationInterface> noViewProcessApplicationSupplier = null;
        if(paViewServiceName != null) {
          // add a dependency on the component start service to make sure we are started after the pa-component (Singleton EJB) has started
          deploymentServiceBuilder.requires(paComponent.getStartServiceName());
          paComponentViewSupplier = deploymentServiceBuilder.requires(paViewServiceName);
        } else {
          noViewProcessApplicationSupplier = deploymentServiceBuilder.requires(noViewStartService);
        }

        Supplier<ExecutorService> executorSupplier = JBossCompatibilityExtension.addServerExecutorDependency(deploymentServiceBuilder);

        ProcessApplicationDeploymentService deploymentService = new ProcessApplicationDeploymentService(
            deploymentResources,
            processArchive,
            module,
            executorSupplier,
            processEngineServiceSupplier,
            noViewProcessApplicationSupplier,
            paComponentViewSupplier,
            paDeploymentProvider);
        deploymentServiceBuilder.setInstance(deploymentService);

        deploymentServiceBuilder.install();

        deploymentServiceNames.add(deploymentServiceName);

      }
    }

    AnnotationInstance postDeploy = ProcessApplicationAttachments.getPostDeployDescription(deploymentUnit);
    AnnotationInstance preUndeploy = ProcessApplicationAttachments.getPreUndeployDescription(deploymentUnit);

    // register the managed process application start service
    ServiceBuilder<?> processApplicationStartServiceBuilder = phaseContext.getServiceTarget().addService(paStartServiceName);
    Consumer<ProcessApplicationStartService> paStartProvider = processApplicationStartServiceBuilder.provides(paStartServiceName);

    processApplicationStartServiceBuilder.requires(phaseContext.getPhaseServiceName());
    Supplier<BpmPlatformPlugins> platformPluginsSupplierStartService = processApplicationStartServiceBuilder.requires(ServiceNames.forBpmPlatformPlugins());
    deploymentServiceNames.forEach(processApplicationStartServiceBuilder::requires);

    processApplicationStartServiceBuilder.setInitialMode(Mode.ACTIVE);

    Supplier<ProcessEngine> defaultProcessEngineSupplier = null;
    if (phaseContext.getServiceRegistry().getService(ServiceNames.forDefaultProcessEngine()) != null) {
      defaultProcessEngineSupplier = processApplicationStartServiceBuilder.requires(ServiceNames.forDefaultProcessEngine());
    }
    Supplier<ComponentView> paComponentViewSupplier = null;
    Supplier<ProcessApplicationInterface> noViewProcessApplication = null;
    if (paViewServiceName != null) {
      paComponentViewSupplier = processApplicationStartServiceBuilder.requires(paViewServiceName);
    } else {
      noViewProcessApplication = processApplicationStartServiceBuilder.requires(noViewStartService);
    }

    ProcessApplicationStartService paStartService = new ProcessApplicationStartService(
        deploymentServiceNames,
        postDeploy,
        preUndeploy,
        module,
        paComponentViewSupplier,
        noViewProcessApplication,
        defaultProcessEngineSupplier,
        platformPluginsSupplierStartService,
        paStartProvider);

    processApplicationStartServiceBuilder.setInstance(paStartService);
    processApplicationStartServiceBuilder.install();
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
    return ProcessApplicationAttachments.getProcessApplicationComponent(deploymentUnit);
  }

  protected ServiceName getProcessEngineServiceName(ProcessArchiveXml processArchive) {
    ServiceName serviceName = null;
    if(processArchive.getProcessEngineName() == null || processArchive.getProcessEngineName().isEmpty()) {
      serviceName = ServiceNames.forDefaultProcessEngine();
    } else {
      serviceName = ServiceNames.forManagedProcessEngine(processArchive.getProcessEngineName());
    }
    return serviceName;
  }

  protected Map<String, byte[]> getDeploymentResources(ProcessArchiveXml processArchive, DeploymentUnit deploymentUnit, VirtualFile processesXmlFile) {

    final Module module = deploymentUnit.getAttachment(MODULE);

    Map<String, byte[]> resources = new HashMap<>();

    // first, add all resources listed in the processes.xml
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