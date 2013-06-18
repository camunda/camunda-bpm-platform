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
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;

/**
 * <p>This HistoryEventProducer is aware of the {@link DbSqlSession} cache
 * and works in combination with the {@link DbHistoryEventHandler}.</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class CacheAwareHistoryEventProducer extends DefaultHistoryEventProducer {
  
  protected HistoricActivityInstanceEventEntity createActivityInstanceEventEntity(ExecutionEntity execution) {
    return new HistoricActivityInstanceEntity();
  }
  
  protected HistoricProcessInstanceEventEntity createProcessInstanceEventEntity(ExecutionEntity execution) {
    return new HistoricProcessInstanceEntity();
  }
  
  protected HistoricTaskInstanceEventEntity createTaskInstanceEvent(DelegateTask task) {
    return new HistoricTaskInstanceEntity();
  }
  
  protected HistoricActivityInstanceEventEntity getActivityInstanceEventEntity(ExecutionEntity execution) {
    final String activityInstanceId = execution.getActivityInstanceId();
    
    HistoricActivityInstanceEntity cachedEntity = findInCache(HistoricActivityInstanceEntity.class, activityInstanceId);
    
    if(cachedEntity != null) {
      return cachedEntity;
      
    } else {      
      return createActivityInstanceEventEntity(execution);
      
    }
    
  }
  
  protected HistoricProcessInstanceEventEntity getProcessInstanceEventEntity(ExecutionEntity execution) {
    final String processInstanceId = execution.getProcessInstanceId();
    
    HistoricProcessInstanceEntity cachedEntity = findInCache(HistoricProcessInstanceEntity.class, processInstanceId);
    
    if(cachedEntity != null) {
      return cachedEntity;
      
    } else {
      return createProcessInstanceEventEntity(execution);
      
    }
    
  }
  
  protected HistoricTaskInstanceEventEntity getTaskInstanceEvent(DelegateTask task) {    
    final String taskId = task.getId();
    
    HistoricTaskInstanceEntity cachedEntity = findInCache(HistoricTaskInstanceEntity.class, taskId);
    
    if(cachedEntity != null) {
      return cachedEntity;
      
    } else {
      return createTaskInstanceEvent(task);
      
    }
  }

  /** find a cached entity by primary key */
  protected <T extends HistoryEvent> T findInCache(Class<T> type, String id) {
    return Context.getCommandContext()
      .getDbSqlSession()
      .findInCache(type, id);
  }

}
