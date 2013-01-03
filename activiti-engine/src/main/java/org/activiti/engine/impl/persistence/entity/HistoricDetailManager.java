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

package org.activiti.engine.impl.persistence.entity;

import java.util.List;

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.impl.HistoricDetailQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailManager extends AbstractHistoricManager {

//  public void deleteHistoricDetail(HistoricDetailEntity historicDetail) {
//    if (historicDetail instanceof HistoricDetailVariableInstanceUpdateEntity) {
//      HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = (HistoricDetailVariableInstanceUpdateEntity) historicDetail;
//      String byteArrayValueId = historicVariableUpdate.getByteArrayValueId();
//      if (byteArrayValueId != null) {
//          // the next apparently useless line is probably to ensure consistency in the DbSqlSession 
//          // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
//          // @see also HistoricVariableInstanceEntity
//        historicVariableUpdate.getByteArrayValue();
//        Context
//          .getCommandContext()
//          .getSession(DbSqlSession.class)
//          .delete(ByteArrayEntity.class, byteArrayValueId);
//      }
//    }
//    getDbSqlSession().delete(HistoricDetailEntity.class, historicDetail.getId());
//  }

  @SuppressWarnings("unchecked")
  public void deleteHistoricDetailsByProcessInstanceId(String historicProcessInstanceId) {
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      List<HistoricDetailEntity> historicDetails = (List) getDbSqlSession()
        .createHistoricDetailQuery()
        .processInstanceId(historicProcessInstanceId)
        .list();
      
      HistoricDetailManager historicDetailManager = Context
        .getCommandContext()
        .getHistoricDetailManager();
      
      for (HistoricDetailEntity historicDetail: historicDetails) {
        historicDetail.delete();
      }
    }
  }
  
  public long findHistoricDetailCountByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery) {
    return (Long) getDbSqlSession().selectOne("selectHistoricDetailCountByQueryCriteria", historicVariableUpdateQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDetail> findHistoricDetailsByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery, Page page) {
    return getDbSqlSession().selectList("selectHistoricDetailsByQueryCriteria", historicVariableUpdateQuery, page);
  }

  public void deleteHistoricDetailsByTaskId(String taskId) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
      HistoricDetailQueryImpl detailsQuery = 
        (HistoricDetailQueryImpl) new HistoricDetailQueryImpl().taskId(taskId);
      List<HistoricDetail> details = detailsQuery.list();
      for(HistoricDetail detail : details) {
        ((HistoricDetailEntity) detail).delete();
      }
    }
  }
}
