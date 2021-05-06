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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.migration.MigrationPlanValidationReport;

public class MigrationPlanReportDto {

  protected List<MigrationInstructionValidationReportDto> instructionReports;
  protected Map<String, MigrationVariableValidationReportDto> variableReports;

  public List<MigrationInstructionValidationReportDto> getInstructionReports() {
    return instructionReports;
  }

  public void setInstructionReports(List<MigrationInstructionValidationReportDto> instructionReports) {
    this.instructionReports = instructionReports;
  }

  public Map<String, MigrationVariableValidationReportDto> getVariableReports() {
    return variableReports;
  }

  public void setVariableReports(Map<String, MigrationVariableValidationReportDto> variableReports) {
    this.variableReports = variableReports;
  }

  public static MigrationPlanReportDto form(MigrationPlanValidationReport validationReport) {
    MigrationPlanReportDto dto = new MigrationPlanReportDto();
    dto.setInstructionReports(MigrationInstructionValidationReportDto.from(validationReport.getInstructionReports()));
    dto.setVariableReports(MigrationVariableValidationReportDto.from(validationReport.getVariableReports()));
    return dto;
  }

  public static MigrationPlanReportDto emptyReport() {
    MigrationPlanReportDto dto = new MigrationPlanReportDto();
    dto.setInstructionReports(Collections.emptyList());
    dto.setVariableReports(Collections.emptyMap());
    return dto;
  }

}
