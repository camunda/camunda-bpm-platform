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
  protected long finishedBatchesCount;
  protected long cleanableBatchesCount;

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

  public long getFinishedBatchesCount() {
    return finishedBatchesCount;
  }

  public void setFinishedBatchesCount(long finishedBatchCount) {
    this.finishedBatchesCount = finishedBatchCount;
  }

  public long getCleanableBatchesCount() {
    return cleanableBatchesCount;
  }

  public void setCleanableBatchesCount(long cleanableBatchCount) {
    this.cleanableBatchesCount = cleanableBatchCount;
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
      dto.setFinishedBatchesCount(current.getFinishedBatchesCount());
      dto.setCleanableBatchesCount(current.getCleanableBatchesCount());
      dtos.add(dto);
    }
    return dtos;
  }

}
