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
package org.camunda.bpm.container.impl.ejb.deployment;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.container.impl.deployment.Attachments;
import org.camunda.bpm.container.impl.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.container.impl.metadata.spi.JobExecutorXml;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.ServiceTypes;

/**
 * <p>Deployment operation responsible registering a service which represents a Proxy to the
 * JCA-Backed {@link ExecutorService}</p>
 *
 * @author Daniel Meyer
 *
 */
public class StartJcaExecutorServiceStep extends DeploymentOperationStep {

  protected ExecutorService executorService;
  
  private static final Logger LOGGER = Logger.getLogger(StartJcaExecutorServiceStep.class.getName());

  public StartJcaExecutorServiceStep(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public String getName() {
    return "Start JCA Executor Service";
  }

  public void performOperationStep(DeploymentOperation operationContext) {
    BpmPlatformXml bpmPlatformXml = operationContext.getAttachment(Attachments.BPM_PLATFORM_XML);
    checkConfiguration(bpmPlatformXml.getJobExecutor());
    
    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();

    serviceContainer.startService(ServiceTypes.BPM_PLATFORM, RuntimeContainerDelegateImpl.SERVICE_NAME_EXECUTOR, new JcaExecutorServiceDelegate(executorService));

  }

  /**
   * Checks the validation to see if properties are present, which will be ignored
   * in this environment so we can log a warning.
   */
  private void checkConfiguration(JobExecutorXml jobExecutorXml) {
    Map<String, String> properties = jobExecutorXml.getProperties();
    for (Entry<String, String> entry : properties.entrySet()) {
      LOGGER.warning("Property " + entry.getKey() + " with value " + entry.getValue() + " from bpm-platform.xml will be ignored for JobExecutor.");
    }
  }

}
