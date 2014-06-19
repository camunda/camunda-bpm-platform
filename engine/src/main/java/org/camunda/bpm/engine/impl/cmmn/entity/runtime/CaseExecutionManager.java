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
package org.camunda.bpm.engine.impl.cmmn.entity.runtime;

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionManager extends AbstractManager {

  public void insertCaseExecution(CaseExecutionEntity caseExecution) {
    getDbSqlSession().insert(caseExecution);
  }

  public void deleteCaseExecution(CaseExecutionEntity caseExecution) {
    getDbSqlSession().delete(caseExecution);
  }

  @SuppressWarnings("unchecked")
  public void deleteCaseInstancesByCaseDefinition(String caseDefinitionId, String deleteReason, boolean cascade) {
    List<String> caseInstanceIds = getDbSqlSession()
        .selectList("selectCaseInstanceIdsByCaseDefinitionId", caseDefinitionId);

    for (String caseInstanceId: caseInstanceIds) {
      deleteCaseInstance(caseInstanceId, deleteReason, cascade);
    }

  }

  public void deleteCaseInstance(String caseInstanceId, String deleteReason) {
    deleteCaseInstance(caseInstanceId, deleteReason, false);
  }

  public void deleteCaseInstance(String caseInstanceId, String deleteReason, boolean cascade) {
    CaseExecutionEntity execution = findCaseExecutionById(caseInstanceId);

    if(execution == null) {
      throw new BadUserRequestException("No case instance found for id '" + caseInstanceId + "'");
    }

    CommandContext commandContext = Context.getCommandContext();
    commandContext
      .getTaskManager()
      .deleteTasksByCaseInstanceId(caseInstanceId, deleteReason, cascade);

    execution.deleteCascade();
  }

  public CaseExecutionEntity findCaseExecutionById(String caseExecutionId) {
    return (CaseExecutionEntity) getDbSqlSession().selectById(CaseExecutionEntity.class, caseExecutionId);
  }

  public long findCaseExecutionCountByQueryCriteria(CaseExecutionQueryImpl caseExecutionQuery) {
    return (Long) getDbSqlSession().selectOne("selectCaseExecutionCountByQueryCriteria", caseExecutionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<CaseExecution> findCaseExecutionsByQueryCriteria(CaseExecutionQueryImpl caseExecutionQuery, Page page) {
    return getDbSqlSession().selectList("selectCaseExecutionsByQueryCriteria", caseExecutionQuery, page);
  }

  public long findCaseInstanceCountByQueryCriteria(CaseInstanceQueryImpl caseInstanceQuery) {
    return (Long) getDbSqlSession().selectOne("selectCaseInstanceCountByQueryCriteria", caseInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<CaseInstance> findCaseInstanceByQueryCriteria(CaseInstanceQueryImpl caseInstanceQuery, Page page) {
    return getDbSqlSession().selectList("selectCaseInstanceByQueryCriteria", caseInstanceQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<CaseExecutionEntity> findChildCaseExecutionsByParentCaseExecutionId(String parentCaseExecutionId) {
    return getDbSqlSession().selectList("selectCaseExecutionsByParentCaseExecutionId", parentCaseExecutionId);
  }

}
