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
package org.camunda.bpm.container.impl;

import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.container.impl.deployment.Attachments;
import org.camunda.bpm.container.impl.deployment.DeployProcessArchivesStep;
import org.camunda.bpm.container.impl.deployment.NotifyPostProcessApplicationUndeployedStep;
import org.camunda.bpm.container.impl.deployment.ParseProcessesXmlStep;
import org.camunda.bpm.container.impl.deployment.PostDeployInvocationStep;
import org.camunda.bpm.container.impl.deployment.PreUndeployInvocationStep;
import org.camunda.bpm.container.impl.deployment.ProcessesXmlStartProcessEnginesStep;
import org.camunda.bpm.container.impl.deployment.ProcessesXmlStopProcessEnginesStep;
import org.camunda.bpm.container.impl.deployment.StartProcessApplicationServiceStep;
import org.camunda.bpm.container.impl.deployment.StopProcessApplicationServiceStep;
import org.camunda.bpm.container.impl.deployment.UndeployProcessArchivesStep;
import org.camunda.bpm.container.impl.jmx.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessApplication;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngine;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.ServiceTypes;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import javax.management.MBeanServer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * <p>This is the default {@link RuntimeContainerDelegate} implementation that delegates
 * to the local {@link MBeanServer} infrastructure. The MBeanServer is available
 * as per the Java Virtual Machine and allows the process engine to expose
 * Management Resources.</p>
 *
 * @author Daniel Meyer
 */
public class RuntimeContainerDelegateImpl implements RuntimeContainerDelegate, ProcessEngineService, ProcessApplicationService {

  protected final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  protected MBeanServiceContainer serviceContainer = new MBeanServiceContainer();

  public final static String SERVICE_NAME_EXECUTOR = "executor-service";
  public final static String SERVICE_NAME_PLATFORM_PLUGINS = "bpm-platform-plugins";

  // runtime container delegate implementation ///////////////////////////////////////////////

  @Override
  public void registerProcessEngine(ProcessEngine processEngine) {
    ensureNotNull("Cannot register process engine in Jmx Runtime Container", "process engine", processEngine);

    String processEngineName = processEngine.getName();

    // build and start the service.
    JmxManagedProcessEngine managedProcessEngine = new JmxManagedProcessEngine(processEngine);
    serviceContainer.startService(ServiceTypes.PROCESS_ENGINE, processEngineName, managedProcessEngine);

  }

  @Override
  public void unregisterProcessEngine(ProcessEngine processEngine) {
    ensureNotNull("Cannot unregister process engine in Jmx Runtime Container", "process engine", processEngine);

    serviceContainer.stopService(ServiceTypes.PROCESS_ENGINE, processEngine.getName());

  }

  @Override
  public void deployProcessApplication(AbstractProcessApplication processApplication) {
    ensureNotNull("Process application", processApplication);

    final String operationName = "Deployment of Process Application " + processApplication.getName();

    serviceContainer.createDeploymentOperation(operationName)
      .addAttachment(Attachments.PROCESS_APPLICATION, processApplication)
      .addSteps(getDeploymentSteps())
      .execute();

    LOG.paDeployed(processApplication.getName());
  }

  @Override
  public void undeployProcessApplication(AbstractProcessApplication processApplication) {
    ensureNotNull("Process application", processApplication);

    final String processAppName = processApplication.getName();

    // if the process application is not deployed, ignore the request.
    if (serviceContainer.getService(ServiceTypes.PROCESS_APPLICATION, processAppName) == null) {
      return;
    }

    final String operationName = "Undeployment of Process Application " + processAppName;

    // perform the undeployment
    serviceContainer.createUndeploymentOperation(operationName)
      .addAttachment(Attachments.PROCESS_APPLICATION, processApplication)
      .addSteps(getUndeploymentSteps())
      .execute();

    LOG.paUndeployed(processApplication.getName());
  }


  protected List<DeploymentOperationStep> getDeploymentSteps() {
    return Arrays.asList(
      new ParseProcessesXmlStep(),
      new ProcessesXmlStartProcessEnginesStep(),
      new DeployProcessArchivesStep(),
      new StartProcessApplicationServiceStep(),
      new PostDeployInvocationStep());
  }

  protected List<DeploymentOperationStep> getUndeploymentSteps() {
    return Arrays.asList(
      new PreUndeployInvocationStep(),
      new UndeployProcessArchivesStep(),
      new ProcessesXmlStopProcessEnginesStep(),
      new StopProcessApplicationServiceStep(),
      new NotifyPostProcessApplicationUndeployedStep()
    );
  }


  @Override
  public ProcessEngineService getProcessEngineService() {
    return this;
  }


  @Override
  public ProcessApplicationService getProcessApplicationService() {
    return this;
  }

  @Override
  public ExecutorService getExecutorService() {
    return serviceContainer.getServiceValue(ServiceTypes.BPM_PLATFORM, SERVICE_NAME_EXECUTOR);
  }

  // ProcessEngineServiceDelegate //////////////////////////////////////////////

  @Override
  public ProcessEngine getDefaultProcessEngine() {
    return serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, "default");
  }

  @Override
  public ProcessEngine getProcessEngine(String name) {
    return serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, name);
  }

  @Override
  public List<ProcessEngine> getProcessEngines() {
    return serviceContainer.getServiceValuesByType(ServiceTypes.PROCESS_ENGINE);
  }

  @Override
  public Set<String> getProcessEngineNames() {
    Set<String> processEngineNames = new HashSet<String>();
    List<ProcessEngine> processEngines = getProcessEngines();
    for (ProcessEngine processEngine : processEngines) {
      processEngineNames.add(processEngine.getName());
    }
    return processEngineNames;
  }

  // process application service implementation /////////////////////////////////

  @Override
  public Set<String> getProcessApplicationNames() {
    List<JmxManagedProcessApplication> processApplications = serviceContainer.getServiceValuesByType(ServiceTypes.PROCESS_APPLICATION);
    Set<String> processApplicationNames = new HashSet<String>();
    for (JmxManagedProcessApplication jmxManagedProcessApplication : processApplications) {
      processApplicationNames.add(jmxManagedProcessApplication.getProcessApplicationName());
    }
    return processApplicationNames;
  }

  @Override
  public ProcessApplicationInfo getProcessApplicationInfo(String processApplicationName) {

    JmxManagedProcessApplication processApplicationService = serviceContainer.getServiceValue(ServiceTypes.PROCESS_APPLICATION, processApplicationName);

    if (processApplicationService == null) {
      return null;
    } else {
      return processApplicationService.getProcessApplicationInfo();
    }
  }

  @Override
  public ProcessApplicationReference getDeployedProcessApplication(String processApplicationName) {
    JmxManagedProcessApplication processApplicationService = serviceContainer.getServiceValue(ServiceTypes.PROCESS_APPLICATION, processApplicationName);

    if (processApplicationService == null) {
      return null;
    } else {
      return processApplicationService.getProcessApplicationReference();
    }
  }

  // Getter / Setter ////////////////////////////////////////////////////////////

  public PlatformServiceContainer getServiceContainer() {
    return serviceContainer;
  }

}
