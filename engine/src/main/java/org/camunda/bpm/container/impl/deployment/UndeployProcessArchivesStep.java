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
package org.camunda.bpm.container.impl.deployment;

import java.util.List;
import java.util.Map;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.deployment.util.DeployedProcessArchive;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessApplication;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.ServiceTypes;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * <p>Deployment operation responsible for undeploying all process archives.</p>
 *
 * @author Daniel Meyer
 *
 */
public class UndeployProcessArchivesStep extends DeploymentOperationStep {

  public String getName() {
    return "Stopping process engines";
  }

  public void performOperationStep(DeploymentOperation operationContext) {

    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();
    final AbstractProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);
    final JmxManagedProcessApplication deployedProcessApplication = serviceContainer.getService(ServiceTypes.PROCESS_APPLICATION, processApplication.getName());

    ensureNotNull("Cannot find process application with name " + processApplication.getName(), "deployedProcessApplication", deployedProcessApplication);

    Map<String, DeployedProcessArchive> deploymentMap = deployedProcessApplication.getProcessArchiveDeploymentMap();
    if (deploymentMap != null) {
      List<ProcessesXml> processesXmls = deployedProcessApplication.getProcessesXmls();
      for (ProcessesXml processesXml : processesXmls) {
        for (ProcessArchiveXml parsedProcessArchive : processesXml.getProcessArchives()) {
          DeployedProcessArchive deployedProcessArchive = deploymentMap.get(parsedProcessArchive.getName());
          if (deployedProcessArchive != null) {
            operationContext.addStep(new UndeployProcessArchiveStep(deployedProcessApplication, parsedProcessArchive, deployedProcessArchive.getProcessEngineName()));
          }
        }
      }
    }

  }

}
