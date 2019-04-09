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
package org.camunda.bpm.engine.impl.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.impl.ProcessApplicationLogger;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionManager;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentFailListener;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationManager {

  public final static ProcessApplicationLogger LOG = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

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

  public synchronized void clearRegistrations() {
    registrationsByDeploymentId.clear();
  }

  public synchronized void unregisterProcessApplicationForDeployments(Set<String> deploymentIds, boolean removeProcessesFromCache) {
    removeJobExecutorRegistrations(deploymentIds);
    removeProcessApplicationRegistration(deploymentIds, removeProcessesFromCache);
  }

  public boolean hasRegistrations() {
    return !registrationsByDeploymentId.isEmpty();
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
      }
      catch (Throwable t) {
        LOG.couldNotRemoveDefinitionsFromCache(t);
      }
      finally {
        if(deploymentId != null) {
          registrationsByDeploymentId.remove(deploymentId);
        }
      }
    }
  }

  protected void createJobExecutorRegistrations(Set<String> deploymentIds) {
    try {
      final DeploymentFailListener deploymentFailListener = new DeploymentFailListener(deploymentIds,
        Context.getProcessEngineConfiguration().getCommandExecutorTxRequiresNew());
      Context.getCommandContext()
        .getTransactionContext()
        .addTransactionListener(TransactionState.ROLLED_BACK, deploymentFailListener);

      Set<String> registeredDeployments = Context.getProcessEngineConfiguration().getRegisteredDeployments();
      registeredDeployments.addAll(deploymentIds);

    }
    catch (Exception e) {
      throw LOG.exceptionWhileRegisteringDeploymentsWithJobExecutor(e);
    }
  }

  protected void removeJobExecutorRegistrations(Set<String> deploymentIds) {
    try {
      Set<String> registeredDeployments = Context.getProcessEngineConfiguration().getRegisteredDeployments();
      registeredDeployments.removeAll(deploymentIds);

    }
    catch (Exception e) {
      LOG.exceptionWhileUnregisteringDeploymentsWithJobExecutor(e);
    }
  }

  // logger ////////////////////////////////////////////////////////////////////////////

  protected void logRegistration(Set<String> deploymentIds, ProcessApplicationReference reference) {

    if (!LOG.isInfoEnabled()) {
      // building the log message is expensive (db queries) so we avoid it if we can
      return;
    }

    try {
      StringBuilder builder = new StringBuilder();
      builder.append("ProcessApplication '");
      builder.append(reference.getName());
      builder.append("' registered for DB deployments ");
      builder.append(deploymentIds);
      builder.append(". ");

      List<ProcessDefinition> processDefinitions = new ArrayList<ProcessDefinition>();
      List<CaseDefinition> caseDefinitions = new ArrayList<CaseDefinition>();

      CommandContext commandContext = Context.getCommandContext();
      ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      boolean cmmnEnabled = processEngineConfiguration.isCmmnEnabled();

      for (String deploymentId : deploymentIds) {

        DeploymentEntity deployment = commandContext
          .getDbEntityManager()
          .selectById(DeploymentEntity.class, deploymentId);

        if(deployment != null) {

          processDefinitions.addAll(getDeployedProcessDefinitionArtifacts(deployment));

          if (cmmnEnabled) {
            caseDefinitions.addAll(getDeployedCaseDefinitionArtifacts(deployment));
          }

        }
      }

      logProcessDefinitionRegistrations(builder, processDefinitions);

      if (cmmnEnabled) {
        logCaseDefinitionRegistrations(builder, caseDefinitions);
      }

      LOG.registrationSummary(builder.toString());

    }
    catch(Throwable e) {
      LOG.exceptionWhileLoggingRegistrationSummary(e);
    }
  }

  protected List<ProcessDefinition> getDeployedProcessDefinitionArtifacts(DeploymentEntity deployment) {
    CommandContext commandContext = Context.getCommandContext();

    // in case deployment was created by this command
    List<ProcessDefinition> entities = deployment.getDeployedProcessDefinitions();

    if (entities == null) {
      String deploymentId = deployment.getId();
      ProcessDefinitionManager manager = commandContext.getProcessDefinitionManager();
      return manager.findProcessDefinitionsByDeploymentId(deploymentId);
    }

    return entities;

  }

  protected List<CaseDefinition> getDeployedCaseDefinitionArtifacts(DeploymentEntity deployment) {
    CommandContext commandContext = Context.getCommandContext();

    // in case deployment was created by this command
    List<CaseDefinition> entities = deployment.getDeployedCaseDefinitions();

    if (entities == null) {
      String deploymentId = deployment.getId();
      CaseDefinitionManager caseDefinitionManager = commandContext.getCaseDefinitionManager();
      return caseDefinitionManager.findCaseDefinitionByDeploymentId(deploymentId);
    }

    return entities;

  }

  protected void logProcessDefinitionRegistrations(StringBuilder builder, List<ProcessDefinition> processDefinitions) {
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
  }

  protected void logCaseDefinitionRegistrations(StringBuilder builder, List<CaseDefinition> caseDefinitions) {
    if(caseDefinitions.isEmpty()) {
      builder.append("Deployment does not provide any case definitions.");

    } else {
      builder.append("\n");
      builder.append("Will execute case definitions ");
      builder.append("\n");
      for (CaseDefinition caseDefinition : caseDefinitions) {
        builder.append("\n");
        builder.append("        ");
        builder.append(caseDefinition.getKey());
        builder.append("[version: ");
        builder.append(caseDefinition.getVersion());
        builder.append(", id: ");
        builder.append(caseDefinition.getId());
        builder.append("]");
      }
      builder.append("\n");
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
