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
package org.camunda.bpm.container.impl.deployment.jobexecutor;

import static org.camunda.bpm.container.impl.deployment.Attachments.PROCESS_APPLICATION;

import java.util.Map;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedJobExecutor;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;
import org.camunda.bpm.container.impl.metadata.spi.JobAcquisitionXml;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.ServiceTypes;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.RuntimeContainerJobExecutor;

/**
 * <p>Deployment operation step responsible for starting a JobEexecutor</p>
 *
 * @author Daniel Meyer
 *
 */
public class StartJobAcquisitionStep extends DeploymentOperationStep {

  protected final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  protected final JobAcquisitionXml jobAcquisitionXml;

  public StartJobAcquisitionStep(JobAcquisitionXml jobAcquisitionXml) {
    this.jobAcquisitionXml = jobAcquisitionXml;

  }

  public String getName() {
    return "Start job acquisition '"+jobAcquisitionXml.getName()+"'";
  }

  public void performOperationStep(DeploymentOperation operationContext) {

    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();
    final AbstractProcessApplication processApplication = operationContext.getAttachment(PROCESS_APPLICATION);

    ClassLoader configurationClassloader = null;

    if(processApplication != null) {
      configurationClassloader = processApplication.getProcessApplicationClassloader();
    } else {
      configurationClassloader = ProcessEngineConfiguration.class.getClassLoader();
    }

    String configurationClassName = jobAcquisitionXml.getJobExecutorClassName();

    if(configurationClassName == null || configurationClassName.isEmpty()) {
      configurationClassName = RuntimeContainerJobExecutor.class.getName();
    }

    // create & instantiate the job executor class
    Class<? extends JobExecutor> jobExecutorClass = loadJobExecutorClass(configurationClassloader, configurationClassName);
    JobExecutor jobExecutor = instantiateJobExecutor(jobExecutorClass);

    // apply properties
    Map<String, String> properties = jobAcquisitionXml.getProperties();
    PropertyHelper.applyProperties(jobExecutor, properties);

    // construct service for job executor
    JmxManagedJobExecutor jmxManagedJobExecutor = new JmxManagedJobExecutor(jobExecutor);

    // deploy the job executor service into the container
    serviceContainer.startService(ServiceTypes.JOB_EXECUTOR, jobAcquisitionXml.getName(), jmxManagedJobExecutor);
  }


  protected JobExecutor instantiateJobExecutor(Class<? extends JobExecutor> configurationClass) {
    try {
      return configurationClass.newInstance();
    }
    catch (Exception e) {
      throw LOG.couldNotInstantiateJobExecutorClass(e);
    }
  }

  @SuppressWarnings("unchecked")
  protected Class<? extends JobExecutor> loadJobExecutorClass(ClassLoader processApplicationClassloader, String jobExecutorClassname) {
    try {
      return (Class<? extends JobExecutor>) processApplicationClassloader.loadClass(jobExecutorClassname);
    }
    catch (ClassNotFoundException e) {
      throw LOG.couldNotLoadJobExecutorClass(e);
    }
  }

}
