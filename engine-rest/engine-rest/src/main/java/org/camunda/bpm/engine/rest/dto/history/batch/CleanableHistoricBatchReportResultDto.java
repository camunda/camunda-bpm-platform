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

package org.camunda.bpm.engine.rest.dto.history.batch;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReport;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReportResult;

public class CleanableHistoricBatchReportResultDto {

  protected String batchType;
  protected Integer historyTimeToLive;
  protected long finishedBatchCount;
  protected long cleanableBatchCount;

  public CleanableHistoricBatchReportResultDto() {
  }

  public String getBatchType() {
    return batchType;
  }

  public void setBatchType(String batchType) {
    this.batchType = batchType;
  }

  public Integer getHistoryTimeToLive() {
    return historyTimeToLive;
  }

  public void setHistoryTimeToLive(Integer historyTimeToLive) {
    this.historyTimeToLive = historyTimeToLive;
  }

  public long getFinishedBatchCount() {
    return finishedBatchCount;
  }

  public void setFinishedBatchCount(long finishedBatchCount) {
    this.finishedBatchCount = finishedBatchCount;
  }

  public long getCleanableBatchCount() {
    return cleanableBatchCount;
  }

  public void setCleanableBatchCount(long cleanableBatchCount) {
    this.cleanableBatchCount = cleanableBatchCount;
  }

  protected CleanableHistoricBatchReport createNewReportQuery(ProcessEngine engine) {
    return engine.getHistoryService().createCleanableHistoricBatchReport();
  }
  public static List<CleanableHistoricBatchReportResultDto> convert(List<CleanableHistoricBatchReportResult> reportResult) {
    List<CleanableHistoricBatchReportResultDto> dtos = new ArrayList<CleanableHistoricBatchReportResultDto>();
    for (CleanableHistoricBatchReportResult current : reportResult) {
      CleanableHistoricBatchReportResultDto dto = new CleanableHistoricBatchReportResultDto();
      dto.setBatchType(current.getBatchType());
      dto.setHistoryTimeToLive(current.getHistoryTimeToLive());
      dto.setFinishedBatchCount(current.getFinishedBatchCount());
      dto.setCleanableBatchCount(current.getCleanableBatchCount());
      dtos.add(dto);
    }
    return dtos;
  }

}
