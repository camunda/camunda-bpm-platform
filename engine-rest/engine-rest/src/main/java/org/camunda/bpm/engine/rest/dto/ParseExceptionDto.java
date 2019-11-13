/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.rest.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.Problem;
import org.camunda.bpm.engine.ResourceReport;

/**
 * Dto for {@link ParseException}
 *
 * The exception contains a list of errors and warning that occurred during
 * parsing.
 */
public class ParseExceptionDto extends ExceptionDto {

  protected Map<String, ResourceReportDto> details = new HashMap<>();

  // transformer /////////////////////////////

  public static ParseExceptionDto fromException(ParseException exception) {
    ParseExceptionDto dto = new ParseExceptionDto();

    dto.setType(ParseException.class.getSimpleName());
    dto.setMessage(exception.getMessage());

    for (ResourceReport report : exception.getResorceReports()) {
      List<ProblemDto> errorDtos = new ArrayList<>();
      for (Problem error : report.getErrors()) {
        errorDtos.add(ProblemDto.fromProblem(error));
      }

      List<ProblemDto> warningDtos = new ArrayList<>();
      for (Problem warning : report.getWarnings()) {
        warningDtos.add(ProblemDto.fromProblem(warning));
      }
      ResourceReportDto resourceReportDto = new ResourceReportDto(errorDtos, warningDtos);
      dto.details.put(report.getResourceName(), resourceReportDto);
    }

    return dto;
  }

  // getter / setters ////////////////////////

  public Map<String, ResourceReportDto> getDetails() {
    return details;
  }

  public void setDetails(Map<String, ResourceReportDto> details) {
    this.details = details;
  }
}
