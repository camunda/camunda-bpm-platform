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
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.event.*;
import org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Incident;

/**
 * <p>This HistoryEventProducer is aware of the {@link DbEntityManager} cache
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

  protected HistoricIncidentEventEntity loadIncidentEvent(Incident incident) {
    String incidentId = incident.getId();

    HistoricIncidentEventEntity cachedEntity = findInCache(HistoricIncidentEventEntity.class, incidentId);

    if(cachedEntity != null) {
      return cachedEntity;

    } else {
      return newIncidentEventEntity(incident);

    }
  }

  /** find a cached entity by primary key */
  protected <T extends HistoryEvent> T findInCache(Class<T> type, String id) {
    return Context.getCommandContext()
      .getDbEntityManager()
      .getCachedEntity(type, id);
  }

}
