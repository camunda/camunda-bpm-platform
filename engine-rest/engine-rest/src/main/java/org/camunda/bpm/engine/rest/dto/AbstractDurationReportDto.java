/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.camunda.bpm.engine.history.ReportResult;
import org.camunda.bpm.engine.query.PeriodUnit;
import org.camunda.bpm.engine.query.Report;
import org.camunda.bpm.engine.rest.dto.converter.PeriodUnitConverter;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 * @param <T>
 */
public abstract class AbstractDurationReportDto<T extends Report> extends AbstractReportDto<T> {


  protected PeriodUnit periodUnit;


  public AbstractDurationReportDto() {
  }

  public AbstractDurationReportDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }


  @CamundaQueryParam(value = "periodUnit", converter = PeriodUnitConverter.class)
  public void setPeriodUnit(PeriodUnit periodUnit) {
    this.periodUnit = periodUnit;
  }

  @Override
  protected List<? extends ReportResult> executeReportQuery(T report) {
    return report.duration(periodUnit);
  }

}
