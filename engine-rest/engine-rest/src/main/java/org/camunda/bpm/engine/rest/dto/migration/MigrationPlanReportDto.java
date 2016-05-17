/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.engine.migration.MigrationPlanValidationReport;

public class MigrationPlanReportDto {

  protected List<MigrationInstructionValidationReportDto> instructionReports;

  public List<MigrationInstructionValidationReportDto> getInstructionReports() {
    return instructionReports;
  }

  public void setInstructionReports(List<MigrationInstructionValidationReportDto> instructionReports) {
    this.instructionReports = instructionReports;
  }

  public static MigrationPlanReportDto form(MigrationPlanValidationReport validationReport) {
    MigrationPlanReportDto dto = new MigrationPlanReportDto();
    dto.setInstructionReports(MigrationInstructionValidationReportDto.from(validationReport.getInstructionReports()));
    return dto;
  }

  public static MigrationPlanReportDto emptyReport() {
    MigrationPlanReportDto dto = new MigrationPlanReportDto();
    dto.setInstructionReports(Collections.<MigrationInstructionValidationReportDto>emptyList());
    return dto;
  }

}
