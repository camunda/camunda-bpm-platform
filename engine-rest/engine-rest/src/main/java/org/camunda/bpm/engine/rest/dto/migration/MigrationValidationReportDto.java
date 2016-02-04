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
package org.camunda.bpm.engine.rest.dto.migration;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstructionInstanceValidationFailure;
import org.camunda.bpm.engine.migration.MigrationInstructionInstanceValidationReport;
import org.camunda.bpm.engine.migration.MigrationPlanValidationFailure;
import org.camunda.bpm.engine.migration.MigrationPlanValidationReport;
import org.camunda.bpm.engine.rest.dto.migration.MigrationValidationErrorDto.MigrationInstructionInstanceValidationErrorDto;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationValidationReportDto<T extends MigrationValidationErrorDto> {

  protected List<T> validationErrors;

  public List<T> getValidationErrors() {
    return validationErrors;
  }

  public void setValidationErrors(List<T> validationErrors) {
    this.validationErrors = validationErrors;
  }

  public static class MigrationPlanValidationReportDto extends MigrationValidationReportDto<MigrationValidationErrorDto> {

    public static MigrationPlanValidationReportDto from(MigrationPlanValidationReport report) {
      MigrationPlanValidationReportDto dto = new MigrationPlanValidationReportDto();
      dto.validationErrors = new ArrayList<MigrationValidationErrorDto>();

      for(MigrationPlanValidationFailure failure : report.getValidationFailures()) {
        dto.validationErrors.add(MigrationValidationErrorDto.from(failure));
      }

      return dto;
    }
  }

  public static class MigrationInstructionInstanceValidationReportDto extends MigrationValidationReportDto<MigrationInstructionInstanceValidationErrorDto> {

    protected String processInstanceId;

    public String getProcessInstanceId() {
      return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
      this.processInstanceId = processInstanceId;
    }

    public static MigrationInstructionInstanceValidationReportDto from(MigrationInstructionInstanceValidationReport report) {
      MigrationInstructionInstanceValidationReportDto dto = new MigrationInstructionInstanceValidationReportDto();
      dto.validationErrors = new ArrayList<MigrationInstructionInstanceValidationErrorDto>();
      dto.processInstanceId = report.getProcessInstanceId();

      for(MigrationInstructionInstanceValidationFailure failure : report.getValidationFailures()) {
        dto.validationErrors.add(MigrationInstructionInstanceValidationErrorDto.from(failure));
      }

      return dto;
    }

  }
}
