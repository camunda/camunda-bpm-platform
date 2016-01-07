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
package org.camunda.bpm.engine.rest.dto;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.ReportResult;
import org.camunda.bpm.engine.query.Report;
import org.camunda.bpm.engine.query.PeriodUnit;
import org.camunda.bpm.engine.rest.dto.converter.PeriodUnitConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Roman Smirnov
 *
 */
public abstract class AbstractReportDto<T extends Report> extends AbstractSearchQueryDto {

  public static final String REPORT_TYPE_DURATION = "duration";

  public static final List<String> VALID_REPORT_TYPE_VALUES;
  static {
    VALID_REPORT_TYPE_VALUES = new ArrayList<String>();
    VALID_REPORT_TYPE_VALUES.add(REPORT_TYPE_DURATION);
  }

  protected String reportType;
  protected PeriodUnit periodUnit;

  // required for populating via jackson
  public AbstractReportDto() {
  }

  public AbstractReportDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("reportType")
  public void setReportType(String reportType) {
    if (!VALID_REPORT_TYPE_VALUES.contains(reportType)) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "reportType parameter has invalid value: " + reportType);
    }
    this.reportType = reportType;
  }

  @CamundaQueryParam(value = "periodUnit", converter = PeriodUnitConverter.class)
  public void setPeriodUnit(PeriodUnit periodUnit) {
    this.periodUnit = periodUnit;
  }

  public List<? extends ReportResult> executeReport(ProcessEngine engine) {
    T reportQuery = createNewReportQuery(engine);
    applyFilters(reportQuery);

    if (REPORT_TYPE_DURATION.equals(reportType)) {
      try {
        return reportQuery.duration(periodUnit);
      }
      catch (NotValidException e) {
        throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());
      }
    }
    else {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Unknown report type " + reportType);
    }

  }

  protected abstract T createNewReportQuery(ProcessEngine engine);

  protected abstract void applyFilters(T reportQuery);

}
