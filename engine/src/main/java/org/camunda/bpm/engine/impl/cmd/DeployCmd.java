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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
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
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Torben Lindhauer
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
    DeploymentEntity deployment = deploymentBuilder.getDeployment();

    deployment.setDeploymentTime(ClockUtil.getCurrentTime());

    DeploymentEntity existingDeployment = null;
    if ( deploymentBuilder.isDuplicateFilterEnabled() ) {
      existingDeployment = Context
        .getCommandContext()
        .getDeploymentManager()
        .findLatestDeploymentByName(deployment.getName());

      if (!((existingDeployment!=null) && !deploymentsDiffer(deployment, existingDeployment))) {
        existingDeployment = null;
      }
    }

    if(existingDeployment == null) {
      deployment.setNew(true);
      Context
        .getCommandContext()
        .getDeploymentManager()
        .insertDeployment(deployment);
    } else {
      deployment = existingDeployment;
    }

    if (deploymentBuilder.getProcessDefinitionsActivationDate() != null) {
      scheduleProcessDefinitionActivation(commandContext, deployment);
    }

    if(deploymentBuilder instanceof ProcessApplicationDeploymentBuilder) {
      // for process application deployments, job executor registration is managed by
      // process application manager
      ProcessApplicationRegistration registration = registerProcessApplication(commandContext, deployment);
      return new ProcessApplicationDeploymentImpl(deployment, registration);

    } else {
      registerWithJobExecutor(commandContext, deployment);
      return deployment;

    }
  }

  protected ProcessApplicationRegistration registerProcessApplication(CommandContext commandContext, DeploymentEntity deployment) {
    ProcessApplicationDeploymentBuilderImpl appDeploymentBuilder = (ProcessApplicationDeploymentBuilderImpl) deploymentBuilder;
    final ProcessApplicationReference appReference = appDeploymentBuilder.getProcessApplicationReference();

    boolean resumePreviousVersions = appDeploymentBuilder.isResumePreviousVersions();

    // build set of deployment ids this process app should be registered for:
    Set<String> deploymentsToRegister = new HashSet<String>(Collections.singleton(deployment.getId()));
    if(resumePreviousVersions) {
      resumePreviousVersions(commandContext, deployment, deploymentsToRegister);
    }

    // register process application for deployments
    return new RegisterProcessApplicationCmd(deploymentsToRegister, appReference).execute(commandContext);

  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void resumePreviousVersions(CommandContext commandContext, DeploymentEntity deployment, Set<String> deploymentsToRegister) {
    List<ProcessDefinitionEntity> deployedProcessDefinitions = deployment.getDeployedArtifacts(ProcessDefinitionEntity.class);
    if(deployedProcessDefinitions == null) {
      // existing deployment
      deployedProcessDefinitions = (List) new ProcessDefinitionQueryImpl(Context.getCommandContext())
        .deploymentId(deployment.getId())
        .list();
    }
    for (ProcessDefinitionEntity processDefinitionEntity : deployedProcessDefinitions) {
      if(processDefinitionEntity.getVersion() > 1) {

        // query for process definitions with the same key:
        List<ProcessDefinition> previousVersionDefinition = new ProcessDefinitionQueryImpl(commandContext)
          .processDefinitionKey(processDefinitionEntity.getKey())
          .list();

        // add their deployment IDs to the set of deployments to register
        for (ProcessDefinition processDefinition : previousVersionDefinition) {
          deploymentsToRegister.add(processDefinition.getDeploymentId());
        }
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

  protected boolean deploymentsDiffer(DeploymentEntity deployment, DeploymentEntity saved) {
    if(deployment.getResources() == null || saved.getResources() == null) {
      return true;
    }

    Map<String, ResourceEntity> resources = deployment.getResources();
    Map<String, ResourceEntity> savedResources = saved.getResources();

    for (String resourceName: resources.keySet()) {
      ResourceEntity savedResource = savedResources.get(resourceName);

      if(savedResource == null) {
        return true;
      }

      if(!savedResource.isGenerated()) {
        ResourceEntity resource = resources.get(resourceName);

        byte[] bytes = resource.getBytes();
        byte[] savedBytes = savedResource.getBytes();
        if (!Arrays.equals(bytes, savedBytes)) {
          return true;
        }
      }
    }
    return false;
  }

  protected void scheduleProcessDefinitionActivation(CommandContext commandContext, DeploymentEntity deployment) {
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

//  private boolean resourcesDiffer(ByteArrayEntity value, ByteArrayEntity other) {
//    if (value == null && other == null) {
//      return false;
//    }
//    String bytes = createKey(value.getBytes());
//    String savedBytes = other == null ? null : createKey(other.getBytes());
//    return !bytes.equals(savedBytes);
//  }
//
//  private String createKey(byte[] bytes) {
//    if (bytes == null) {
//      return "";
//    }
//    MessageDigest digest;
//    try {
//      digest = MessageDigest.getInstance("MD5");
//    } catch (NoSuchAlgorithmException e) {
//      throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
//    }
//    bytes = digest.digest(bytes);
//    return String.format("%032x", new BigInteger(1, bytes));
//  }
}
