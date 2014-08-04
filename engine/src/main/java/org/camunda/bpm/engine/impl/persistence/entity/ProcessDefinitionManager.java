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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.repository.ProcessDefinition;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Saeid Mirzaei
 */
public class ProcessDefinitionManager extends AbstractManager {

  public ProcessDefinitionEntity findLatestProcessDefinitionByKey(String processDefinitionKey) {
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectLatestProcessDefinitionByKey", processDefinitionKey);
  }

  public void deleteProcessDefinitionsByDeploymentId(String deploymentId) {
    getDbSqlSession().delete(ProcessDefinitionEntity.class, "deleteProcessDefinitionsByDeploymentId", deploymentId);
  }

  public ProcessDefinitionEntity findLatestProcessDefinitionById(String processDefinitionId) {
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectProcessDefinitionById", processDefinitionId);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessDefinition> findProcessDefinitionsByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery, Page page) {
//    List<ProcessDefinition> processDefinitions =
    return getDbSqlSession().selectList("selectProcessDefinitionsByQueryCriteria", processDefinitionQuery, page);

    //skipped this after discussion within the team
//    // retrieve process definitions from cache (http://jira.codehaus.org/browse/ACT-1020) to have all available information
//    ArrayList<ProcessDefinition> result = new ArrayList<ProcessDefinition>();
//    for (ProcessDefinition processDefinitionEntity : processDefinitions) {
//      ProcessDefinitionEntity fullProcessDefinition = Context
//              .getProcessEngineConfiguration()
//              .getDeploymentCache().resolveProcessDefinition((ProcessDefinitionEntity)processDefinitionEntity);
//      result.add(fullProcessDefinition);
//    }
//    return result;
  }

  public long findProcessDefinitionCountByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery) {
    return (Long) getDbSqlSession().selectOne("selectProcessDefinitionCountByQueryCriteria", processDefinitionQuery);
  }

  public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectProcessDefinitionByDeploymentAndKey", parameters);
  }

  public ProcessDefinition findProcessDefinitionByKeyAndVersion(String processDefinitionKey, Integer processDefinitionVersion) {
    ProcessDefinitionQueryImpl processDefinitionQuery = new ProcessDefinitionQueryImpl()
      .processDefinitionKey(processDefinitionKey)
      .processDefinitionVersion(processDefinitionVersion);
    List<ProcessDefinition> results = findProcessDefinitionsByQueryCriteria(processDefinitionQuery, null);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
      throw new ProcessEngineException("There are " + results.size() + " process definitions with key = '" + processDefinitionKey + "' and version = '" + processDefinitionVersion + "'.");
    }
    return null;
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

  public void updateProcessDefinitionSuspensionStateById(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(ProcessDefinitionEntity.class, "updateProcessDefinitionSuspensionStateByParameters", parameters);
  }

  public void updateProcessDefinitionSuspensionStateByKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(ProcessDefinitionEntity.class, "updateProcessDefinitionSuspensionStateByParameters", parameters);
  }


}
