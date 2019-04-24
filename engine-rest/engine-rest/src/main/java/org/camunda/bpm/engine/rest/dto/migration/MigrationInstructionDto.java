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

import org.camunda.bpm.engine.migration.MigrationInstruction;

public class MigrationInstructionDto {

  protected List<String> sourceActivityIds;
  protected List<String> targetActivityIds;
  protected Boolean updateEventTrigger;

  public List<String> getSourceActivityIds() {
    return sourceActivityIds;
  }

  public void setSourceActivityIds(List<String> sourceActivityIds) {
    this.sourceActivityIds = sourceActivityIds;
  }

  public List<String> getTargetActivityIds() {
    return targetActivityIds;
  }

  public void setTargetActivityIds(List<String> targetActivityIds) {
    this.targetActivityIds = targetActivityIds;
  }

  public void setUpdateEventTrigger(Boolean isUpdateEventTrigger) {
    this.updateEventTrigger = isUpdateEventTrigger;
  }

  public Boolean isUpdateEventTrigger() {
    return updateEventTrigger;
  }

  public static MigrationInstructionDto from(MigrationInstruction migrationInstruction) {
    if (migrationInstruction != null) {
      MigrationInstructionDto dto = new MigrationInstructionDto();

      dto.setSourceActivityIds(Collections.singletonList(migrationInstruction.getSourceActivityId()));
      dto.setTargetActivityIds(Collections.singletonList(migrationInstruction.getTargetActivityId()));
      dto.setUpdateEventTrigger(migrationInstruction.isUpdateEventTrigger());

      return dto;
    }
    else {
      return null;
    }
  }

}
