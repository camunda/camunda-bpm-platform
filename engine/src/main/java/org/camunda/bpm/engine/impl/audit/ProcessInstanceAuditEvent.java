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

/**
 * <p>{@link AuditEvent} signifying a top-level event in a process instance.</p>  
 *
 * @author Daniel Meyer
 *
 */
public class ProcessInstanceAuditEvent extends ActivityInstanceAuditEvent {

  private static final long serialVersionUID = 1L;
  
  /** the id of the activity that ended the process instance */
  protected String endActivityId;
  
  /** the business key of the process instance */
  protected String businessKey;
  
  /** the id of the user that started the process instance */
  protected String startUserId;
  
  /** the id of the activity at which the process instance was started */
  protected String startActivityId;
  
  /** the id of the super process instance */
  protected String superProcessInstanceId;

  /** the reason why this process instance was cancelled (deleted) */
  protected String deleteReason;

  // getters / setters ////////////////////////////////////////

  public String getEndActivityId() {
    return endActivityId;
  }

  public void setEndActivityId(String endActivityId) {
    this.endActivityId = endActivityId;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getStartUserId() {
    return startUserId;
  }

  public void setStartUserId(String startUserId) {
    this.startUserId = startUserId;
  }

  public String getStartActivityId() {
    return startActivityId;
  }

  public void setStartActivityId(String startActivityId) {
    this.startActivityId = startActivityId;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }  

}
