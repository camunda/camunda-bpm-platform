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
package org.camunda.bpm.engine.impl.cmd;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.TransactionLogger;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.cmmn.deployer.CmmnDeployer;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentFailListener;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessApplicationDeploymentImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceManager;
import org.camunda.bpm.engine.impl.persistence.entity.UserOperationLogManager;
import org.camunda.bpm.engine.impl.repository.CandidateDeploymentImpl;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.repository.ProcessApplicationDeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.repository.CandidateDeployment;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentHandler;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.repository.ResumePreviousBy;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.Case;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 */
public class DeployCmd implements Command<DeploymentWithDefinitions>, Serializable {

  private static final long serialVersionUID = 1L;
  private static final CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;
  private static final TransactionLogger TX_LOG = ProcessEngineLogger.TX_LOGGER;

  protected DeploymentBuilderImpl deploymentBuilder;
  protected DeploymentHandler deploymentHandler;

  public DeployCmd(DeploymentBuilderImpl deploymentBuilder) {
    this.deploymentBuilder = deploymentBuilder;
  }

  @Override
  public DeploymentWithDefinitions execute(final CommandContext commandContext) {
    if (commandContext.getProcessEngineConfiguration().isDeploymentSynchronized()) {
      // ensure serial processing of multiple deployments on the same node.
      // We experienced deadlock situations with highly concurrent deployment of multiple
      // applications on Jboss & Wildfly
      synchronized (ProcessEngine.class) {
        return doExecute(commandContext);
      }
    } else {
      return doExecute(commandContext);
    }
  }

  protected DeploymentWithDefinitions doExecute(final CommandContext commandContext) {
    DeploymentManager deploymentManager = commandContext.getDeploymentManager();

    // load deployment handler
    ProcessEngine processEngine = commandContext.getProcessEngineConfiguration().getProcessEngine();
    deploymentHandler = commandContext.getProcessEngineConfiguration()
        .getDeploymentHandlerFactory()
        .buildDeploymentHandler(processEngine);

    Set<String> deploymentIds = getAllDeploymentIds(deploymentBuilder);
    if (!deploymentIds.isEmpty()) {
      String[] deploymentIdArray = deploymentIds.toArray(new String[deploymentIds.size()]);
      List<DeploymentEntity> deployments = deploymentManager.findDeploymentsByIds(deploymentIdArray);
      ensureDeploymentsWithIdsExists(deploymentIds, deployments);
    }

    checkCreateAndReadDeployments(commandContext, deploymentIds);

    // set deployment name if it should retrieved from an existing deployment
    String nameFromDeployment = deploymentBuilder.getNameFromDeployment();
    setDeploymentName(nameFromDeployment, deploymentBuilder, commandContext);

    // get resources to re-deploy
    List<ResourceEntity> resources = getResources(deploymentBuilder, commandContext);
    // .. and add them the builder
    addResources(resources, deploymentBuilder);

    Collection<String> resourceNames = deploymentBuilder.getResourceNames();
    if (resourceNames == null || resourceNames.isEmpty()) {
      throw new NotValidException("No deployment resources contained to deploy.");
    }

    // perform deployment
    DeploymentWithDefinitions deployment = commandContext.runWithoutAuthorization(() -> {
      acquireExclusiveLock(commandContext);
      DeploymentEntity deploymentToRegister = initDeployment();
      Map<String, ResourceEntity> resourcesToDeploy =
          resolveResourcesToDeploy(commandContext, deploymentToRegister);
      Map<String, ResourceEntity> resourcesToIgnore = new HashMap<>(deploymentToRegister.getResources());
      resourcesToIgnore.keySet().removeAll(resourcesToDeploy.keySet());

      // save initial deployment resources before they are replaced with only the deployed ones
      CandidateDeployment candidateDeployment =
          CandidateDeploymentImpl.fromDeploymentEntity(deploymentToRegister);
      if (!resourcesToDeploy.isEmpty()) {
        LOG.debugCreatingNewDeployment();
        deploymentToRegister.setResources(resourcesToDeploy);
        deploy(commandContext, deploymentToRegister);
      } else {
        // if there are no resources to be deployed, find an existing deployment
        String duplicateDeploymentId =
            deploymentHandler.determineDuplicateDeployment(candidateDeployment);
        deploymentToRegister =
            commandContext.getDeploymentManager().findDeploymentById(duplicateDeploymentId);
      }

      scheduleProcessDefinitionActivation(commandContext, deploymentToRegister);

      if(deploymentBuilder instanceof ProcessApplicationDeploymentBuilder) {
        // for process application deployments, job executor registration
        // is managed by the ProcessApplicationManager
        ProcessApplicationRegistration registration = registerProcessApplication(
            commandContext,
            deploymentToRegister,
            candidateDeployment,
            resourcesToIgnore.values());

        return new ProcessApplicationDeploymentImpl(deploymentToRegister, registration);
      } else {
        registerWithJobExecutor(commandContext, deploymentToRegister);
      }

      return deploymentToRegister;
    });

    createUserOperationLog(deploymentBuilder, deployment, commandContext);

    return deployment;
  }

