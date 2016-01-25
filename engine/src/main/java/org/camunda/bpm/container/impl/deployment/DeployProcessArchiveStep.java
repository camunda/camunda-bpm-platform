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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.deployment.scanning.ProcessApplicationScanningUtil;
import org.camunda.bpm.container.impl.deployment.util.DeployedProcessArchive;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.ServiceTypes;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.camunda.bpm.engine.repository.ResumePreviousBy;

/**
 * <p>
 * Deployment operation step responsible for deploying a process archive
 * </p>
 *
 * @author Daniel Meyer
 *
 */
public class DeployProcessArchiveStep extends DeploymentOperationStep {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  protected final ProcessArchiveXml processArchive;
  protected URL metaFileUrl;
  protected ProcessApplicationDeployment deployment;

  public DeployProcessArchiveStep(ProcessArchiveXml parsedProcessArchive, URL url) {
    processArchive = parsedProcessArchive;
    this.metaFileUrl = url;
  }

  @Override
  public String getName() {
    return "Deployment of process archive '" + processArchive.getName();
  }

  @Override
  public void performOperationStep(DeploymentOperation operationContext) {

    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();
    final AbstractProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);
    final ClassLoader processApplicationClassloader = processApplication.getProcessApplicationClassloader();

    ProcessEngine processEngine = getProcessEngine(serviceContainer);

    // start building deployment map
    Map<String, byte[]> deploymentMap = new HashMap<String, byte[]>();

    // add all processes listed in the processes.xml
    List<String> listedProcessResources = processArchive.getProcessResourceNames();
    for (String processResource : listedProcessResources) {
      InputStream resourceAsStream = null;
      try {
        resourceAsStream = processApplicationClassloader.getResourceAsStream(processResource);
        byte[] bytes = IoUtil.readInputStream(resourceAsStream, processResource);
        deploymentMap.put(processResource, bytes);
      } finally {
        IoUtil.closeSilently(resourceAsStream);
      }
    }

