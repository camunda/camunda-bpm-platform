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

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;

/**
 * <p>History event handler that writes history events to the process engine
 * database using the DbSqlSession.</p>
 * 
 * <p>This history implementation is INSERT-only: when writing history events to
 * the database we do not perform updates but rather append all events to their
 * respective tables.
 * 
 * @author Daniel Meyer
 * 
 */
public class DbHistoryEventHandler implements HistoryEventHandler {

  public void handleEvent(HistoryEvent historyEvent) {
    
    insert(historyEvent);

  }

  /** general history event insert behavior */
  protected void insert(HistoryEvent historyEvent) {
    DbSqlSession dbSqlSession = getDbSqlSession();
    dbSqlSession.insert(historyEvent);
  }

  /** customized insert behvior for HistoricVariableUpdateEventEntity */
  protected void insertHistoricVariableUpdateEntity(HistoricVariableUpdateEventEntity historyEvent) {
    DbSqlSession dbSqlSession = getDbSqlSession();
    
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
  
  protected DbSqlSession getDbSqlSession() {
    return Context.getCommandContext().getDbSqlSession();
  }

}
