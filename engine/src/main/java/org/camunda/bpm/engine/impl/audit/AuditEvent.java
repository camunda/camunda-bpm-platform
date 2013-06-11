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
package org.camunda.bpm.engine.impl.audit;

import java.io.Serializable;

import org.camunda.bpm.engine.impl.audit.handler.AuditEventHandler;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.db.PersistentObject;

/**
 * <p>The base class for all audit events.</p>
 * 
 * <p>An audit event contains data about an event that has happened 
 * in a process instance. Such an event may be the start of an activity,
 * the end of an activity, a task instance that is created or the 
 * like... </p> 
 * 
 * <p>Audit events contain data in a serializable form. Some 
 * implementations may persist audit events directly or may serialize 
 * them as an intermediate representation for later processing 
 * (ie. for an asynchronous implementation).</p>
 * 
 * <p>This class implements {@link PersistentObject} as well in oder 
 * for {@link AuditEvent}s to be easily persistable using the 
 * {@link DbSqlSession}. This may not be used by all {@link AuditEventHandler} 
 * implementations but itdoes also not cause harm.</p>
 *    
 * @author Daniel Meyer
 *
 */
public class AuditEvent implements Serializable, PersistentObject {
  
  private static final long serialVersionUID = 1L;
  
  /** each {@link AuditEvent} has a unique id */
  protected String id;
  
  /** the process instance in which the event has happened */
  protected String processInstanceId;
  
  /** the id of the execution in which the event has happened */
  protected String executionId;
  
  /** the id of the process definition */
  protected String processDefinitionId;
  
  /** a timestamp taken at the moment in time this event happens */
  protected Long timestamp;
  
  // getters / setters ///////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  public String getExecutionId() {
    return executionId;
  }
  
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  
  public Long getTimestamp() {
    return timestamp;
  }
  
  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  // persistent object implementation ///////////////

  public Object getPersistentState() {    
    // audit events are immutable
    return AuditEvent.class;
  }

}