  protected void acquireExclusiveLock(CommandContext commandContext) {
    if (commandContext.getProcessEngineConfiguration().isDeploymentLockUsed()) {
      // Acquire global exclusive lock: this ensures that there can be only one
      // transaction in the cluster which is allowed to perform deployments.
      // This is important to ensure that duplicate filtering works correctly
      // in a multi-node cluster. See also https://app.camunda.com/jira/browse/CAM-2128

      // It is also important to ensure the uniqueness of a process definition key,
      // version and tenant-id since there is no database constraint to check it.

      commandContext.getPropertyManager().acquireExclusiveLock();
    } else {
      LOG.warnDisabledDeploymentLock();
    }
  }

  protected Map<String, ResourceEntity> resolveResourcesToDeploy(
      CommandContext commandContext,
      DeploymentEntity candidateDeployment) {

    Map<String, ResourceEntity> resourcesToDeploy = new HashMap<>();
    Map<String, ResourceEntity> candidateResources = candidateDeployment.getResources();

    if (deploymentBuilder.isDuplicateFilterEnabled()) {

      if (candidateDeployment.getName() == null) {
        LOG.warnFilteringDuplicatesEnabledWithNullDeploymentName();
      }

      String source = candidateDeployment.getSource();
      if (source == null || source.isEmpty()) {
        source = ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE;
      }

      Map<String, ResourceEntity> existingResources = commandContext
          .getResourceManager()
          .findLatestResourcesByDeploymentName(
              candidateDeployment.getName(),
              candidateResources.keySet(),
              source,
              candidateDeployment.getTenantId());

      for (ResourceEntity deployedResource : candidateResources.values()) {
        String resourceName = deployedResource.getName();
        ResourceEntity existingResource = existingResources.get(resourceName);

        if (existingResource == null
            || existingResource.isGenerated()
            || deploymentHandler.shouldDeployResource(deployedResource, existingResource)) {

          if (deploymentBuilder.isDeployChangedOnly()) {
            // resource should be deployed
            resourcesToDeploy.put(resourceName, deployedResource);
          } else {
            // all resources should be deployed
            resourcesToDeploy = candidateResources;
            break;
          }
        }
      }

    } else {
      resourcesToDeploy = candidateResources;
    }

    return resourcesToDeploy;
  }

  protected void deploy(CommandContext commandContext, DeploymentEntity deployment) {
    deployment.setNew(true);
    commandContext.getDeploymentManager().insertDeployment(deployment);
  }