    // scan for additional process definitions if not turned off
    if(PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS, true)) {
      String paResourceRoot = processArchive.getProperties().get(ProcessArchiveXml.PROP_RESOURCE_ROOT_PATH);
      String[] additionalResourceSuffixes = StringUtil.split(processArchive.getProperties().get(ProcessArchiveXml.PROP_ADDITIONAL_RESOURCE_SUFFIXES), ProcessArchiveXml.PROP_ADDITIONAL_RESOURCE_SUFFIXES_SEPARATOR);
      deploymentMap.putAll(findResources(processApplicationClassloader, paResourceRoot, additionalResourceSuffixes));
    }

    // perform process engine deployment
    RepositoryService repositoryService = processEngine.getRepositoryService();
    ProcessApplicationDeploymentBuilder deploymentBuilder = repositoryService.createDeployment(processApplication.getReference());

    // set the name for the deployment
    String deploymentName = processArchive.getName();
    if(deploymentName == null || deploymentName.isEmpty()) {
      deploymentName = processApplication.getName();
    }
    deploymentBuilder.name(deploymentName);

    // set the tenant id for the deployment
    String tenantId = processArchive.getTenantId();
    if(tenantId != null && !tenantId.isEmpty()) {
      deploymentBuilder.tenantId(tenantId);
    }

    // enable duplicate filtering
    deploymentBuilder.enableDuplicateFiltering(PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_DEPLOY_CHANGED_ONLY, false));

    if (PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_RESUME_PREVIOUS_VERSIONS, true)) {
      enableResumingOfPreviousVersions(deploymentBuilder);
    }

    // add all resources obtained through the processes.xml and through scanning
    for (Entry<String, byte[]> deploymentResource : deploymentMap.entrySet()) {
      deploymentBuilder.addInputStream(deploymentResource.getKey(), new ByteArrayInputStream(deploymentResource.getValue()));
    }

    // allow the process application to add additional resources to the deployment
    processApplication.createDeployment(processArchive.getName(), deploymentBuilder);

    Collection<String> deploymentResourceNames = deploymentBuilder.getResourceNames();
    if(!deploymentResourceNames.isEmpty()) {

      LOG.deploymentSummary(deploymentResourceNames, deploymentName);

      // perform the process engine deployment
      deployment = deploymentBuilder.deploy();

      // add attachment
      Map<String, DeployedProcessArchive> processArchiveDeploymentMap = operationContext.getAttachment(Attachments.PROCESS_ARCHIVE_DEPLOYMENT_MAP);
      if(processArchiveDeploymentMap == null) {
        processArchiveDeploymentMap = new HashMap<String, DeployedProcessArchive>();
        operationContext.addAttachment(Attachments.PROCESS_ARCHIVE_DEPLOYMENT_MAP, processArchiveDeploymentMap);
      }
      processArchiveDeploymentMap.put(processArchive.getName(), new DeployedProcessArchive(deployment));

    }
    else {
      LOG.notCreatingPaDeployment(processApplication.getName());
    }
  }

  protected void enableResumingOfPreviousVersions(ProcessApplicationDeploymentBuilder deploymentBuilder) throws IllegalArgumentException {
    deploymentBuilder.resumePreviousVersions();
    String resumePreviousBy = processArchive.getProperties().get(ProcessArchiveXml.PROP_RESUME_PREVIOUS_BY);
    if (resumePreviousBy == null) {
      deploymentBuilder.resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY);
    } else if (isValidValueForResumePreviousBy(resumePreviousBy)) {
      deploymentBuilder.resumePreviousVersionsBy(resumePreviousBy);
    } else {
      StringBuilder b = new StringBuilder();
      b.append("Illegal value passed for property ").append(ProcessArchiveXml.PROP_RESUME_PREVIOUS_BY);
      b.append(". Value was ").append(resumePreviousBy);
      b.append(" expected ").append(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME);
      b.append(" or ").append(ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY).append(".");
      throw LOG.illegalValueForResumePreviousByProperty(b.toString());
    }
  }

  protected boolean isValidValueForResumePreviousBy(String resumePreviousBy) {
    return resumePreviousBy.equals(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME) || resumePreviousBy.equals(ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY);
  }

  protected Map<String, byte[]> findResources(final ClassLoader processApplicationClassloader, String paResourceRoot, String[] additionalResourceSuffixes) {
    return ProcessApplicationScanningUtil.findResources(processApplicationClassloader, paResourceRoot, metaFileUrl, additionalResourceSuffixes);
  }

  @Override
  public void cancelOperationStep(DeploymentOperation operationContext) {

    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();

    ProcessEngine processEngine = getProcessEngine(serviceContainer);

    // if a registration was performed, remove it.
    if (deployment != null && deployment.getProcessApplicationRegistration() != null) {
      processEngine.getManagementService().unregisterProcessApplication(deployment.getProcessApplicationRegistration().getDeploymentIds(), true);
    }

    // delete deployment if we were able to create one AND if
    // isDeleteUponUndeploy is set.
    if (deployment != null && PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_DELETE_UPON_UNDEPLOY, false)) {
      if (processEngine != null) {
        processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
      }
    }

  }

  protected ProcessEngine getProcessEngine(final PlatformServiceContainer serviceContainer) {
    String processEngineName = processArchive.getProcessEngineName();
    if (processEngineName != null) {
      ProcessEngine processEngine = serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, processEngineName);
      ensureNotNull("Cannot deploy process archive '" + processArchive.getName() + "' to process engine '" + processEngineName
          + "' no such process engine exists", "processEngine", processEngine);
      return processEngine;

    } else {
      ProcessEngine processEngine = serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, "default");
      ensureNotNull("Cannot deploy process archive '" + processArchive.getName() + "' to default process: no such process engine exists", "processEngine",
          processEngine);
      return processEngine;
    }
  }

}
