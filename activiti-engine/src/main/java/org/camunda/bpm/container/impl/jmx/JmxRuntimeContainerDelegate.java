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

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.container.impl.jmx.deployment.Attachments;
import org.camunda.bpm.container.impl.jmx.deployment.DeployProcessArchivesStep;
import org.camunda.bpm.container.impl.jmx.deployment.ParseProcessesXmlStep;
import org.camunda.bpm.container.impl.jmx.deployment.StartProcessApplicationServiceStep;
import org.camunda.bpm.container.impl.jmx.deployment.StartProcessEnginesStep;
import org.camunda.bpm.container.impl.jmx.deployment.StopProcessApplicationServiceStep;
import org.camunda.bpm.container.impl.jmx.deployment.StopProcessEnginesStep;
import org.camunda.bpm.container.impl.jmx.deployment.UndeployProcessArchivesStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer.ServiceType;
import org.camunda.bpm.container.impl.jmx.services.JmxProcessEngine;
import org.camunda.bpm.container.spi.RuntimeContainerDelegate;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.jobexecutor.JobExecutorService;
import com.camunda.fox.platform.jobexecutor.impl.acquisition.JobAcquisition;

/**
 * <p>This is the default {@link RuntimeContainerDelegate} implementation that delegates
 * to the local {@link MBeanServer} infrastructure. The MBeanServer is available
 * as per the Java Virtual Machine and allows the process engine to expose
 * Management Resources.</p>
 * 
 * <p>Many container implementations such as JBoss Application Server 5/6 (and before) 
 * provide a custom MBean Server implementation. Using the MBeanServer as our default 
 * Container allows us to register the process engine resources along other management 
 * resources provided by those containers.</p>
 * 
 * <p>Note that this implementation does not maintain any internal state but rather 
 * delegates all state management to the {@link MBeanServer}.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class JmxRuntimeContainerDelegate implements RuntimeContainerDelegate, ProcessEngineService, JobExecutorService, ProcessApplicationService {

  protected static String BASE_REALM = "org.camunda.bpm.platform";
  protected static String ENGINE_REALM = BASE_REALM + ".process-engine";
  protected static String JOB_EXECUTOR_REALM = BASE_REALM + ".job-executor";
  protected static String PROCESS_APPLICATION_REALM = BASE_REALM + ".process-application";
  
  final protected MBeanServiceContainer serviceContainer = new MBeanServiceContainer();
  
  /**
   * The service types managed by this container.
   * 
   */
  public enum ServiceTypes implements ServiceType {
    
    PROCESS_ENGINE(ENGINE_REALM),
    JOB_ACQUISITION(JOB_EXECUTOR_REALM),
    PROCESS_APPLICATION(PROCESS_APPLICATION_REALM);

    protected String serviceRealm;

    private ServiceTypes(String serviceRealm) {
      this.serviceRealm = serviceRealm;
    }
    
    public ObjectName getServiceName(String localName) {
      try {
        return new ObjectName(serviceRealm+":type=" + localName);
      } catch (Exception e) {
        throw new FoxPlatformException("Could not compose name for ProcessEngineMBean", e);
      }
    }
    
    public ObjectName getTypeName() {
      try {
        return new ObjectName(serviceRealm + ":type=*");
      } catch (Exception e) {
        throw new FoxPlatformException("Could not compose name for ProcessEngineMBean", e);
      }
    }
                
  }
  
  // Container delegate implementation ///////////////////////////////////////////////
  
  public void registerProcessEngine(ProcessEngine processEngine) {
    
    if(processEngine == null) {
      throw new ActivitiException("Cannot register process engine with MBeans Container: process engine 'null'");
    }
    
    JmxProcessEngine processEngineMBeanImpl = new JmxProcessEngine(processEngine, true);
    serviceContainer.startService(ServiceTypes.PROCESS_ENGINE, processEngine.getName(), processEngineMBeanImpl);
        
  }
  
  public void unregisterProcessEngine(ProcessEngine processEngine) {
    
    if(processEngine == null) {
      throw new ActivitiException("Cannot unregister process engine with MBeans Container: process engine 'null'");
    }
    
    serviceContainer.stopService(ServiceTypes.PROCESS_ENGINE, processEngine.getName());
    
  }
  
  public void registerJobAcquisition(JobAcquisition jobAcquisitionConfiguration) {
    
  }
  
  public void unregisterJobAcquisition(JobAcquisition jobAcquisitionConfiguration) {
    
  }

  public void deployProcessApplication(ProcessApplication processApplication) {
    
    final String operationName = "Deployment of Process Application "+processApplication.getName();
    
    serviceContainer.createDeploymentOperation(operationName)
      .addAttachment(Attachments.PROCESS_APPLICATION, processApplication)
      .addStep(new ParseProcessesXmlStep())
      .addStep(new StartProcessEnginesStep())
      .addStep(new DeployProcessArchivesStep())
      .addStep(new StartProcessApplicationServiceStep())
      .execute();
    	  
  }

  public void undeployProcessApplication(ProcessApplication processApplication) {
    
    final String operationName = "Undeployment of Process Application "+processApplication.getName();
    
    serviceContainer.createUndeploymentOperation(operationName)
      .addAttachment(Attachments.PROCESS_APPLICATION, processApplication)
      .addStep(new StopProcessEnginesStep())
      .addStep(new UndeployProcessArchivesStep())
      .addStep(new StopProcessApplicationServiceStep())
      .execute();
    
  }
  
  public ProcessEngineService getProcessEngineService() {
    return this;
  }

  public JobExecutorService getJobExecutorService() {
    return this;
  }
  
  public ProcessApplicationService getProcessApplicationService() {
    return this;
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

  // JobExecutorService implementation /////////////////////////////////////////
  
  public JobExecutor getJobAcquisitionByName(String name) {
    return serviceContainer.getServiceValue(ServiceTypes.JOB_ACQUISITION, name);
  }

  public List<JobExecutor> getJobAcquisitions() {
    return serviceContainer.getServiceValuesByType(ServiceTypes.JOB_ACQUISITION);
  }

 
}
