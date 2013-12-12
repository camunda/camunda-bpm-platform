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
package org.camunda.bpm.engine.impl.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentFailListener;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationManager {

  private Logger LOGGER = Logger.getLogger(ProcessApplicationManager.class.getName());

  protected Map<String, DefaultProcessApplicationRegistration> registrationsByDeploymentId = new HashMap<String, DefaultProcessApplicationRegistration>();

  public ProcessApplicationReference getProcessApplicationForDeployment(String deploymentId) {
    DefaultProcessApplicationRegistration registration = registrationsByDeploymentId.get(deploymentId);
    if (registration != null) {
      return registration.getReference();
    } else {
      return null;
    }
  }

  public synchronized ProcessApplicationRegistration registerProcessApplicationForDeployments(Set<String> deploymentsToRegister, ProcessApplicationReference reference) {
    // create process application registration
    DefaultProcessApplicationRegistration registration = createProcessApplicationRegistration(deploymentsToRegister, reference);
    // register with job executor
    createJobExecutorRegistrations(deploymentsToRegister);
    logRegistration(deploymentsToRegister, reference);
    return registration;
  }

  public synchronized void unregisterProcessApplicationForDeployments(Set<String> deploymentIds, boolean removeProcessesFromCache) {
    removeJobExecutorRegistrations(deploymentIds);
    removeProcessApplicationRegistration(deploymentIds, removeProcessesFromCache);
  }

  protected DefaultProcessApplicationRegistration createProcessApplicationRegistration(Set<String> deploymentsToRegister, ProcessApplicationReference reference) {
    final String processEngineName = Context.getProcessEngineConfiguration().getProcessEngineName();

    DefaultProcessApplicationRegistration registration = new DefaultProcessApplicationRegistration(reference, deploymentsToRegister, processEngineName);
    // add to registration map
    for (String deploymentId : deploymentsToRegister) {
      registrationsByDeploymentId.put(deploymentId, registration);
    }
    return registration;
  }

  protected void removeProcessApplicationRegistration(final Set<String> deploymentIds, boolean removeProcessesFromCache) {
    for (String deploymentId : deploymentIds) {
      try {
        if(removeProcessesFromCache) {
          Context.getProcessEngineConfiguration()
            .getDeploymentCache()
            .removeDeployment(deploymentId);
        }
      } catch (Throwable t) {
        LOGGER.log(Level.WARNING, "unregistering process application for deployment but could not remove process definitions from deployment cache. ", t);

      } finally {
        if(deploymentId != null) {
          registrationsByDeploymentId.remove(deploymentId);
        }
      }
    }
  }

  protected void createJobExecutorRegistrations(Set<String> deploymentIds) {
    try {
      Context.getCommandContext()
        .getTransactionContext()
        .addTransactionListener(TransactionState.ROLLED_BACK, new DeploymentFailListener(deploymentIds));

      Set<String> registeredDeployments = Context.getProcessEngineConfiguration().getRegisteredDeployments();
      registeredDeployments.addAll(deploymentIds);

    } catch (Exception e) {
      throw new ProcessEngineException("Could not register deployments with Job Executor.", e);

    }
  }

  protected void removeJobExecutorRegistrations(Set<String> deploymentIds) {
    try {
      Set<String> registeredDeployments = Context.getProcessEngineConfiguration().getRegisteredDeployments();
      registeredDeployments.removeAll(deploymentIds);

    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Could not unregister deployments with Job Executor.", e);

    }
  }

  // logger ////////////////////////////////////////////////////////////////////////////

  protected void logRegistration(Set<String> deploymentIds, ProcessApplicationReference reference) {
    try {
      StringBuilder builder = new StringBuilder();
      builder.append("ProcessApplication '");
      builder.append(reference.getName());
      builder.append("' registered for DB deployments ");
      builder.append(deploymentIds);
      builder.append(". ");

      List<ProcessDefinition> processDefinitions = new ArrayList<ProcessDefinition>();
      for (String deploymentId : deploymentIds) {
        DeploymentEntity deployment = Context.getCommandContext()
          .getDbSqlSession()
          .selectById(DeploymentEntity.class, deploymentId);
        if(deployment != null) {
          // in case deployment was created by this command
          List<ProcessDefinitionEntity> deployedArtifacts = deployment.getDeployedArtifacts(ProcessDefinitionEntity.class);
          if(deployedArtifacts != null) {
            processDefinitions.addAll(deployedArtifacts);
          } else {
            // query db
            processDefinitions.addAll(new ProcessDefinitionQueryImpl(Context.getCommandContext())
                .deploymentId(deploymentId)
                .list());
          }
        }
      }

      if(processDefinitions.isEmpty()) {
        builder.append("Deployment does not provide any process definitions.");

      } else {
        builder.append("Will execute process definitions ");
        builder.append("\n");
        for (ProcessDefinition processDefinition : processDefinitions) {
          builder.append("\n");
          builder.append("        ");
          builder.append(processDefinition.getKey());
          builder.append("[version: ");
          builder.append(processDefinition.getVersion());
          builder.append(", id: ");
          builder.append(processDefinition.getId());
          builder.append("]");
        }
        builder.append("\n");
      }

      LOGGER.info(builder.toString());

    } catch(Throwable e) {
      // ignore
      LOGGER.log(Level.WARNING, "Exception while logging registration summary", e);
    }
  }

  public String getRegistrationSummary() {
    StringBuilder builder = new StringBuilder();
    for (Entry<String, DefaultProcessApplicationRegistration> entry : registrationsByDeploymentId.entrySet()) {
      if(builder.length()>0) {
        builder.append(", ");
      }
      builder.append(entry.getKey());
      builder.append("->");
      builder.append(entry.getValue().getReference().getName());
    }
    return builder.toString();
  }

}
