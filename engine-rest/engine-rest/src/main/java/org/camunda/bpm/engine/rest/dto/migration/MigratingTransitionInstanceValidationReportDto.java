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

import org.camunda.bpm.engine.migration.MigratingTransitionInstanceValidationReport;

public class MigratingTransitionInstanceValidationReportDto {

  protected MigrationInstructionDto migrationInstruction;
  protected String transitionInstanceId;
  protected String sourceScopeId;
  protected List<String> failures;

  public MigrationInstructionDto getMigrationInstruction() {
    return migrationInstruction;
  }

  public void setMigrationInstruction(MigrationInstructionDto migrationInstruction) {
    this.migrationInstruction = migrationInstruction;
  }

  public String getTransitionInstanceId() {
    return transitionInstanceId;
  }

  public void setTransitionInstanceId(String transitionInstanceId) {
    this.transitionInstanceId = transitionInstanceId;
  }

  public List<String> getFailures() {
    return failures;
  }

  public void setFailures(List<String> failures) {
    this.failures = failures;
  }

  public String getSourceScopeId() {
    return sourceScopeId;
  }

  public void setSourceScopeId(String sourceScopeId) {
    this.sourceScopeId = sourceScopeId;
  }

  public static List<MigratingTransitionInstanceValidationReportDto> from(List<MigratingTransitionInstanceValidationReport> reports) {
    ArrayList<MigratingTransitionInstanceValidationReportDto> dtos = new ArrayList<MigratingTransitionInstanceValidationReportDto>();
    for (MigratingTransitionInstanceValidationReport report : reports) {
      dtos.add(MigratingTransitionInstanceValidationReportDto.from(report));
    }
    return dtos;
  }

  public static MigratingTransitionInstanceValidationReportDto from(MigratingTransitionInstanceValidationReport report) {
    MigratingTransitionInstanceValidationReportDto dto = new MigratingTransitionInstanceValidationReportDto();
    dto.setMigrationInstruction(MigrationInstructionDto.from(report.getMigrationInstruction()));
    dto.setTransitionInstanceId(report.getTransitionInstanceId());
    dto.setFailures(report.getFailures());
    dto.setSourceScopeId(report.getSourceScopeId());
    return dto;
  }

}
