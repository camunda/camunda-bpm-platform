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
package org.camunda.bpm.engine.impl.history.handler;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.history.event.HistoricScopeInstanceEvent;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;

/**
 * <p>History event handler that writes history events to the process engine
 * database using the DbSqlSession.</p>
 *
 * @author Daniel Meyer
 *
 */
public class DbHistoryEventHandler implements HistoryEventHandler {

  public void handleEvent(HistoryEvent historyEvent) {

    if (historyEvent instanceof HistoricVariableUpdateEventEntity) {
      insertHistoricVariableUpdateEntity((HistoricVariableUpdateEventEntity) historyEvent);
    } else {
      insertOrUpdate(historyEvent);
    }

  }

  public void handleEvents(List<HistoryEvent> historyEvents) {
    for (HistoryEvent historyEvent : historyEvents) {
      handleEvent(historyEvent);
    }
  }

  /** general history event insert behavior */
  protected void insertOrUpdate(HistoryEvent historyEvent) {

    final DbSqlSession dbSqlSession = getDbSqlSession();

    String eventType = historyEvent.getEventType();
    if(eventType == null || isInitialEvent(eventType)) {
      dbSqlSession.insert(historyEvent);
    } else {
      if(dbSqlSession.findInCache(historyEvent.getClass(), historyEvent.getId()) == null) {
        if (historyEvent instanceof HistoricScopeInstanceEvent) {
          // if this is a scope, get start time from existing event in DB
          HistoricScopeInstanceEvent existingEvent = (HistoricScopeInstanceEvent) dbSqlSession.selectById(historyEvent.getClass(), historyEvent.getId());
          if(existingEvent != null) {
            HistoricScopeInstanceEvent historicScopeInstanceEvent = (HistoricScopeInstanceEvent) historyEvent;
            historicScopeInstanceEvent.setStartTime(existingEvent.getStartTime());
          }
        }
        if(historyEvent.getId() == null) {
//          dbSqlSession.insert(historyEvent);
        } else {
          dbSqlSession.merge(historyEvent);
        }
      }
    }
  }


  /** customized insert behavior for HistoricVariableUpdateEventEntity */
  protected void insertHistoricVariableUpdateEntity(HistoricVariableUpdateEventEntity historyEvent) {
    DbSqlSession dbSqlSession = getDbSqlSession();

    // insert update only if history level = FULL
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if(historyLevel == ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {

      // insert byte array entity (if applicable)
      byte[] byteValue = historyEvent.getByteValue();
      if(byteValue != null) {
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(historyEvent.getVariableName(), byteValue);
        Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(byteArrayEntity);
        historyEvent.setByteArrayId(byteArrayEntity.getId());

      }
      dbSqlSession.insert(historyEvent);
    }

    // always insert/update HistoricProcessVariableInstance
    if(HistoryEvent.VARIABLE_EVENT_TYPE_CREATE.equals(historyEvent.getEventType())) {
      HistoricVariableInstanceEntity persistentObject = new HistoricVariableInstanceEntity(historyEvent);
      dbSqlSession.insert(persistentObject);

    } else if(HistoryEvent.VARIABLE_EVENT_TYPE_UPDATE.equals(historyEvent.getEventType())) {
      HistoricVariableInstanceEntity historicVariableInstanceEntity = dbSqlSession.selectById(HistoricVariableInstanceEntity.class, historyEvent.getVariableInstanceId());
      if(historicVariableInstanceEntity != null) {
        historicVariableInstanceEntity.updateFromEvent(historyEvent);

      } else {
        // #CAM-1344 / #SUPPORT-688
        // this is a FIX for process instances which were started in camunda fox 6.1 and migrated to camunda BPM 7.0.
        // in fox 6.1 the HistoricVariable instances were flushed to the DB when the process instance completed.
        // Since fox 6.2 we populate the HistoricVariable table as we go.
        HistoricVariableInstanceEntity persistentObject = new HistoricVariableInstanceEntity(historyEvent);
        dbSqlSession.insert(persistentObject);
      }

    } else if(HistoryEvent.VARIABLE_EVENT_TYPE_DELETE.equals(historyEvent.getEventType())) {
      HistoricVariableInstanceEntity historicVariableInstanceEntity = dbSqlSession.selectById(HistoricVariableInstanceEntity.class, historyEvent.getVariableInstanceId());
      if(historicVariableInstanceEntity != null) {
        historicVariableInstanceEntity.delete();
      }
    }

  }


  protected boolean isInitialEvent(String eventType) {
    return HistoryEvent.ACTIVITY_EVENT_TYPE_START.equals(eventType)
        || HistoryEvent.TASK_EVENT_TYPE_CREATE.equals(eventType)
        || HistoryEvent.FORM_PROPERTY_UPDATE.equals(eventType)
        || HistoryEvent.INCIDENT_CREATE.equals(eventType);
  }

  protected DbSqlSession getDbSqlSession() {
    return Context.getCommandContext().getDbSqlSession();
  }

}
