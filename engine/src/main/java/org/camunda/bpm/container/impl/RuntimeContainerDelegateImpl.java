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
package org.camunda.bpm.container.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.container.impl.deployment.Attachments;
import org.camunda.bpm.container.impl.deployment.DeployProcessArchivesStep;
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
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.ServiceTypes;
import org.camunda.bpm.engine.ProcessEngine;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * <p>This is the default {@link RuntimeContainerDelegate} implementation that delegates
 * to the local {@link MBeanServer} infrastructure. The MBeanServer is available
 * as per the Java Virtual Machine and allows the process engine to expose
 * Management Resources.</p>
 *
 * @author Daniel Meyer
 *
 */
public class RuntimeContainerDelegateImpl implements RuntimeContainerDelegate, ProcessEngineService, ProcessApplicationService {

  private final Logger LOGGER = Logger.getLogger(RuntimeContainerDelegateImpl.class.getName());

  protected MBeanServiceContainer serviceContainer = new MBeanServiceContainer();

  public final static String SERVICE_NAME_EXECUTOR = "executor-service";

  // runtime container delegate implementation ///////////////////////////////////////////////

  public void registerProcessEngine(ProcessEngine processEngine) {
    ensureNotNull("Cannot register process engine in Jmx Runtime Container", "process engine", processEngine);

    String processEngineName = processEngine.getName();

    // build and start the service.
    JmxManagedProcessEngine managedProcessEngine = new JmxManagedProcessEngine(processEngine);
    serviceContainer.startService(ServiceTypes.PROCESS_ENGINE, processEngineName, managedProcessEngine);

  }

  public void unregisterProcessEngine(ProcessEngine processEngine) {
    ensureNotNull("Cannot unregister process engine in Jmx Runtime Container", "process engine", processEngine);

    serviceContainer.stopService(ServiceTypes.PROCESS_ENGINE, processEngine.getName());

  }

  public void deployProcessApplication(AbstractProcessApplication processApplication) {
    ensureNotNull("Process application", processApplication);

    final String operationName = "Deployment of Process Application "+processApplication.getName();

    serviceContainer.createDeploymentOperation(operationName)
      .addAttachment(Attachments.PROCESS_APPLICATION, processApplication)
      .addStep(new ParseProcessesXmlStep())
      .addStep(new ProcessesXmlStartProcessEnginesStep())
      .addStep(new DeployProcessArchivesStep())
      .addStep(new StartProcessApplicationServiceStep())
      .addStep(new PostDeployInvocationStep())
      .execute();

    LOGGER.info("Process Application "+processApplication.getName()+" successfully deployed.");

  }

  public void undeployProcessApplication(AbstractProcessApplication processApplication) {
    ensureNotNull("Process application", processApplication);

    final String processAppName = processApplication.getName();

    // if the process application is not deployed, ignore the request.
    if(serviceContainer.getService(ServiceTypes.PROCESS_APPLICATION, processAppName) == null) {
      return;
    }

    final String operationName = "Undeployment of Process Application "+processAppName;

    // perform the undeployment
    serviceContainer.createUndeploymentOperation(operationName)
      .addAttachment(Attachments.PROCESS_APPLICATION, processApplication)
      .addStep(new PreUndeployInvocationStep())
      .addStep(new UndeployProcessArchivesStep())
      .addStep(new ProcessesXmlStopProcessEnginesStep())
      .addStep(new StopProcessApplicationServiceStep())
      .execute();

    LOGGER.info("Process Application "+processAppName+" undeployed.");
  }

  public ProcessEngineService getProcessEngineService() {
    return this;
  }


  public ProcessApplicationService getProcessApplicationService() {
    return this;
  }

  public ExecutorService getExecutorService() {
    return serviceContainer.getServiceValue(ServiceTypes.BPM_PLATFORM, SERVICE_NAME_EXECUTOR);
  }

  // ProcessEngineServiceDelegate //////////////////////////////////////////////

  public ProcessEngine getDefaultProcessEngine() {
    return serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, "default");
  }

  public ProcessEngine getProcessEngine(String name) {
    return serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, name);
  }

  public List<ProcessEngine> getProcessEngines() {
    return serviceContainer.getServiceValuesByType(ServiceTypes.PROCESS_ENGINE);
  }

  public Set<String> getProcessEngineNames() {
    Set<String> processEngineNames = new HashSet<String>();
    List<ProcessEngine> processEngines = getProcessEngines();
    for (ProcessEngine processEngine : processEngines) {
      processEngineNames.add(processEngine.getName());
    }
    return processEngineNames;
  }

  // process application service implementation /////////////////////////////////

  public Set<String> getProcessApplicationNames() {
    List<JmxManagedProcessApplication> processApplications = serviceContainer.getServiceValuesByType(ServiceTypes.PROCESS_APPLICATION);
    Set<String> processApplicationNames = new HashSet<String>();
    for (JmxManagedProcessApplication jmxManagedProcessApplication : processApplications) {
      processApplicationNames.add(jmxManagedProcessApplication.getProcessApplicationName());
    }
    return processApplicationNames;
  }

  public ProcessApplicationInfo getProcessApplicationInfo(String processApplicationName) {

    JmxManagedProcessApplication processApplicationService = serviceContainer.getServiceValue(ServiceTypes.PROCESS_APPLICATION, processApplicationName);

    if(processApplicationService == null) {
      return null;
    } else {
      return processApplicationService.getProcessApplicationInfo();
    }
  }

  // Getter / Setter ////////////////////////////////////////////////////////////

  public PlatformServiceContainer getServiceContainer() {
    return serviceContainer;
  }

}