  protected void scheduleProcessDefinitionActivation(CommandContext commandContext,
      DeploymentWithDefinitions deployment) {

    if (deploymentBuilder.getProcessDefinitionsActivationDate() != null) {
      RepositoryService repositoryService = commandContext.getProcessEngineConfiguration()
          .getRepositoryService();

      for (ProcessDefinition processDefinition: getDeployedProcesses(commandContext, deployment)) {

        // If activation date is set, we first suspend all the process definition
        repositoryService
            .updateProcessDefinitionSuspensionState()
            .byProcessDefinitionId(processDefinition.getId())
            .suspend();

        // And we schedule an activation at the provided date
        repositoryService
            .updateProcessDefinitionSuspensionState()
            .byProcessDefinitionId(processDefinition.getId())
            .executionDate(deploymentBuilder.getProcessDefinitionsActivationDate())
            .activate();
      }
    }
  }

  protected ProcessApplicationRegistration registerProcessApplication(CommandContext commandContext,
      DeploymentEntity deploymentToRegister,
      CandidateDeployment candidateDeployment, Collection ignoredResources) {

    ProcessApplicationDeploymentBuilderImpl appDeploymentBuilder = (ProcessApplicationDeploymentBuilderImpl) deploymentBuilder;
    final ProcessApplicationReference appReference = appDeploymentBuilder.getProcessApplicationReference();

    // build set of deployment ids this process app should be registered for:
    Set<String> deploymentsToRegister = new HashSet<>(Collections.singleton(deploymentToRegister.getId()));
    if (appDeploymentBuilder.isResumePreviousVersions()) {
      String resumePreviousBy = appDeploymentBuilder.getResumePreviousVersionsBy();

      switch (resumePreviousBy) {

      case ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME:

        deploymentsToRegister.addAll(deploymentHandler
            .determineDeploymentsToResumeByDeploymentName(candidateDeployment));
        break;

      case ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY:
      default:

        String[] processDefinitionKeys = getProcessDefinitionsFromResources(commandContext,
            deploymentToRegister,
            ignoredResources);

        // only determine deployments to resume if there are actual process definitions to look for
        if (processDefinitionKeys.length > 0) {
          deploymentsToRegister.addAll(deploymentHandler
              .determineDeploymentsToResumeByProcessDefinitionKey(processDefinitionKeys));
        }
        break;
      }
    }

    // register process application for deployments
    return new RegisterProcessApplicationCmd(deploymentsToRegister, appReference).execute(commandContext);
  }

  protected void registerWithJobExecutor(CommandContext commandContext, Deployment deployment) {
    try {
      new RegisterDeploymentCmd(deployment.getId()).execute(commandContext);

    } finally {
      DeploymentFailListener listener = new DeploymentFailListener(deployment.getId(),
          commandContext.getProcessEngineConfiguration().getCommandExecutorTxRequiresNew());

      try {
        commandContext.getTransactionContext().addTransactionListener(TransactionState.ROLLED_BACK, listener);
      } catch (Exception e) {
        TX_LOG.debugTransactionOperation("Could not register transaction synchronization. Probably the TX has already been rolled back by application code.");
        listener.execute(commandContext);
      }
    }
  }

  // setters, initializers etc.

  protected void createUserOperationLog(DeploymentBuilderImpl deploymentBuilder, Deployment deployment, CommandContext commandContext) {
    UserOperationLogManager logManager = commandContext.getOperationLogManager();

    List<PropertyChange> properties = new ArrayList<>();

    PropertyChange filterDuplicate = new PropertyChange("duplicateFilterEnabled", null, deploymentBuilder.isDuplicateFilterEnabled());
    properties.add(filterDuplicate);

    if (deploymentBuilder.isDuplicateFilterEnabled()) {
      PropertyChange deployChangedOnly = new PropertyChange("deployChangedOnly", null, deploymentBuilder.isDeployChangedOnly());
      properties.add(deployChangedOnly);
    }

    logManager.logDeploymentOperation(UserOperationLogEntry.OPERATION_TYPE_CREATE, deployment.getId(), deployment.getTenantId(), properties);
  }

