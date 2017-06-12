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

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionEvaluationEvent;
import org.camunda.bpm.engine.impl.history.event.HistoricScopeInstanceEvent;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;

/**
 * <p>History event handler that writes history events to the process engine
 * database using the DbEntityManager.</p>
 *
 * @author Daniel Meyer
 *
 */
public class DbHistoryEventHandler implements HistoryEventHandler {

  public void handleEvent(HistoryEvent historyEvent) {

    if (historyEvent instanceof HistoricVariableUpdateEventEntity) {
      insertHistoricVariableUpdateEntity((HistoricVariableUpdateEventEntity) historyEvent);
    } else if(historyEvent instanceof HistoricDecisionEvaluationEvent) {
      insertHistoricDecisionEvaluationEvent((HistoricDecisionEvaluationEvent) historyEvent);
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

    final DbEntityManager dbEntityManager = getDbEntityManager();

    if(isInitialEvent(historyEvent)) {
      dbEntityManager.insert(historyEvent);
    } else {
      if(dbEntityManager.getCachedEntity(historyEvent.getClass(), historyEvent.getId()) == null) {
        if (historyEvent instanceof HistoricScopeInstanceEvent) {
          // if this is a scope, get start time from existing event in DB
          HistoricScopeInstanceEvent existingEvent = (HistoricScopeInstanceEvent) dbEntityManager.selectById(historyEvent.getClass(), historyEvent.getId());
          if(existingEvent != null) {
            HistoricScopeInstanceEvent historicScopeInstanceEvent = (HistoricScopeInstanceEvent) historyEvent;
            historicScopeInstanceEvent.setStartTime(existingEvent.getStartTime());
          }
        }
        if(historyEvent.getId() == null) {
//          dbSqlSession.insert(historyEvent);
        } else {
          dbEntityManager.merge(historyEvent);
        }
      }
    }
  }


  /** customized insert behavior for HistoricVariableUpdateEventEntity */
  protected void insertHistoricVariableUpdateEntity(HistoricVariableUpdateEventEntity historyEvent) {
    DbEntityManager dbEntityManager = getDbEntityManager();

    // insert update only if history level = FULL
    if(shouldWriteHistoricDetail(historyEvent)) {

      // insert byte array entity (if applicable)
      byte[] byteValue = historyEvent.getByteValue();
      if(byteValue != null) {
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(historyEvent.getVariableName(), byteValue);
        Context
        .getCommandContext()
        .getDbEntityManager()
        .insert(byteArrayEntity);
        historyEvent.setByteArrayId(byteArrayEntity.getId());

      }
      dbEntityManager.insert(historyEvent);
    }

    // always insert/update HistoricProcessVariableInstance
    if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_CREATE)) {
      HistoricVariableInstanceEntity persistentObject = new HistoricVariableInstanceEntity(historyEvent);
      dbEntityManager.insert(persistentObject);

    } else if (historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_UPDATE)
        || historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE)) {
      HistoricVariableInstanceEntity historicVariableInstanceEntity = dbEntityManager.selectById(HistoricVariableInstanceEntity.class, historyEvent.getVariableInstanceId());
      if(historicVariableInstanceEntity != null) {
        historicVariableInstanceEntity.updateFromEvent(historyEvent);
        historicVariableInstanceEntity.setState(HistoricVariableInstance.STATE_CREATED);

      } else {
        // #CAM-1344 / #SUPPORT-688
        // this is a FIX for process instances which were started in camunda fox 6.1 and migrated to camunda BPM 7.0.
        // in fox 6.1 the HistoricVariable instances were flushed to the DB when the process instance completed.
        // Since fox 6.2 we populate the HistoricVariable table as we go.
        HistoricVariableInstanceEntity persistentObject = new HistoricVariableInstanceEntity(historyEvent);
        dbEntityManager.insert(persistentObject);
      }

    } else if(historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_DELETE)) {
      HistoricVariableInstanceEntity historicVariableInstanceEntity = dbEntityManager.selectById(HistoricVariableInstanceEntity.class, historyEvent.getVariableInstanceId());
      if(historicVariableInstanceEntity != null) {
        historicVariableInstanceEntity.setState(HistoricVariableInstance.STATE_DELETED);
      }
    }
  }

  protected boolean shouldWriteHistoricDetail(HistoricVariableUpdateEventEntity historyEvent) {

    return Context.getProcessEngineConfiguration().getHistoryLevel()
        .isHistoryEventProduced(HistoryEventTypes.VARIABLE_INSTANCE_UPDATE_DETAIL, historyEvent)
      && !historyEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE);
  }


  protected void insertHistoricDecisionEvaluationEvent(HistoricDecisionEvaluationEvent event) {

    Context
      .getCommandContext()
      .getHistoricDecisionInstanceManager()
      .insertHistoricDecisionInstances(event);
  }


  protected boolean isInitialEvent(HistoryEvent historyEvent) {
    return historyEvent.getEventType() == null
        || historyEvent.isEventOfType(HistoryEventTypes.ACTIVITY_INSTANCE_START)
        || historyEvent.isEventOfType(HistoryEventTypes.PROCESS_INSTANCE_START)
        || historyEvent.isEventOfType(HistoryEventTypes.TASK_INSTANCE_CREATE)
        || historyEvent.isEventOfType(HistoryEventTypes.FORM_PROPERTY_UPDATE)
        || historyEvent.isEventOfType(HistoryEventTypes.INCIDENT_CREATE)
        || historyEvent.isEventOfType(HistoryEventTypes.CASE_INSTANCE_CREATE)
        || historyEvent.isEventOfType(HistoryEventTypes.DMN_DECISION_EVALUATE)
        || historyEvent.isEventOfType(HistoryEventTypes.BATCH_START)
        || historyEvent.isEventOfType(HistoryEventTypes.IDENTITY_LINK_ADD)
        || historyEvent.isEventOfType(HistoryEventTypes.IDENTITY_LINK_DELETE)
        ;
  }

  protected DbEntityManager getDbEntityManager() {
    return Context.getCommandContext().getDbEntityManager();
  }

}
