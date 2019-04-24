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

import org.camunda.bpm.engine.migration.MigratingActivityInstanceValidationReport;

public class MigratingActivityInstanceValidationReportDto {

  protected MigrationInstructionDto migrationInstruction;
  protected String activityInstanceId;
  protected String sourceScopeId;
  protected List<String> failures;

  public MigrationInstructionDto getMigrationInstruction() {
    return migrationInstruction;
  }

  public void setMigrationInstruction(MigrationInstructionDto migrationInstruction) {
    this.migrationInstruction = migrationInstruction;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
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

  public static List<MigratingActivityInstanceValidationReportDto> from(List<MigratingActivityInstanceValidationReport> reports) {
    ArrayList<MigratingActivityInstanceValidationReportDto> dtos = new ArrayList<MigratingActivityInstanceValidationReportDto>();
    for (MigratingActivityInstanceValidationReport report : reports) {
      dtos.add(MigratingActivityInstanceValidationReportDto.from(report));
    }
    return dtos;
  }

  public static MigratingActivityInstanceValidationReportDto from(MigratingActivityInstanceValidationReport report) {
    MigratingActivityInstanceValidationReportDto dto = new MigratingActivityInstanceValidationReportDto();
    dto.setMigrationInstruction(MigrationInstructionDto.from(report.getMigrationInstruction()));
    dto.setActivityInstanceId(report.getActivityInstanceId());
    dto.setFailures(report.getFailures());
    dto.setSourceScopeId(report.getSourceScopeId());
    return dto;
  }

}
