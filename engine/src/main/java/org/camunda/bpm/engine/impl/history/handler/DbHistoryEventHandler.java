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
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * <p>History event handler that writes history events to the 
 * process engine database using the DbSqlSession.</p>
 * 
 * <p>This history implementation is INSERT-only: when writing history
 * events to the database we do not perform updates but rather append 
 * all events to their respective tables. 
 * 
 * @author Daniel Meyer
 *
 */
public class DbHistoryEventHandler implements HistoryEventHandler {

  public void handleEvent(HistoryEvent historyEvent) {
    
    final DbSqlSession dbSqlSession = Context.getCommandContext()
      .getDbSqlSession();
    
    dbSqlSession.insert(historyEvent);
    
  }

}
