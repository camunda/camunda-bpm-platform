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

import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.impl.HistoricDetailQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailManager extends AbstractHistoricManager {

  public void deleteHistoricDetailsByProcessInstanceId(String historicProcessInstanceId) {
    if (isHistoryEnabled()) {
      getDbEntityManager().delete(ByteArrayEntity.class, "deleteHistoricDetailsByProcessInstanceId_byteArray", historicProcessInstanceId);
      getDbEntityManager().delete(HistoricDetailEventEntity.class, "deleteHistoricDetailsByProcessInstanceId", historicProcessInstanceId);
    }
  }

  public long findHistoricDetailCountByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery) {
    return (Long) getDbEntityManager().selectOne("selectHistoricDetailCountByQueryCriteria", historicVariableUpdateQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDetail> findHistoricDetailsByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery, Page page) {
    return getDbEntityManager().selectList("selectHistoricDetailsByQueryCriteria", historicVariableUpdateQuery, page);
  }

  public void deleteHistoricDetailsByTaskId(String taskId) {
    if (isHistoryEnabled()) {
      getDbEntityManager().delete(ByteArrayEntity.class, "deleteHistoricDetailsByTaskId_byteArray", taskId);
      getDbEntityManager().delete(HistoricDetailEventEntity.class, "deleteHistoricDetailsByTaskId", taskId);
    }
  }
}
