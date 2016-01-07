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

import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.ReportResult;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Roman Smirnov
 *
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type"
)
@JsonSubTypes({
    @Type(value = DurationReportResultDto.class)
})
public abstract class ReportResultDto {

  protected int period;
  protected String periodUnit;

  public int getPeriod() {
    return period;
  }

  public String getPeriodUnit() {
    return periodUnit;
  }

  public static ReportResultDto fromReportResult(ReportResult reportResult) {

    ReportResultDto dto = null;

    if (reportResult instanceof DurationReportResult) {
      DurationReportResult durationReport = (DurationReportResult) reportResult;
      dto = DurationReportResultDto.fromDurationReportResult(durationReport);
    }

    dto.period = reportResult.getPeriod();
    dto.periodUnit = reportResult.getPeriodUnit().toString();

    return dto;
  }

}
