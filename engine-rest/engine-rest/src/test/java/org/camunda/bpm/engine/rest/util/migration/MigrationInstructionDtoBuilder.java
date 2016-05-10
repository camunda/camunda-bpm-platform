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

package org.camunda.bpm.engine.rest.util.migration;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.rest.dto.migration.MigrationInstructionDto;

public class MigrationInstructionDtoBuilder {

  protected final MigrationInstructionDto migrationInstructionDto;

  public MigrationInstructionDtoBuilder() {
    migrationInstructionDto = new MigrationInstructionDto();
  }

  public MigrationInstructionDtoBuilder migrate(String sourceActivityId, String targetActivityId) {
    return migrate(Collections.singletonList(sourceActivityId), Collections.singletonList(targetActivityId), null);
  }

  public MigrationInstructionDtoBuilder migrate(String sourceActivityId, String targetActivityId, Boolean updateEventTrigger) {
    return migrate(Collections.singletonList(sourceActivityId), Collections.singletonList(targetActivityId), updateEventTrigger);
  }

  public MigrationInstructionDtoBuilder migrate(List<String> sourceActivityId, List<String> targetActivityId, Boolean updateEventTrigger) {
    migrationInstructionDto.setSourceActivityIds(sourceActivityId);
    migrationInstructionDto.setTargetActivityIds(targetActivityId);
    migrationInstructionDto.setUpdateEventTrigger(updateEventTrigger);

    return this;
  }

  public MigrationInstructionDto build() {
    return migrationInstructionDto;
  }

}
