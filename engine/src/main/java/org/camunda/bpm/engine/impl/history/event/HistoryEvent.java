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
package org.camunda.bpm.engine.impl.history.event;

import java.io.Serializable;
import java.util.Date;

import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.db.PersistentObject;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

/**
 * <p>The base class for all history events.</p>
 * 
 * <p>A history event contains data about an event that has happened 
 * in a process instance. Such an event may be the start of an activity,
 * the end of an activity, a task instance that is created or other similar
 * events...</p> 
 * 
 * <p>History events contain data in a serializable form. Some 
 * implementations may persist events directly or may serialize 
 * them as an intermediate representation for later processing 
 * (ie. in an asynchronous implementation).</p>
 * 
 * <p>This class implements {@link PersistentObject}. This was chosen so
 * that {@link HistoryEvent}s can be easily persisted using the 
 * {@link DbSqlSession}. This may not be used by all {@link HistoryEventHandler} 
 * implementations but it does also not cause harm.</p>
 *    
 * @author Daniel Meyer
 *
 */
public class HistoryEvent implements Serializable, PersistentObject {
  
  private static final long serialVersionUID = 1L;
  
  /** each {@link HistoryEvent} has a unique id */
  protected String id;
  
  /** the process instance in which the event has happened */
  protected String processInstanceId;
  
  /** the id of the execution in which the event has happened */
  protected String executionId;
  
  /** the id of the process definition */
  protected String processDefinitionId;
  
  /** a timestamp taken at the moment in time this event happens */
  protected Date timestamp;
  
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
  
  public Date getTimestamp() {
    return timestamp;
  }
  
  public void setTimestamp(Date timestamp) {
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
    // events are immutable
    return HistoryEvent.class;
  }

}