  protected DeploymentEntity initDeployment() {
    DeploymentEntity deployment = deploymentBuilder.getDeployment();
    deployment.setDeploymentTime(ClockUtil.getCurrentTime());
    return deployment;
  }

  protected void setDeploymentName(String deploymentId, DeploymentBuilderImpl deploymentBuilder, CommandContext commandContext) {
    if (deploymentId != null && !deploymentId.isEmpty()) {
      DeploymentManager deploymentManager = commandContext.getDeploymentManager();
      DeploymentEntity deployment = deploymentManager.findDeploymentById(deploymentId);
      deploymentBuilder.getDeployment().setName(deployment.getName());
    }
  }

  protected void addResources(List<ResourceEntity> resources, DeploymentBuilderImpl deploymentBuilder) {
    DeploymentEntity deployment = deploymentBuilder.getDeployment();
    Map<String, ResourceEntity> existingResources = deployment.getResources();

    for (ResourceEntity resource : resources) {
      String resourceName = resource.getName();

      if (existingResources != null && existingResources.containsKey(resourceName)) {
        String message = String.format("Cannot add resource with id '%s' and name '%s' from "
            + "deployment with id '%s' to new deployment because the new deployment contains "
            + "already a resource with same name.", resource.getId(), resourceName, resource.getDeploymentId());

        throw new NotValidException(message);
      }

      ByteArrayInputStream inputStream = new ByteArrayInputStream(resource.getBytes());
      deploymentBuilder.addInputStream(resourceName, inputStream);
    }
  }

  // getters

  protected List<String> getMissingElements(Set<String> expected, Map<String, ?> actual) {
    List<String> missingElements = new ArrayList<>();
    for (String value : expected) {
      if (!actual.containsKey(value)) {
        missingElements.add(value);
      }
    }
    return missingElements;
  }

  protected List<ResourceEntity> getResources(final DeploymentBuilderImpl deploymentBuilder, final CommandContext commandContext) {
    List<ResourceEntity> resources = new ArrayList<>();

    Set<String> deploymentIds = deploymentBuilder.getDeployments();
    resources.addAll(getResourcesByDeploymentId(deploymentIds, commandContext));

    Map<String, Set<String>> deploymentResourcesById = deploymentBuilder.getDeploymentResourcesById();
    resources.addAll(getResourcesById(deploymentResourcesById, commandContext));

    Map<String, Set<String>> deploymentResourcesByName = deploymentBuilder.getDeploymentResourcesByName();
    resources.addAll(getResourcesByName(deploymentResourcesByName, commandContext));

    checkDuplicateResourceName(resources);

    return resources;
  }

  protected List<ResourceEntity> getResourcesByDeploymentId(Set<String> deploymentIds, CommandContext commandContext) {
    List<ResourceEntity> result = new ArrayList<>();

    if (!deploymentIds.isEmpty()) {

      DeploymentManager deploymentManager = commandContext.getDeploymentManager();

      for (String deploymentId : deploymentIds) {
        DeploymentEntity deployment = deploymentManager.findDeploymentById(deploymentId);
        Map<String, ResourceEntity> resources = deployment.getResources();
        Collection<ResourceEntity> values = resources.values();
        result.addAll(values);
      }
    }

    return result;
  }

  protected List<ResourceEntity> getResourcesById(Map<String, Set<String>> resourcesById, CommandContext commandContext) {
    List<ResourceEntity> result = new ArrayList<>();

    ResourceManager resourceManager = commandContext.getResourceManager();

    for (String deploymentId : resourcesById.keySet()) {
      Set<String> resourceIds = resourcesById.get(deploymentId);

      String[] resourceIdArray = resourceIds.toArray(new String[resourceIds.size()]);
      List<ResourceEntity> resources = resourceManager.findResourceByDeploymentIdAndResourceIds(deploymentId, resourceIdArray);

      ensureResourcesWithIdsExist(deploymentId, resourceIds, resources);

      result.addAll(resources);
    }

    return result;
  }

