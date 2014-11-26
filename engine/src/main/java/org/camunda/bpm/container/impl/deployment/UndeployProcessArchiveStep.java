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
package org.camunda.bpm.container.impl.deployment;

import java.util.Map;

import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.container.impl.deployment.util.DeployedProcessArchive;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessApplication;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.ServiceTypes;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;

/**
 * <p>Deployment operation step responsible for performing the undeployment of a
 * process archive</p>
 *
 * @author Daniel Meyer
 *
 */
public class UndeployProcessArchiveStep extends DeploymentOperationStep {

  protected String processArchvieName;
  protected JmxManagedProcessApplication deployedProcessApplication;
  protected ProcessArchiveXml processArchive;
  protected String processEngineName;

  public UndeployProcessArchiveStep(JmxManagedProcessApplication deployedProcessApplication, ProcessArchiveXml processArchive, String processEngineName) {
    this.deployedProcessApplication = deployedProcessApplication;
    this.processArchive = processArchive;
    this.processEngineName = processEngineName;
  }

  public String getName() {
    return "Undeploying process archvie "+processArchvieName;
  }

  public void performOperationStep(DeploymentOperation operationContext) {

    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();

    final Map<String, DeployedProcessArchive> processArchiveDeploymentMap = deployedProcessApplication.getProcessArchiveDeploymentMap();
    final DeployedProcessArchive deployedProcessArchive = processArchiveDeploymentMap.get(processArchive.getName());
    final ProcessEngine processEngine = serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, processEngineName);

    // unregrister with the process engine.
    processEngine.getManagementService().unregisterProcessApplication(deployedProcessArchive.getAllDeploymentIds(), true);

    // delete the deployment if not disabled
    if (PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_DELETE_UPON_UNDEPLOY, false)) {
      if (processEngine != null) {
        // always cascade & skip custom listeners
        deleteDeployment(deployedProcessArchive.getPrimaryDeploymentId(), processEngine.getRepositoryService());
      }
    }

  }

  protected void deleteDeployment(String deploymentId, RepositoryService repositoryService) {
    repositoryService.deleteDeployment(deploymentId, true, true);
  }

}
