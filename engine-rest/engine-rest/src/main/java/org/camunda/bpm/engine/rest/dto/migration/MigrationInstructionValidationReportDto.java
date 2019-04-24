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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstructionValidationReport;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationInstructionValidationReportDto {

  protected MigrationInstructionDto instruction;
  protected List<String> failures;

  public MigrationInstructionDto getInstruction() {
    return instruction;
  }

  public void setInstruction(MigrationInstructionDto instruction) {
    this.instruction = instruction;
  }

  public List<String> getFailures() {
    return failures;
  }

  public void setFailures(List<String> failures) {
    this.failures = failures;
  }

  public static List<MigrationInstructionValidationReportDto> from(List<MigrationInstructionValidationReport> instructionReports) {
    List<MigrationInstructionValidationReportDto> dtos = new ArrayList<MigrationInstructionValidationReportDto>();
    for (MigrationInstructionValidationReport instructionReport : instructionReports) {
      dtos.add(MigrationInstructionValidationReportDto.from(instructionReport));
    }
    return dtos;
  }

  public static MigrationInstructionValidationReportDto from(MigrationInstructionValidationReport instructionReport) {
    MigrationInstructionValidationReportDto dto = new MigrationInstructionValidationReportDto();
    dto.setInstruction(MigrationInstructionDto.from(instructionReport.getMigrationInstruction()));
    dto.setFailures(instructionReport.getFailures());
    return dto;
  }

}
