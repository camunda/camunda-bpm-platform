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

import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstructionInstanceValidationFailure;
import org.camunda.bpm.engine.migration.MigrationPlanValidationFailure;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationValidationErrorDto {

  protected String message;
  protected MigrationInstructionDto instruction;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public MigrationInstructionDto getInstruction() {
    return instruction;
  }

  public void setInstruction(MigrationInstructionDto instruction) {
    this.instruction = instruction;
  }

  public static MigrationValidationErrorDto from(MigrationPlanValidationFailure validationFailure) {
    MigrationValidationErrorDto dto = new MigrationValidationErrorDto();
    dto.instruction = MigrationInstructionDto.fromMigrationInstruction(validationFailure.getMigrationInstruction());
    dto.message = validationFailure.getErrorMessage();
    return dto;
  }

  public static class MigrationInstructionInstanceValidationErrorDto extends MigrationValidationErrorDto {

    protected List<String> activityInstanceIds;

    public List<String> getActivityInstanceIds() {
      return activityInstanceIds;
    }
    public void setActivityInstanceIds(List<String> activityInstanceIds) {
      this.activityInstanceIds = activityInstanceIds;
    }

    public static MigrationInstructionInstanceValidationErrorDto from(MigrationInstructionInstanceValidationFailure validationFailure) {
      MigrationInstructionInstanceValidationErrorDto dto = new MigrationInstructionInstanceValidationErrorDto();
      dto.instruction = MigrationInstructionDto.fromMigrationInstruction(validationFailure.getMigrationInstruction());
      dto.message = validationFailure.getErrorMessage();
      dto.activityInstanceIds = validationFailure.getActivityInstanceIds();
      return dto;
    }
  }

}
