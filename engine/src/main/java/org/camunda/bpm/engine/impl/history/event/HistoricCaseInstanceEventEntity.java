/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.history.event;

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ACTIVE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.CLOSED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.COMPLETED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.FAILED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;

import java.util.Date;

/**
 * @author Sebastian Menski
 */
public class HistoricCaseInstanceEventEntity extends HistoricScopeInstanceEvent {

  private static final long serialVersionUID = 1L;

  /** the business key of the case instance */
  protected String businessKey;

  /** the id of the user that created the case instance */
  protected String createUserId;

  /** the current state of the case instance */
  protected int state;

  /** the case instance which started this case instance */
  protected String superCaseInstanceId;

  /** the process instance which started this case instance */
  protected String superProcessInstanceId;

  /** id of the tenant which belongs to the case instance  */
  protected String tenantId;

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public Date getCreateTime() {
    return getStartTime();
  }

  public void setCreateTime(Date createTime) {
    setStartTime(createTime);
  }

  public Date getCloseTime() {
    return getEndTime();
  }

  public void setCloseTime(Date closeTime) {
    setEndTime(closeTime);
  }

  public String getCreateUserId() {
    return createUserId;
  }

  public void setCreateUserId(String userId) {
    createUserId = userId;
  }

  public String getSuperCaseInstanceId() {
    return superCaseInstanceId;
  }

  public void setSuperCaseInstanceId(String superCaseInstanceId) {
    this.superCaseInstanceId = superCaseInstanceId;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public boolean isActive() {
    return state == ACTIVE.getStateCode();
  }

  public boolean isCompleted() {
    return state == COMPLETED.getStateCode();
  }

  public boolean isTerminated() {
    return state == TERMINATED.getStateCode();
  }

  public boolean isFailed() {
    return state == FAILED.getStateCode();
  }

  public boolean isSuspended() {
    return state == SUSPENDED.getStateCode();
  }

  public boolean isClosed() {
    return state == CLOSED.getStateCode();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
      + "[businessKey=" + businessKey
      + ", startUserId=" + createUserId
      + ", superCaseInstanceId=" + superCaseInstanceId
      + ", superProcessInstanceId=" + superProcessInstanceId
      + ", durationInMillis=" + durationInMillis
      + ", createTime=" + startTime
      + ", closeTime=" + endTime
      + ", id=" + id
      + ", eventType=" + eventType
      + ", caseExecutionId=" + caseExecutionId
      + ", caseDefinitionId=" + caseDefinitionId
      + ", caseInstanceId=" + caseInstanceId
      + ", tenantId=" + tenantId
      + "]";
  }
}
