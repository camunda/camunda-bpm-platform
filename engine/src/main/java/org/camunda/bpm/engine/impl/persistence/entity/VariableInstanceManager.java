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
import java.util.Map;

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.VariableInstanceQueryImpl;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.runtime.VariableInstance;


/**
 * @author Tom Baeyens
 */
public class VariableInstanceManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
    return getDbEntityManager().selectList("selectVariablesByTaskId", taskId);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId) {
    return getDbEntityManager().selectList("selectVariablesByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByCaseExecutionId(String caseExecutionId) {
    return getDbEntityManager().selectList("selectVariablesByCaseExecutionId", caseExecutionId);
  }

  public void deleteVariableInstanceByTask(TaskEntity task) {
    Map<String, CoreVariableInstance> variableInstances = task.getVariableInstancesLocal();
    if (variableInstances!=null) {
      for (CoreVariableInstance variableInstance: variableInstances.values()) {
        ((VariableInstanceEntity) variableInstance).delete();
      }
    }
  }

  public long findVariableInstanceCountByQueryCriteria(VariableInstanceQueryImpl variableInstanceQuery) {
    configureAuthorizationCheck(variableInstanceQuery);
    return (Long) getDbEntityManager().selectOne("selectVariableInstanceCountByQueryCriteria", variableInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstance> findVariableInstanceByQueryCriteria(VariableInstanceQueryImpl variableInstanceQuery, Page page) {
    configureAuthorizationCheck(variableInstanceQuery);
    return getDbEntityManager().selectList("selectVariableInstanceByQueryCriteria", variableInstanceQuery, page);
  }

  protected void configureAuthorizationCheck(VariableInstanceQueryImpl query) {
    getAuthorizationManager().configureVariableInstanceQuery(query);
  }

}
