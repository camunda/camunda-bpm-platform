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

import org.camunda.bpm.engine.history.TaskReportResult;

/**
 * @author Stefan Hentschel.
 */
public class HistoricTaskInstanceReportResultDto {

  protected String definition;
  protected Long count;

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public static HistoricTaskInstanceReportResultDto fromHistoricTaskInstanceReportResult(TaskReportResult taskReportResult) {
    HistoricTaskInstanceReportResultDto dto = new HistoricTaskInstanceReportResultDto();

    dto.count = taskReportResult.getCount();
    dto.definition = taskReportResult.getDefinition();

    return dto;
  }
}
