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
package org.camunda.bpm.engine.rest.dto.history;

import org.camunda.bpm.engine.history.HistoricTaskInstanceReportResult;

/**
 * @author Stefan Hentschel.
 */
public class HistoricTaskInstanceReportResultDto {

  protected Long count;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String processDefinitionName;
  protected String taskName;
  protected String tenantId;

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public static HistoricTaskInstanceReportResultDto fromHistoricTaskInstanceReportResult(HistoricTaskInstanceReportResult taskReportResult) {
    HistoricTaskInstanceReportResultDto dto = new HistoricTaskInstanceReportResultDto();

    dto.count = taskReportResult.getCount();
    dto.processDefinitionKey = taskReportResult.getProcessDefinitionKey();
    dto.processDefinitionId = taskReportResult.getProcessDefinitionId();
    dto.processDefinitionName = taskReportResult.getProcessDefinitionName();
    dto.taskName = taskReportResult.getTaskName();
    dto.tenantId = taskReportResult.getTenantId();

    return dto;
  }
}
