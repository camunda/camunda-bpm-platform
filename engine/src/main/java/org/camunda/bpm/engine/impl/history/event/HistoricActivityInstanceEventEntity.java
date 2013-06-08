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

/**
 * <p>{@link HistoryEvent} implementation for events that happen in an activity.</p>
 * 
 * @author Daniel Meyer
 * @author Marcel Wieczorek
 * 
 */
public class HistoricActivityInstanceEventEntity extends HistoricScopeInstanceEventEntity {

  private static final long serialVersionUID = 1L;

  /** the id of the child activity instance */
  protected String calledProcessInstanceId;
  protected String taskId;
  protected String taskAssignee;

  // getters / setters ///////////////////////////////

  public String getCalledProcessInstanceId() {
    return calledProcessInstanceId;
  }

  public void setCalledProcessInstanceId(String calledProcessInstanceId) {
    this.calledProcessInstanceId = calledProcessInstanceId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getTaskAssignee() {
    return taskAssignee;
  }

  public void setTaskAssignee(String taskAssignee) {
    this.taskAssignee = taskAssignee;
  }

}
