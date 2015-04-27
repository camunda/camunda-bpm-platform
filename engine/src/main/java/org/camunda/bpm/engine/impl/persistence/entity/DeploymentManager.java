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

import java.util.List;

import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.DeploymentQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.event.MessageEventHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;


/**
 * @author Tom Baeyens
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
    deleteDeployment(deploymentId, cascade, false);
  }

  public void deleteDeployment(String deploymentId, boolean cascade, boolean skipCustomListeners) {
    List<ProcessDefinition> processDefinitions = getProcessDefinitionManager().findProcessDefinitionsByDeploymentId(deploymentId);

    if (cascade) {

      // delete process instances
      for (ProcessDefinition processDefinition: processDefinitions) {
        String processDefinitionId = processDefinition.getId();

        getProcessInstanceManager()
          .deleteProcessInstancesByProcessDefinition(processDefinitionId, "deleted deployment", true, skipCustomListeners);
      }
    }

    for (ProcessDefinition processDefinition : processDefinitions) {
      String processDefinitionId = processDefinition.getId();
      // remove related authorization parameters in IdentityLink table
      getIdentityLinkManager().deleteIdentityLinksByProcDef(processDefinitionId);

      // remove timer start events:
      List<Job> timerStartJobs = getJobManager().findJobsByConfiguration(TimerStartEventJobHandler.TYPE, processDefinition.getKey());

      ProcessDefinitionEntity latestVersion = getProcessDefinitionManager().findLatestProcessDefinitionByKey(processDefinition.getKey());

      // delete timer start event jobs only if this is the latest version of the process definition.
      if(latestVersion != null && latestVersion.getId().equals(processDefinition.getId())) {
        for (Job job : timerStartJobs) {
          ((JobEntity)job).delete();
        }
      }

      if (cascade) {
        // remove historic incidents which are not referenced to a process instance
        getHistoricIncidentManager().deleteHistoricIncidentsByProcessDefinitionId(processDefinitionId);

        // remove historic op log entries which are not related to a process instance
        getUserOperationLogManager().deleteOperationLogEntriesByProcessDefinitionId(processDefinitionId);
      }
    }

    if (cascade) {
      // delete historic job logs (for example for timer start event jobs)
      getHistoricJobLogManager().deleteHistoricJobLogsByDeploymentId(deploymentId);
    }

    // delete process definitions from db
    getProcessDefinitionManager().deleteProcessDefinitionsByDeploymentId(deploymentId);

    for (ProcessDefinition processDefinition : processDefinitions) {
      String processDefinitionId = processDefinition.getId();

      // remove process definitions from cache:
      Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .removeProcessDefinition(processDefinitionId);

      // remove message event subscriptions:
      List<EventSubscriptionEntity> findEventSubscriptionsByConfiguration = getEventSubscriptionManager()
        .findEventSubscriptionsByConfiguration(MessageEventHandler.EVENT_HANDLER_TYPE, processDefinition.getId());

      for (EventSubscriptionEntity eventSubscriptionEntity : findEventSubscriptionsByConfiguration) {
        eventSubscriptionEntity.delete();
      }

      // delete job definitions
      getJobDefinitionManager().deleteJobDefinitionsByProcessDefinitionId(processDefinition.getId());

    }

    deleteCaseDeployment(deploymentId, cascade);

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

  public long findDeploymentCountByQueryCriteria(DeploymentQueryImpl deploymentQuery) {
    getAuthorizationManager().configureDeploymentQuery(deploymentQuery);
    return (Long) getDbEntityManager().selectOne("selectDeploymentCountByQueryCriteria", deploymentQuery);
  }

  @SuppressWarnings("unchecked")
  public List<Deployment> findDeploymentsByQueryCriteria(DeploymentQueryImpl deploymentQuery, Page page) {
    getAuthorizationManager().configureDeploymentQuery(deploymentQuery);
    return getDbEntityManager().selectList("selectDeploymentsByQueryCriteria", deploymentQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return getDbEntityManager().selectList("selectResourceNamesByDeploymentId", deploymentId);
  }

  public void close() {
  }

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

}
