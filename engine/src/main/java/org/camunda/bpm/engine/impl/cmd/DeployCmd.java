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

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.cmmn.deployer.CmmnDeployer;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentFailListener;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessApplicationDeploymentImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.repository.ProcessApplicationDeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
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

  public Deployment execute(CommandContext commandContext) {

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

      Map<String, ResourceEntity> existingResources = commandContext
          .getResourceManager()
          .findLatestResourcesByDeploymentName(deployment.getName(), containedResources.keySet());

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
      Set<String> additionalProcessKeysToRegisterFor) {
    ProcessApplicationDeploymentBuilderImpl appDeploymentBuilder = (ProcessApplicationDeploymentBuilderImpl) deploymentBuilder;
    final ProcessApplicationReference appReference = appDeploymentBuilder.getProcessApplicationReference();

    // build set of deployment ids this process app should be registered for:
    Set<String> deploymentsToRegister = new HashSet<String>(Collections.singleton(deployment.getId()));

    if (appDeploymentBuilder.isResumePreviousVersions()) {
      Set<String> processDefinitionKeys = new HashSet<String>();

      List<ProcessDefinitionEntity> deployedProcesses = getDeployedProcesses(deployment);
      for (ProcessDefinitionEntity deployedProcess : deployedProcesses) {
        if (deployedProcess.getVersion() > 1) {
          processDefinitionKeys.add(deployedProcess.getKey());
        }
      }

      processDefinitionKeys.addAll(additionalProcessKeysToRegisterFor);
      resumePreviousVersions(commandContext, processDefinitionKeys, deploymentsToRegister);
    }

    // register process application for deployments
    return new RegisterProcessApplicationCmd(deploymentsToRegister, appReference).execute(commandContext);

  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected List<ProcessDefinitionEntity> getDeployedProcesses(DeploymentEntity deployment) {
    List<ProcessDefinitionEntity> deployedProcessDefinitions = deployment.getDeployedArtifacts(ProcessDefinitionEntity.class);
    if(deployedProcessDefinitions == null) {
      // existing deployment
      deployedProcessDefinitions = (List) new ProcessDefinitionQueryImpl(Context.getCommandContext())
        .deploymentId(deployment.getId())
        .list();
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

  protected void resumePreviousVersions(CommandContext commandContext, Set<String> processDefinitionKeys, Set<String> deploymentsToRegister) {
    for (String processDefinitionKey : processDefinitionKeys) {
      // query for process definitions with that key:
      List<ProcessDefinition> previousVersionDefinition = new ProcessDefinitionQueryImpl(commandContext)
        .processDefinitionKey(processDefinitionKey)
        .list();

      // add their deployment IDs to the set of deployments to register
      for (ProcessDefinition processDefinition : previousVersionDefinition) {
        deploymentsToRegister.add(processDefinition.getDeploymentId());
      }
    }
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
