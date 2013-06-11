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
package org.camunda.bpm.engine.impl.audit.handler;

import org.camunda.bpm.engine.impl.audit.AuditEvent;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;

/**
 * <p>Audit event handler that writes audit events to the 
 * process engine database using the DbSqlSession.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class DbAuditEventHandler implements AuditEventHandler {

  public void handleAuditEvent(AuditEvent auditEvent) {
    
    final DbSqlSession dbSqlSession = Context.getCommandContext()
      .getDbSqlSession();
    
    dbSqlSession.insert(auditEvent);
    
  }

}
