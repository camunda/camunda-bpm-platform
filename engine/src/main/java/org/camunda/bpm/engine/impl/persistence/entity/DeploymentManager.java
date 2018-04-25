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

package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.DeploymentQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import org.camunda.bpm.engine.impl.cmd.DeleteProcessDefinitionsByIdsCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionManager;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionManager;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;


/**
 * @author Tom Baeyens
 * @author Deivarayan Azhagappan
 * @author Christopher Zell
 */
public class DeploymentManager extends AbstractManager {

  public void insertDeployment(DeploymentEntity deployment) {
    getDbEntityManager().insert(deployment);
    createDefaultAuthorizations(deployment);

    for (ResourceEntity resource : deployment.getResources().values()) {
      resource.setDeploymentId(deployment.getId());
      getResourceManager().insertResource(resource);
    }

    Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .deploy(deployment);
  }

  public void deleteDeployment(String deploymentId, boolean cascade) {
    deleteDeployment(deploymentId, cascade, false, false);
  }

  public void deleteDeployment(String deploymentId, final boolean cascade, final boolean skipCustomListeners, boolean skipIoMappings) {
    List<ProcessDefinition> processDefinitions = getProcessDefinitionManager().findProcessDefinitionsByDeploymentId(deploymentId);
    if (cascade) {
      // *NOTE*:
      // The process instances of ALL process definitions must be
      // deleted, before every process definition can be deleted!
      //
      // On deletion of all process instances, the task listeners will
      // be deleted as well. Deletion of tasks and listeners needs
      // the redeployment of deployments, which can cause to problems if
      // is done sequential with deletion of process definition.
      //
      // For example:
      // Deployment contains two process definiton. First process definition
      // and instances will be removed, also cleared from the cache.
      // Second process definition will be removed and his instances.
      // Deletion of instances will cause redeployment this deploys again
      // first into the cache. Only the second will be removed from cache and
      // first remains in the cache after the deletion process.
      //
      // Thats why we have to clear up all instances at first, after that
      // we can cleanly remove the process definitions.
      for (ProcessDefinition processDefinition: processDefinitions) {
        String processDefinitionId = processDefinition.getId();
        getProcessInstanceManager()
          .deleteProcessInstancesByProcessDefinition(processDefinitionId, "deleted deployment", true, skipCustomListeners, skipIoMappings);
      }
      // delete historic job logs (for example for timer start event jobs)
      getHistoricJobLogManager().deleteHistoricJobLogsByDeploymentId(deploymentId);
    }


    for (ProcessDefinition processDefinition : processDefinitions) {
      final String processDefinitionId = processDefinition.getId();
      // Process definition cascade true deletes the history and
      // process instances if instances flag is set as well to true.
      // Problem as described above, redeployes the deployment.
      // Represents no problem if only one process definition is deleted
      // in a transaction! We have to set the instances flag to false.
      final CommandContext commandContext = Context.getCommandContext();
      commandContext.runWithoutAuthorization(new Callable<Void>() {
        public Void call() throws Exception {
          DeleteProcessDefinitionsByIdsCmd cmd = new DeleteProcessDefinitionsByIdsCmd(
              Arrays.asList(processDefinitionId),
              cascade,
              false,
              skipCustomListeners,
              false);
          cmd.execute(commandContext);
          return null;
        }
      });
    }

    deleteCaseDeployment(deploymentId, cascade);

    deleteDecisionDeployment(deploymentId, cascade);
    deleteDecisionRequirementDeployment(deploymentId);

    getResourceManager().deleteResourcesByDeploymentId(deploymentId);

    deleteAuthorizations(Resources.DEPLOYMENT, deploymentId);
    getDbEntityManager().delete(DeploymentEntity.class, "deleteDeployment", deploymentId);

  }

