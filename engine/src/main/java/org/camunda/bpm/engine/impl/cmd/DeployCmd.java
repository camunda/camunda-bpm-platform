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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.DeploymentQueryImpl;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.cmmn.deployer.CmmnDeployer;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentFailListener;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessApplicationDeploymentImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceManager;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.repository.ProcessApplicationDeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
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
public class DeployCmd<T> implements Command<Deployment>, Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(DeployCmd.class.getName());

  protected DeploymentBuilderImpl deploymentBuilder;

  public DeployCmd(DeploymentBuilderImpl deploymentBuilder) {
    this.deploymentBuilder = deploymentBuilder;
  }

  public Deployment execute(final CommandContext commandContext) {
    // ensure serial processing of multiple deployments on the same node.
    // We experienced deadlock situations with highly concurrent deployment of multiple
    // applications on Jboss & Wildfly
    synchronized (ProcessEngine.class) {
      return doExecute(commandContext);
    }
  }

  protected Deployment doExecute(final CommandContext commandContext) {
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkCreateDeployment();
    checkReadDeployments(authorizationManager, deploymentBuilder);

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
    return commandContext.runWithoutAuthorization(new Callable<Deployment>() {
      public Deployment call() throws Exception {
        acquireExclusiveLock(commandContext);
        DeploymentEntity deployment = initDeployment();
        Map<String, ResourceEntity> resourcesToDeploy = resolveResourcesToDeploy(commandContext, deployment);
        Map<String, ResourceEntity> resourcesToIgnore = new HashMap<String, ResourceEntity>(deployment.getResources());
        resourcesToIgnore.keySet().removeAll(resourcesToDeploy.keySet());

        if (!resourcesToDeploy.isEmpty()) {
          log.fine("Creating new deployment.");
          deployment.setResources(resourcesToDeploy);
          deploy(deployment);
        } else {
          log.fine("Using existing deployment.");
          deployment = getExistingDeployment(commandContext, deployment.getName());
        }

        scheduleProcessDefinitionActivation(commandContext, deployment);

        if(deploymentBuilder instanceof ProcessApplicationDeploymentBuilder) {
          // for process application deployments, job executor registration is managed by
          // process application manager
          Set<String> processesToRegisterFor = retrieveProcessKeysFromResources(resourcesToIgnore);
          ProcessApplicationRegistration registration = registerProcessApplication(commandContext, deployment, processesToRegisterFor);
          return new ProcessApplicationDeploymentImpl(deployment, registration);
        } else {
          registerWithJobExecutor(commandContext, deployment);
        }

        return deployment;
      }
    });
  }

  protected void setDeploymentName(String deploymentId, DeploymentBuilderImpl deploymentBuilder, CommandContext commandContext) {
    if (deploymentId != null && !deploymentId.isEmpty()) {
      DeploymentManager deploymentManager = commandContext.getDeploymentManager();
      DeploymentEntity deployment = deploymentManager.findDeploymentById(deploymentId);
      ensureNotNull(NotFoundException.class, "No deployment found with id '" + deploymentId + "'", "deployment", deployment);
      deploymentBuilder.getDeployment().setName(deployment.getName());
    }
  }

  protected List<ResourceEntity> getResources(final DeploymentBuilderImpl deploymentBuilder, final CommandContext commandContext) {
    List<ResourceEntity> resources = new ArrayList<ResourceEntity>();

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
    List<ResourceEntity> result = new ArrayList<ResourceEntity>();

    if (!deploymentIds.isEmpty()) {

      DeploymentManager deploymentManager = commandContext.getDeploymentManager();

      String[] deploymentIdArray = deploymentIds.toArray(new String[deploymentIds.size()]);
      List<DeploymentEntity> deployments = deploymentManager.findDeploymentsByIds(deploymentIdArray);

      checkDeploymentsToRedeploy(deploymentIds, deployments);

      for (DeploymentEntity deployment : deployments) {
        Map<String, ResourceEntity> resources = deployment.getResources();
        Collection<ResourceEntity> values = resources.values();
        result.addAll(values);
      }
    }

    return result;
  }

  protected List<ResourceEntity> getResourcesById(Map<String, Set<String>> resourcesById, CommandContext commandContext) {
    List<ResourceEntity> result = new ArrayList<ResourceEntity>();

    ResourceManager resourceManager = commandContext.getResourceManager();

    for (String deploymentId : resourcesById.keySet()) {
      Set<String> resourceIds = resourcesById.get(deploymentId);

      String[] resourceIdArray = resourceIds.toArray(new String[resourceIds.size()]);
      List<ResourceEntity> resources = resourceManager.findResourceByDeploymentIdAndResourceIds(deploymentId, resourceIdArray);

      checkResourcesToDeployById(deploymentId, resourceIds, resources);

      result.addAll(resources);
    }

    return result;
  }

  protected List<ResourceEntity> getResourcesByName(Map<String, Set<String>> resourcesByName, CommandContext commandContext) {
    List<ResourceEntity> result = new ArrayList<ResourceEntity>();

    ResourceManager resourceManager = commandContext.getResourceManager();

    for (String deploymentId : resourcesByName.keySet()) {
      Set<String> resourceNames = resourcesByName.get(deploymentId);

      String[] resourceNameArray = resourceNames.toArray(new String[resourceNames.size()]);
      List<ResourceEntity> resources = resourceManager.findResourceByDeploymentIdAndResourceNames(deploymentId, resourceNameArray);

      checkResourcesToDeployByName(deploymentId, resourceNames, resources);

      result.addAll(resources);
    }

    return result;
  }

  protected void addResources(List<ResourceEntity> resources, DeploymentBuilderImpl deploymentBuilder) {
    for (ResourceEntity resource : resources) {
      String resourceName = resource.getName();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(resource.getBytes());
      deploymentBuilder.addInputStream(resourceName, inputStream);
    }
  }

  protected void checkDuplicateResourceName(List<ResourceEntity> resources) {
    Map<String, ResourceEntity> resourceMap = new HashMap<String, ResourceEntity>();

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

  protected void checkDeploymentsToRedeploy(Set<String> expected, List<DeploymentEntity> actual) {
    Map<String, DeploymentEntity> deploymentMap = new HashMap<String, DeploymentEntity>();
    for (DeploymentEntity deployment : actual) {
      deploymentMap.put(deployment.getId(), deployment);
    }

    List<String> missingDeployments = getMissingElements(expected, deploymentMap);

    if (!missingDeployments.isEmpty()) {
      StringBuilder builder = new StringBuilder();

      builder.append("The following deployments are not found by id: ");

      boolean first = true;
      for(String missingDeployment: missingDeployments) {
        if (!first) {
          builder.append(", ");
        } else {
          first = false;
        }
        builder.append(missingDeployment);
      }

      throw new NotFoundException(builder.toString());
    }
  }

  protected void checkResourcesToDeployById(String deploymentId, Set<String> expected, List<ResourceEntity> actual) {
    Map<String, ResourceEntity> resources = new HashMap<String, ResourceEntity>();
    for (ResourceEntity resource : actual) {
      resources.put(resource.getId(), resource);
    }
    checkResourcesToDeploy(deploymentId, expected, resources, "id");
  }

  protected void checkResourcesToDeployByName(String deploymentId, Set<String> expected, List<ResourceEntity> actual) {
    Map<String, ResourceEntity> resources = new HashMap<String, ResourceEntity>();
    for (ResourceEntity resource : actual) {
      resources.put(resource.getName(), resource);
    }
    checkResourcesToDeploy(deploymentId, expected, resources, "name");
  }

  protected void checkResourcesToDeploy(String deploymentId, Set<String> expected, Map<String, ResourceEntity> actual, String valueProperty) {
    List<String> missingResources = getMissingElements(expected, actual);

    if (!missingResources.isEmpty()) {
      StringBuilder builder = new StringBuilder();

      builder.append("The deployment with id '");
      builder.append(deploymentId);
      builder.append("' does not contain one of the following resources with ");
      builder.append(valueProperty);
      builder.append(": ");

      boolean first = true;
      for(String missingResource: missingResources) {
        if (!first) {
          builder.append(", ");
        } else {
          first = false;
        }
        builder.append(missingResource);
      }

      throw new NotFoundException(builder.toString());
    }
  }

  protected List<String> getMissingElements(Set<String> expected, Map<String, ?> actual) {
    List<String> missingElements = new ArrayList<String>();
    for (String value : expected) {
      if (!actual.containsKey(value)) {
        missingElements.add(value);
      }
    }
    return missingElements;
  }

  protected void checkReadDeployments(AuthorizationManager authorizationManager, DeploymentBuilderImpl deploymentBuilder) {
    Set<String> deploymentsToCheck = new HashSet<String>();

    String nameFromDeployment = deploymentBuilder.getNameFromDeployment();
    if (nameFromDeployment != null && !nameFromDeployment.isEmpty()) {
      deploymentsToCheck.add(nameFromDeployment);
    }

    Set<String> deployments = deploymentBuilder.getDeployments();
    if (!deployments.isEmpty()) {
      deploymentsToCheck.addAll(deployments);
    }

    deployments = deploymentBuilder.getDeploymentResourcesById().keySet();
    if (!deployments.isEmpty()) {
      deploymentsToCheck.addAll(deployments);
    }

    deployments = deploymentBuilder.getDeploymentResourcesByName().keySet();
    if (!deployments.isEmpty()) {
      deploymentsToCheck.addAll(deployments);
    }

    for (String deploymentId : deploymentsToCheck) {
      authorizationManager.checkReadDeployment(deploymentId);
    }
  }

  protected void acquireExclusiveLock(CommandContext commandContext) {
    if (Context.getProcessEngineConfiguration().isDeploymentLockUsed()) {
      // Acquire global exclusive lock: this ensures that there can be only one
      // transaction in the cluster which is allowed to perform deployments.
      // This is important to ensure that duplicate filtering works correctly
      // in a multi-node cluster. See also https://app.camunda.com/jira/browse/CAM-2128

      commandContext.getPropertyManager().acquireExclusiveLock();
    }
  }

  protected DeploymentEntity initDeployment() {
    DeploymentEntity deployment = deploymentBuilder.getDeployment();
    deployment.setDeploymentTime(ClockUtil.getCurrentTime());
    return deployment;
  }

  protected Map<String, ResourceEntity> resolveResourcesToDeploy(CommandContext commandContext, DeploymentEntity deployment) {
    Map<String, ResourceEntity> resourcesToDeploy = new HashMap<String, ResourceEntity>();
    Map<String, ResourceEntity> containedResources = deployment.getResources();

    if (deploymentBuilder.isDuplicateFilterEnabled()) {

      String source = deployment.getSource();
      if (source == null || source.isEmpty()) {
        source = ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE;
      }

      Map<String, ResourceEntity> existingResources = commandContext
          .getResourceManager()
          .findLatestResourcesByDeploymentName(deployment.getName(), containedResources.keySet(), source);

      for (ResourceEntity deployedResource : containedResources.values()) {
        String resourceName = deployedResource.getName();
        ResourceEntity existingResource = existingResources.get(resourceName);

        if (existingResource == null
            || existingResource.isGenerated()
            || resourcesDiffer(deployedResource, existingResource)) {
          // resource should be deployed

          if (deploymentBuilder.isDeployChangedOnly()) {
            resourcesToDeploy.put(resourceName, deployedResource);
          } else {
            // all resources should be deployed
            resourcesToDeploy = containedResources;
            break;
          }
        }
      }

    } else {
      resourcesToDeploy = containedResources;
    }

    return resourcesToDeploy;
  }

  protected boolean resourcesDiffer(ResourceEntity resource, ResourceEntity existing) {
    byte[] bytes = resource.getBytes();
    byte[] savedBytes = existing.getBytes();
    return !Arrays.equals(bytes, savedBytes);
  }

  protected void deploy(DeploymentEntity deployment) {
    deployment.setNew(true);
    Context
      .getCommandContext()
      .getDeploymentManager()
      .insertDeployment(deployment);
  }

  protected DeploymentEntity getExistingDeployment(CommandContext commandContext, String deploymentName) {
    return commandContext
        .getDeploymentManager()
        .findLatestDeploymentByName(deploymentName);
  }

  protected void scheduleProcessDefinitionActivation(CommandContext commandContext, DeploymentEntity deployment) {
    if (deploymentBuilder.getProcessDefinitionsActivationDate() != null) {
      for (ProcessDefinitionEntity processDefinitionEntity : deployment.getDeployedArtifacts(ProcessDefinitionEntity.class)) {

        // If activation date is set, we first suspend all the process definition
        SuspendProcessDefinitionCmd suspendProcessDefinitionCmd =
                new SuspendProcessDefinitionCmd(processDefinitionEntity, false, null);
        suspendProcessDefinitionCmd.execute(commandContext);

        // And we schedule an activation at the provided date
        ActivateProcessDefinitionCmd activateProcessDefinitionCmd =
                new ActivateProcessDefinitionCmd(processDefinitionEntity, false, deploymentBuilder.getProcessDefinitionsActivationDate());
        activateProcessDefinitionCmd.execute(commandContext);
      }
    }
  }

  protected ProcessApplicationRegistration registerProcessApplication(CommandContext commandContext, DeploymentEntity deployment,
      Set<String> processKeysToRegisterFor) {
    ProcessApplicationDeploymentBuilderImpl appDeploymentBuilder = (ProcessApplicationDeploymentBuilderImpl) deploymentBuilder;
    final ProcessApplicationReference appReference = appDeploymentBuilder.getProcessApplicationReference();

    // build set of deployment ids this process app should be registered for:
    Set<String> deploymentsToRegister = new HashSet<String>(Collections.singleton(deployment.getId()));

    if (appDeploymentBuilder.isResumePreviousVersions()) {
      if (ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY.equals(appDeploymentBuilder.getResumePreviousVersionsBy())) {
        deploymentsToRegister.addAll(resumePreviousByProcessDefinitionKey(commandContext, deployment, processKeysToRegisterFor));
      }else if(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME.equals(appDeploymentBuilder.getResumePreviousVersionsBy())){
        deploymentsToRegister.addAll(resumePreviousByDeploymentName(commandContext, deployment));
      }
    }

    // register process application for deployments
    return new RegisterProcessApplicationCmd(deploymentsToRegister, appReference).execute(commandContext);

  }

  /**
   * Searches in previous deployments for the same processes and retrieves the deployment ids.
   *
   * @param commandContext
   * @param deployment
   *          the current deployment
   * @param processKeysToRegisterFor
   *          the process keys this process application wants to register
   * @param deploymentsToRegister
   *          the set where to add further deployments this process application
   *          should be registered for
   * @return a set of deployment ids that contain versions of the
   *         processKeysToRegisterFor
   */
  protected Set<String> resumePreviousByProcessDefinitionKey(CommandContext commandContext, DeploymentEntity deployment, Set<String> processKeysToRegisterFor) {
    Set<String> processDefinitionKeys = new HashSet<String>(processKeysToRegisterFor);

    List<? extends ProcessDefinition> deployedProcesses = getDeployedProcesses(deployment);
    for (ProcessDefinition deployedProcess : deployedProcesses) {
      if (deployedProcess.getVersion() > 1) {
        processDefinitionKeys.add(deployedProcess.getKey());
      }
    }

    return findDeploymentIdsForProcessDefinitions(commandContext, processDefinitionKeys);
  }

  /**
   * Searches for previous deployments with the same name.
   * @param commandContext
   * @param deployment the current deployment
   * @return a set of deployment ids
   */
  protected Set<String> resumePreviousByDeploymentName(CommandContext commandContext, DeploymentEntity deployment) {
    List<Deployment> previousDeployments = new DeploymentQueryImpl().deploymentName(deployment.getName()).list();
    Set<String> deploymentIds = new HashSet<String>(previousDeployments.size());
    for (Deployment d : previousDeployments) {
      deploymentIds.add(d.getId());
    }
    return deploymentIds;
  }

  protected List<? extends ProcessDefinition> getDeployedProcesses(DeploymentEntity deployment) {
    List<? extends ProcessDefinition> deployedProcessDefinitions = deployment.getDeployedArtifacts(ProcessDefinitionEntity.class);
    if (deployedProcessDefinitions == null) {
      // existing deployment
      CommandContext commandContext = Context.getCommandContext();
      ProcessDefinitionManager manager = commandContext.getProcessDefinitionManager();
      deployedProcessDefinitions = manager.findProcessDefinitionsByDeploymentId(deployment.getId());
    }

    return deployedProcessDefinitions;
  }

  protected Set<String> retrieveProcessKeysFromResources(Map<String, ResourceEntity> resources) {
    Set<String> keys = new HashSet<String>();

    for (ResourceEntity resource : resources.values()) {
      if (isBpmnResource(resource)) {

        ByteArrayInputStream byteStream = new ByteArrayInputStream(resource.getBytes());
        BpmnModelInstance model = Bpmn.readModelFromStream(byteStream);
        for (Process process : model.getDefinitions().getChildElementsByType(Process.class)) {
          keys.add(process.getId());
        }
      } else if (isCmmnResource(resource)) {

        ByteArrayInputStream byteStream = new ByteArrayInputStream(resource.getBytes());
        CmmnModelInstance model = Cmmn.readModelFromStream(byteStream);
        for (Case cmmnCase : model.getDefinitions().getCases()) {
          keys.add(cmmnCase.getId());
        }
      }
    }

    return keys;
  }

  protected boolean isBpmnResource(ResourceEntity resourceEntity) {
    return StringUtil.hasAnySuffix(resourceEntity.getName(), BpmnDeployer.BPMN_RESOURCE_SUFFIXES);
  }

  protected boolean isCmmnResource(ResourceEntity resourceEntity) {
    return StringUtil.hasAnySuffix(resourceEntity.getName(), CmmnDeployer.CMMN_RESOURCE_SUFFIXES);
  }

  protected Set<String> findDeploymentIdsForProcessDefinitions(CommandContext commandContext, Set<String> processDefinitionKeys) {
    Set<String> deploymentsToRegister = new HashSet<String>();

    if (!processDefinitionKeys.isEmpty()) {

      String[] keys = processDefinitionKeys.toArray(new String[processDefinitionKeys.size()]);
      ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();
      List<ProcessDefinition> previousDefinitions = processDefinitionManager.findProcessDefinitionsByKeyIn(keys);

      for (ProcessDefinition definition : previousDefinitions) {
        deploymentsToRegister.add(definition.getDeploymentId());
      }
    }
    return deploymentsToRegister;
  }

  protected void registerWithJobExecutor(CommandContext commandContext, DeploymentEntity deployment) {
    try {
      new RegisterDeploymentCmd(deployment.getId()).execute(commandContext);

    } finally {
      DeploymentFailListener listener = new DeploymentFailListener(deployment.getId());

      try {
        commandContext.getTransactionContext().addTransactionListener(TransactionState.ROLLED_BACK, listener);
      } catch (Exception e) {
        log.log(Level.FINE, "Could not register transaction synchronization. Probably the TX has already been rolled back by application code.", e);
        listener.execute(commandContext);
      }
    }
  }
}
