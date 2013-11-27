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
package org.camunda.bpm.cockpit.impl.plugin.base.dto;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;


public class ProcessInstanceDto {

  protected String id;
  protected String businessKey;
  protected Date startTime;
  protected List<IncidentStatisticsDto> incidents;
  protected int suspensionState;

  public ProcessInstanceDto() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public List<IncidentStatisticsDto> getIncidents() {
    return incidents;
  }

  public void setIncidents(List<IncidentStatisticsDto> incidents) {
    this.incidents = incidents;
  }

  public boolean isSuspended() {
    return SuspensionState.SUSPENDED.getStateCode() == suspensionState;
  }

  protected void setSuspensionState(int state) {
    this.suspensionState = state;
  }

}