  protected void deleteCaseDeployment(String deploymentId, boolean cascade) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if (processEngineConfiguration.isCmmnEnabled()) {
      List<CaseDefinition> caseDefinitions = getCaseDefinitionManager().findCaseDefinitionByDeploymentId(deploymentId);

      if (cascade) {

        // delete case instances
        for (CaseDefinition caseDefinition: caseDefinitions) {
          String caseDefinitionId = caseDefinition.getId();

          getCaseInstanceManager()
            .deleteCaseInstancesByCaseDefinition(caseDefinitionId, "deleted deployment", true);

        }
      }

      // delete case definitions from db
      getCaseDefinitionManager()
        .deleteCaseDefinitionsByDeploymentId(deploymentId);

      for (CaseDefinition caseDefinition : caseDefinitions) {
        String processDefinitionId = caseDefinition.getId();

        // remove case definitions from cache:
        Context
          .getProcessEngineConfiguration()
          .getDeploymentCache()
          .removeCaseDefinition(processDefinitionId);
      }
    }
  }

  protected void deleteDecisionDeployment(String deploymentId, boolean cascade) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if (processEngineConfiguration.isDmnEnabled()) {
      DecisionDefinitionManager decisionDefinitionManager = getDecisionDefinitionManager();
      List<DecisionDefinition> decisionDefinitions = decisionDefinitionManager.findDecisionDefinitionByDeploymentId(deploymentId);

      if(cascade) {
        // delete historic decision instances
        for(DecisionDefinition decisionDefinition : decisionDefinitions) {
          getHistoricDecisionInstanceManager().deleteHistoricDecisionInstancesByDecisionDefinitionId(decisionDefinition.getId());
        }
      }

      // delete decision definitions from db
      decisionDefinitionManager
        .deleteDecisionDefinitionsByDeploymentId(deploymentId);

      DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

      for (DecisionDefinition decisionDefinition : decisionDefinitions) {
        String decisionDefinitionId = decisionDefinition.getId();

        // remove decision definitions from cache:
        deploymentCache
          .removeDecisionDefinition(decisionDefinitionId);
      }
    }
  }

  protected void deleteDecisionRequirementDeployment(String deploymentId) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if (processEngineConfiguration.isDmnEnabled()) {
      DecisionRequirementsDefinitionManager manager = getDecisionRequirementsDefinitionManager();
      List<DecisionRequirementsDefinition> decisionRequirementsDefinitions =
          manager.findDecisionRequirementsDefinitionByDeploymentId(deploymentId);

      // delete decision requirements definitions from db
      manager.deleteDecisionRequirementsDefinitionsByDeploymentId(deploymentId);

      DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

      for (DecisionRequirementsDefinition decisionRequirementsDefinition : decisionRequirementsDefinitions) {
        String decisionDefinitionId = decisionRequirementsDefinition.getId();

        // remove decision requirements definitions from cache:
        deploymentCache.removeDecisionRequirementsDefinition(decisionDefinitionId);
      }
    }
  }

  public DeploymentEntity findLatestDeploymentByName(String deploymentName) {
    List<?> list = getDbEntityManager().selectList("selectDeploymentsByName", deploymentName, 0, 1);
    if (list!=null && !list.isEmpty()) {
      return (DeploymentEntity) list.get(0);
    }
    return null;
  }

  public DeploymentEntity findDeploymentById(String deploymentId) {
    return getDbEntityManager().selectById(DeploymentEntity.class, deploymentId);
  }

  @SuppressWarnings("unchecked")
  public List<DeploymentEntity> findDeploymentsByIds(String... deploymentsIds) {
    return getDbEntityManager().selectList("selectDeploymentsByIds", deploymentsIds);
  }

  public long findDeploymentCountByQueryCriteria(DeploymentQueryImpl deploymentQuery) {
    configureQuery(deploymentQuery);
    return (Long) getDbEntityManager().selectOne("selectDeploymentCountByQueryCriteria", deploymentQuery);
  }

  @SuppressWarnings("unchecked")
  public List<Deployment> findDeploymentsByQueryCriteria(DeploymentQueryImpl deploymentQuery, Page page) {
    configureQuery(deploymentQuery);
    return getDbEntityManager().selectList("selectDeploymentsByQueryCriteria", deploymentQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return getDbEntityManager().selectList("selectResourceNamesByDeploymentId", deploymentId);
  }

  @SuppressWarnings("unchecked")
  public List<String> findDeploymentIdsByProcessInstances(List<String> processInstanceIds) {
    return getDbEntityManager().selectList("selectDeploymentIdsByProcessInstances", processInstanceIds);
  }

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }

  // helper /////////////////////////////////////////////////

  protected void createDefaultAuthorizations(DeploymentEntity deployment) {
    if(isAuthorizationEnabled()) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();
      AuthorizationEntity[] authorizations = provider.newDeployment(deployment);
      saveDefaultAuthorizations(authorizations);
    }
  }

  protected void configureQuery(DeploymentQueryImpl query) {
    getAuthorizationManager().configureDeploymentQuery(query);
    getTenantManager().configureQuery(query);
  }

}
