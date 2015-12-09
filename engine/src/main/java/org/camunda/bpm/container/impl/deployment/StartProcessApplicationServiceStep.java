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

import static org.camunda.bpm.container.impl.deployment.Attachments.PROCESSES_XML_RESOURCES;
import static org.camunda.bpm.container.impl.deployment.Attachments.PROCESS_APPLICATION;
import static org.camunda.bpm.container.impl.deployment.Attachments.PROCESS_ARCHIVE_DEPLOYMENT_MAP;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationDeploymentInfo;
import org.camunda.bpm.application.impl.ProcessApplicationDeploymentInfoImpl;
import org.camunda.bpm.application.impl.ProcessApplicationInfoImpl;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.container.impl.deployment.util.DeployedProcessArchive;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedBpmPlatformPlugins;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessApplication;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugin;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.ServiceTypes;

/**
 * <p>This deployment operation step starts an {@link MBeanService} for the process application.</p>
 *
 * @author Daniel Meyer
 *
 */
public class StartProcessApplicationServiceStep extends DeploymentOperationStep {

  public String getName() {
    return "Start Process Application Service";
  }

  public void performOperationStep(DeploymentOperation operationContext) {

    final AbstractProcessApplication processApplication = operationContext.getAttachment(PROCESS_APPLICATION);
    final Map<URL, ProcessesXml> processesXmls = operationContext.getAttachment(PROCESSES_XML_RESOURCES);
    final Map<String, DeployedProcessArchive> processArchiveDeploymentMap = operationContext.getAttachment(PROCESS_ARCHIVE_DEPLOYMENT_MAP);
    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();

    ProcessApplicationInfoImpl processApplicationInfo = createProcessApplicationInfo(processApplication, processArchiveDeploymentMap);

    // create service
    JmxManagedProcessApplication mbean = new JmxManagedProcessApplication(processApplicationInfo, processApplication.getReference());
    mbean.setProcessesXmls(new ArrayList<ProcessesXml>(processesXmls.values()));
    mbean.setDeploymentMap(processArchiveDeploymentMap);

    // start service
    serviceContainer.startService(ServiceTypes.PROCESS_APPLICATION, processApplication.getName(), mbean);

    notifyBpmPlatformPlugins(serviceContainer, processApplication);
  }

  protected ProcessApplicationInfoImpl createProcessApplicationInfo(final AbstractProcessApplication processApplication,
      final Map<String, DeployedProcessArchive> processArchiveDeploymentMap) {
    // populate process application info
    ProcessApplicationInfoImpl processApplicationInfo = new ProcessApplicationInfoImpl();

    processApplicationInfo.setName(processApplication.getName());
    processApplicationInfo.setProperties(processApplication.getProperties());

    // create deployment infos
    List<ProcessApplicationDeploymentInfo> deploymentInfoList = new ArrayList<ProcessApplicationDeploymentInfo>();
    if(processArchiveDeploymentMap != null) {
      for (Entry<String, DeployedProcessArchive> deployment : processArchiveDeploymentMap.entrySet()) {

        final DeployedProcessArchive deployedProcessArchive = deployment.getValue();
        for (String deploymentId : deployedProcessArchive.getAllDeploymentIds()) {
          ProcessApplicationDeploymentInfoImpl deploymentInfo = new ProcessApplicationDeploymentInfoImpl();
          deploymentInfo.setDeploymentId(deploymentId);
          deploymentInfo.setProcessEngineName(deployedProcessArchive.getProcessEngineName());
          deploymentInfoList.add(deploymentInfo);
        }

      }
    }

    processApplicationInfo.setDeploymentInfo(deploymentInfoList);

    return processApplicationInfo;
  }

  protected void notifyBpmPlatformPlugins(PlatformServiceContainer serviceContainer, AbstractProcessApplication processApplication) {
    JmxManagedBpmPlatformPlugins plugins =
        serviceContainer.getService(ServiceTypes.BPM_PLATFORM, RuntimeContainerDelegateImpl.SERVICE_NAME_PLATFORM_PLUGINS);

    if (plugins != null) {
      for (BpmPlatformPlugin  plugin : plugins.getValue().getPlugins()) {
        plugin.postProcessApplicationDeploy(processApplication);
      }
    }
  }

}