  protected List<ResourceEntity> getResourcesByName(Map<String, Set<String>> resourcesByName, CommandContext commandContext) {
    List<ResourceEntity> result = new ArrayList<>();

    ResourceManager resourceManager = commandContext.getResourceManager();

    for (String deploymentId : resourcesByName.keySet()) {
      Set<String> resourceNames = resourcesByName.get(deploymentId);

      String[] resourceNameArray = resourceNames.toArray(new String[resourceNames.size()]);
      List<ResourceEntity> resources = resourceManager.findResourceByDeploymentIdAndResourceNames(deploymentId, resourceNameArray);

      ensureResourcesWithNamesExist(deploymentId, resourceNames, resources);

      result.addAll(resources);
    }

    return result;
  }

  protected List<? extends ProcessDefinition>   getDeployedProcesses(CommandContext commandContext, DeploymentWithDefinitions deployment) {
    List<? extends ProcessDefinition> deployedProcessDefinitions = deployment.getDeployedProcessDefinitions();
    if (deployedProcessDefinitions == null) {
      // existing deployment
      ProcessDefinitionManager manager = commandContext.getProcessDefinitionManager();
      deployedProcessDefinitions = manager.findProcessDefinitionsByDeploymentId(deployment.getId());
    }

    return deployedProcessDefinitions;
  }

  protected String[] getProcessDefinitionsFromResources(CommandContext commandContext,
      DeploymentEntity deploymentToRegister,
      Collection ignoredResources) {

    Set<String> processDefinitionKeys = new HashSet<>();

    // get process definition keys for already available process definitions
    processDefinitionKeys.addAll(parseProcessDefinitionKeys(ignoredResources));

    // get process definition keys for updated process definitions
    for (ProcessDefinition processDefinition :
        getDeployedProcesses(commandContext, deploymentToRegister)) {
      if (processDefinition.getVersion() > 1) {
        processDefinitionKeys.add(processDefinition.getKey());
      }
    }

    return processDefinitionKeys.toArray(new String[processDefinitionKeys.size()]);
  }

  protected Set<String> parseProcessDefinitionKeys(Collection<Resource> resources) {
    Set<String> processDefinitionKeys = new HashSet<>(resources.size());

    for (Resource resource : resources) {
      if (isBpmnResource(resource)) {

        ByteArrayInputStream byteStream = new ByteArrayInputStream(resource.getBytes());
        BpmnModelInstance model = Bpmn.readModelFromStream(byteStream);
        for (Process process : model.getDefinitions().getChildElementsByType(Process.class)) {
          processDefinitionKeys.add(process.getId());
        }
      } else if (isCmmnResource(resource)) {

        ByteArrayInputStream byteStream = new ByteArrayInputStream(resource.getBytes());
        CmmnModelInstance model = Cmmn.readModelFromStream(byteStream);
        for (Case cmmnCase : model.getDefinitions().getCases()) {
          processDefinitionKeys.add(cmmnCase.getId());
        }
      }
    }

    return processDefinitionKeys;
  }

  protected Set<String> getAllDeploymentIds(DeploymentBuilderImpl deploymentBuilder) {
    Set<String> result = new HashSet<>();

    String nameFromDeployment = deploymentBuilder.getNameFromDeployment();
    if (nameFromDeployment != null && !nameFromDeployment.isEmpty()) {
      result.add(nameFromDeployment);
    }

    Set<String> deployments = deploymentBuilder.getDeployments();
    result.addAll(deployments);

    deployments = deploymentBuilder.getDeploymentResourcesById().keySet();
    result.addAll(deployments);

    deployments = deploymentBuilder.getDeploymentResourcesByName().keySet();
    result.addAll(deployments);

    return result;
  }

  // checkers

