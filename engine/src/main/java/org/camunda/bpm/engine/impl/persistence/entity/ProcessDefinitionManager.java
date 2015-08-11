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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.repository.ProcessDefinition;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Saeid Mirzaei
 */
public class ProcessDefinitionManager extends AbstractManager {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  // insert ///////////////////////////////////////////////////////////

  public void insertProcessDefinition(ProcessDefinitionEntity processDefinition) {
    getDbEntityManager().insert(processDefinition);
    createDefaultAuthorizations(processDefinition);
  }

  // select ///////////////////////////////////////////////////////////

  public ProcessDefinitionEntity findLatestProcessDefinitionByKey(String processDefinitionKey) {
    return (ProcessDefinitionEntity) getDbEntityManager().selectOne("selectLatestProcessDefinitionByKey", processDefinitionKey);
  }

  public ProcessDefinitionEntity findLatestProcessDefinitionById(String processDefinitionId) {
    return getDbEntityManager().selectById(ProcessDefinitionEntity.class, processDefinitionId);
  }

  @SuppressWarnings({ "unchecked" })
  public List<ProcessDefinition> findProcessDefinitionsByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery, Page page) {
    configureProcessDefinitionQuery(processDefinitionQuery);
    return getDbEntityManager().selectList("selectProcessDefinitionsByQueryCriteria", processDefinitionQuery, page);
  }

  public long findProcessDefinitionCountByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery) {
    configureProcessDefinitionQuery(processDefinitionQuery);
    return (Long) getDbEntityManager().selectOne("selectProcessDefinitionCountByQueryCriteria", processDefinitionQuery);
  }

  public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    return (ProcessDefinitionEntity) getDbEntityManager().selectOne("selectProcessDefinitionByDeploymentAndKey", parameters);
  }

  public ProcessDefinition findProcessDefinitionByKeyAndVersion(String processDefinitionKey, Integer processDefinitionVersion) {
    ProcessDefinitionQueryImpl processDefinitionQuery = new ProcessDefinitionQueryImpl()
      .processDefinitionKey(processDefinitionKey)
      .processDefinitionVersion(processDefinitionVersion);
    List<ProcessDefinition> results = findProcessDefinitionsByQueryCriteria(processDefinitionQuery, null);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
      throw LOG.toManyProcessDefinitionsException(results.size(), processDefinitionKey, processDefinitionVersion);
    }
    return null;
  }

  public List<ProcessDefinition> findProcessDefinitionsByKey(String processDefinitionKey) {
    ProcessDefinitionQueryImpl processDefinitionQuery = new ProcessDefinitionQueryImpl()
      .processDefinitionKey(processDefinitionKey);
    return  findProcessDefinitionsByQueryCriteria(processDefinitionQuery, null);
  }

  public List<ProcessDefinition> findProcessDefinitionsStartableByUser(String user) {
    return   new ProcessDefinitionQueryImpl().startableByUser(user).list();
  }

  public List<User> findProcessDefinitionPotentialStarterUsers() {
    return null;
  }

  public List<Group> findProcessDefinitionPotentialStarterGroups() {
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<ProcessDefinition> findProcessDefinitionsByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectProcessDefinitionByDeploymentId", deploymentId);
  }

  // update ///////////////////////////////////////////////////////////

  public void updateProcessDefinitionSuspensionStateById(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(ProcessDefinitionEntity.class, "updateProcessDefinitionSuspensionStateByParameters", parameters);
  }

  public void updateProcessDefinitionSuspensionStateByKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(ProcessDefinitionEntity.class, "updateProcessDefinitionSuspensionStateByParameters", parameters);
  }

  // delete  ///////////////////////////////////////////////////////////

  public void deleteProcessDefinitionsByDeploymentId(String deploymentId) {
    getDbEntityManager().delete(ProcessDefinitionEntity.class, "deleteProcessDefinitionsByDeploymentId", deploymentId);
  }

  // helper ///////////////////////////////////////////////////////////

  protected void createDefaultAuthorizations(ProcessDefinition processDefinition) {
    if(isAuthorizationEnabled()) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();
      AuthorizationEntity[] authorizations = provider.newProcessDefinition(processDefinition);
      saveDefaultAuthorizations(authorizations);
    }
  }

  protected void configureProcessDefinitionQuery(ProcessDefinitionQueryImpl query) {
    getAuthorizationManager().configureProcessDefinitionQuery(query);
  }
}
