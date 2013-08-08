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
package org.camunda.bpm.engine.impl.history.producer;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * <p>This HistoryEventProducer is aware of the {@link DbSqlSession} cache
 * and works in combination with the {@link DbHistoryEventHandler}.</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class CacheAwareHistoryEventProducer extends DefaultHistoryEventProducer {
  
   protected HistoricActivityInstanceEventEntity loadActivityInstanceEventEntity(ExecutionEntity execution) {
    final String activityInstanceId = execution.getActivityInstanceId();
    
    HistoricActivityInstanceEventEntity cachedEntity = findInCache(HistoricActivityInstanceEventEntity.class, activityInstanceId);
    
    if(cachedEntity != null) {
      return cachedEntity;
      
    } else {      
      return newActivityInstanceEventEntity(execution);
      
    }
    
  }
  
  protected HistoricProcessInstanceEventEntity loadProcessInstanceEventEntity(ExecutionEntity execution) {
    final String processInstanceId = execution.getProcessInstanceId();
    
    HistoricProcessInstanceEventEntity cachedEntity = findInCache(HistoricProcessInstanceEventEntity.class, processInstanceId);
    
    if(cachedEntity != null) {
      return cachedEntity;
      
    } else {
      return newProcessInstanceEventEntity(execution);
      
    }
    
  }
  
  protected HistoricTaskInstanceEventEntity loadTaskInstanceEvent(DelegateTask task) {    
    final String taskId = task.getId();
    
    HistoricTaskInstanceEventEntity cachedEntity = findInCache(HistoricTaskInstanceEventEntity.class, taskId);
    
    if(cachedEntity != null) {
      return cachedEntity;
      
    } else {
      return newTaskInstanceEventEntity(task);
      
    }
  }

  /** find a cached entity by primary key */
  protected <T extends HistoryEvent> T findInCache(Class<T> type, String id) {
    return Context.getCommandContext()
      .getDbSqlSession()
      .findInCache(type, id);
  }

}
