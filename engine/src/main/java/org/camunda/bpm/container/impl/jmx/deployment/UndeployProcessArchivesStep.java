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

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.deployment.util.DeployedProcessArchive;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessApplication;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>Deployment operation responsible for undeploying all process archives.</p>
 *
 * @author Daniel Meyer
 *
 */
public class UndeployProcessArchivesStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Stopping process engines";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {

    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    final AbstractProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);
    final JmxManagedProcessApplication deployedProcessApplication = serviceContainer.getService(ServiceTypes.PROCESS_APPLICATION, processApplication.getName());

    if(deployedProcessApplication == null) {
      throw new ProcessEngineException("Cannot find process application with name "+processApplication.getName()+".");
    }

    Map<String, DeployedProcessArchive> deploymentMap = deployedProcessApplication.getProcessArchiveDeploymentMap();

    List<ProcessesXml> processesXmls = deployedProcessApplication.getProcessesXmls();
    for (ProcessesXml processesXml : processesXmls) {
      for (ProcessArchiveXml parsedProcessArchive : processesXml.getProcessArchives()) {
        operationContext.addStep(new UndeployProcessArchiveStep(deployedProcessApplication, parsedProcessArchive, deploymentMap.get(parsedProcessArchive.getName()).getProcessEngineName()));
      }
    }

  }

}