  protected void checkDuplicateResourceName(List<ResourceEntity> resources) {
    Map<String, ResourceEntity> resourceMap = new HashMap<>();

    for (ResourceEntity resource : resources) {
      String name = resource.getName();

      ResourceEntity duplicate = resourceMap.get(name);
      if (duplicate != null) {
        String deploymentId = resource.getDeploymentId();
        if (!deploymentId.equals(duplicate.getDeploymentId())) {
          String message = String.format("The deployments with id '%s' and '%s' contain a resource with same name '%s'.", deploymentId, duplicate.getDeploymentId(), name);
          throw new NotValidException(message);
        }
      }
      resourceMap.put(name, resource);
    }
  }

  protected void checkCreateAndReadDeployments(CommandContext commandContext, Set<String> deploymentIds) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateDeployment();
      for (String deploymentId : deploymentIds) {
        checker.checkReadDeployment(deploymentId);
      }
    }
  }

  protected boolean isBpmnResource(Resource resourceEntity) {
    return StringUtil.hasAnySuffix(resourceEntity.getName(), BpmnDeployer.BPMN_RESOURCE_SUFFIXES);
  }

  protected boolean isCmmnResource(Resource resourceEntity) {
    return StringUtil.hasAnySuffix(resourceEntity.getName(), CmmnDeployer.CMMN_RESOURCE_SUFFIXES);
  }

  // ensures

  protected void ensureDeploymentsWithIdsExists(Set<String> expected, List<DeploymentEntity> actual) {
    Map<String, DeploymentEntity> deploymentMap = new HashMap<>();
    for (DeploymentEntity deployment : actual) {
      deploymentMap.put(deployment.getId(), deployment);
    }

    List<String> missingDeployments = getMissingElements(expected, deploymentMap);

    if (!missingDeployments.isEmpty()) {
      StringBuilder builder = new StringBuilder();

      builder.append("The following deployments are not found by id: ");
      builder.append(StringUtil.join(missingDeployments.iterator()));

      throw new NotFoundException(builder.toString());
    }
  }

  protected void ensureResourcesWithIdsExist(String deploymentId, Set<String> expectedIds, List<ResourceEntity> actual) {
    Map<String, ResourceEntity> resources = new HashMap<>();
    for (ResourceEntity resource : actual) {
      resources.put(resource.getId(), resource);
    }
    ensureResourcesWithKeysExist(deploymentId, expectedIds, resources, "id");
  }

  protected void ensureResourcesWithNamesExist(String deploymentId, Set<String> expectedNames, List<ResourceEntity> actual) {
    Map<String, ResourceEntity> resources = new HashMap<>();
    for (ResourceEntity resource : actual) {
      resources.put(resource.getName(), resource);
    }
    ensureResourcesWithKeysExist(deploymentId, expectedNames, resources, "name");
  }

  protected void ensureResourcesWithKeysExist(String deploymentId, Set<String> expectedKeys, Map<String, ResourceEntity> actual, String valueProperty) {
    List<String> missingResources = getMissingElements(expectedKeys, actual);

    if (!missingResources.isEmpty()) {
      StringBuilder builder = new StringBuilder();

      builder.append("The deployment with id '");
      builder.append(deploymentId);
      builder.append("' does not contain the following resources with ");
      builder.append(valueProperty);
      builder.append(": ");
      builder.append(StringUtil.join(missingResources.iterator()));

      throw new NotFoundException(builder.toString());
    }
  }

  /**
   * When CockroachDB is used, this command may be retried multiple times until
   * it is successful, or the retries are exhausted. CockroachDB uses a stricter,
   * SERIALIZABLE transaction isolation which ensures a serialized manner
   * of transaction execution. A concurrent transaction that attempts to modify
   * the same data as another transaction is required to abort, rollback and retry.
   * This also makes our use-case of pessimistic locks redundant since we only use
   * them as synchronization barriers, and not to lock actual data which would
   * protect it from concurrent modifications.
   *
   * The Deploy command only executes internal code, so we are certain that a retry
   * of a failed deployment will not impact user data, and may be performed multiple times.
   */
  @Override
  public boolean isRetryable() {
    return true;
  }
}
