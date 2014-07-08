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
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.runtime.VariableInstance;


/**
 * @author Tom Baeyens
 */
public class VariableInstanceManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectVariablesByTaskId", taskId);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectVariablesByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByCaseExecutionId(String caseExecutionId) {
    return getDbSqlSession().selectList("selectVariablesByCaseExecutionId", caseExecutionId);
  }

  public void deleteVariableInstanceByTask(TaskEntity task) {
    Map<String, VariableInstance> variableInstances = task.getVariableInstancesLocal();
    if (variableInstances!=null) {
      for (VariableInstance variableInstance: variableInstances.values()) {
        ((VariableInstanceEntity) variableInstance).delete();
      }
    }
  }

  public long findVariableInstanceCountByQueryCriteria(VariableInstanceQueryImpl variableInstanceQuery) {
    return (Long) getDbSqlSession().selectOne("selectVariableInstanceCountByQueryCriteria", variableInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstance> findVariableInstanceByQueryCriteria(VariableInstanceQueryImpl variableInstanceQuery, Page page) {
    return getDbSqlSession().selectList("selectVariableInstanceByQueryCriteria", variableInstanceQuery, page);
  }

}
