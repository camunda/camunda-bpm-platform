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
package org.camunda.bpm.container.impl.jmx;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.container.impl.jmx.deployment.Attachments;
import org.camunda.bpm.container.impl.jmx.deployment.DeployProcessArchivesStep;
import org.camunda.bpm.container.impl.jmx.deployment.ParseProcessesXmlStep;
import org.camunda.bpm.container.impl.jmx.deployment.PostDeployInvocationStep;
import org.camunda.bpm.container.impl.jmx.deployment.PreUndeployInvocationStep;
import org.camunda.bpm.container.impl.jmx.deployment.ProcessesXmlStartProcessEnginesStep;
import org.camunda.bpm.container.impl.jmx.deployment.ProcessesXmlStopProcessEnginesStep;
import org.camunda.bpm.container.impl.jmx.deployment.StartProcessApplicationServiceStep;
import org.camunda.bpm.container.impl.jmx.deployment.StopProcessApplicationServiceStep;
import org.camunda.bpm.container.impl.jmx.deployment.UndeployProcessArchivesStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer.ServiceType;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessApplication;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngine;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>This is the default {@link RuntimeContainerDelegate} implementation that delegates
 * to the local {@link MBeanServer} infrastructure. The MBeanServer is available
 * as per the Java Virtual Machine and allows the process engine to expose
 * Management Resources.</p>
 *
 * @author Daniel Meyer
 *
 */
public class JmxRuntimeContainerDelegate implements RuntimeContainerDelegate, ProcessEngineService, ProcessApplicationService {

  private final Logger LOGGER = Logger.getLogger(JmxRuntimeContainerDelegate.class.getName());

  protected static String BASE_REALM = "org.camunda.bpm.platform";
  protected static String ENGINE_REALM = BASE_REALM + ".process-engine";
  protected static String JOB_EXECUTOR_REALM = BASE_REALM + ".job-executor";
  protected static String PROCESS_APPLICATION_REALM = BASE_REALM + ".process-application";

  public final static String SERVICE_NAME_EXECUTOR = "executor-service";

  protected MBeanServiceContainer serviceContainer = new MBeanServiceContainer();

  /**
   * The service types managed by this container.
   *
   */
  public enum ServiceTypes implements ServiceType {

    BPM_PLATFORM(BASE_REALM),
    PROCESS_ENGINE(ENGINE_REALM),
    JOB_EXECUTOR(JOB_EXECUTOR_REALM),
    PROCESS_APPLICATION(PROCESS_APPLICATION_REALM);

    protected String serviceRealm;

    private ServiceTypes(String serviceRealm) {
      this.serviceRealm = serviceRealm;
    }

    public ObjectName getServiceName(String localName) {
      try {
        return new ObjectName(serviceRealm+":type=" + localName);
      } catch (Exception e) {
        throw new ProcessEngineException("Could not compose name for ProcessEngineMBean", e);
      }
    }

    public ObjectName getTypeName() {
      try {
        return new ObjectName(serviceRealm + ":type=*");
      } catch (Exception e) {
        throw new ProcessEngineException("Could not compose name for ProcessEngineMBean", e);
      }
    }

  }

  // runtime container delegate implementation ///////////////////////////////////////////////

  public void registerProcessEngine(ProcessEngine processEngine) {

    if(processEngine == null) {
      throw new ProcessEngineException("Cannot register process engine in Jmx Runtime Container: process engine is 'null'");
    }

    String processEngineName = processEngine.getName();

    // build and start the service.
    JmxManagedProcessEngine managedProcessEngine = new JmxManagedProcessEngine(processEngine);
    serviceContainer.startService(ServiceTypes.PROCESS_ENGINE, processEngineName, managedProcessEngine);

  }

  public void unregisterProcessEngine(ProcessEngine processEngine) {

    if(processEngine == null) {
      throw new ProcessEngineException("Cannot unregister process engine in Jmx Runtime Container: process engine is 'null'");
    }

    serviceContainer.stopService(ServiceTypes.PROCESS_ENGINE, processEngine.getName());

  }

  public void deployProcessApplication(AbstractProcessApplication processApplication) {

    if(processApplication == null) {
      throw new ProcessEngineException("Process application cannot be null");
    }

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

    if(processApplication == null) {
      throw new ProcessEngineException("Process application cannot be null");
    }

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

  public MBeanServiceContainer getServiceContainer() {
    return serviceContainer;
  }

}
