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
package org.camunda.bpm.engine.rest.dto.migration;

import org.camunda.bpm.engine.migration.MigrationVariableValidationReport;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrationVariableValidationReportDto extends VariableValueDto {

  protected List<String> failures;

  public List<String> getFailures() {
    return failures;
  }

  public void setFailures(List<String> failures) {
    this.failures = failures;
  }

  public static Map<String, MigrationVariableValidationReportDto> from(Map<String, MigrationVariableValidationReport> variableReports) {
    Map<String, MigrationVariableValidationReportDto> dtos = new HashMap<>();
    variableReports.forEach((name, report) ->
        dtos.put(name, MigrationVariableValidationReportDto.from(report)));
    return dtos;
  }

  public static MigrationVariableValidationReportDto from(MigrationVariableValidationReport variableReport) {
    MigrationVariableValidationReportDto dto = new MigrationVariableValidationReportDto();
    VariableValueDto valueDto = VariableValueDto.fromTypedValue(variableReport.getTypedValue());
    dto.setType(valueDto.getType());
    dto.setValue(valueDto.getValue());
    dto.setValueInfo(valueDto.getValueInfo());
    dto.setFailures(variableReport.getFailures());
    return dto;
  }

}
