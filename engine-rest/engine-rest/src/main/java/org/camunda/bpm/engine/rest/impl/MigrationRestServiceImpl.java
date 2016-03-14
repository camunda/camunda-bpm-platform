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

package org.camunda.bpm.engine.rest.impl;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.rest.MigrationRestService;
import org.camunda.bpm.engine.rest.dto.migration.MigrationExecutionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MigrationRestServiceImpl extends AbstractRestProcessEngineAware implements MigrationRestService {

  public MigrationRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  public MigrationPlanDto generateMigrationPlan(MigrationPlanDto initialMigrationPlan) {
    RuntimeService runtimeService = processEngine.getRuntimeService();

    String sourceProcessDefinitionId = initialMigrationPlan.getSourceProcessDefinitionId();
    String targetProcessDefinitionId = initialMigrationPlan.getTargetProcessDefinitionId();

    try {
      MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
        .mapEqualActivities()
        .build();

      return MigrationPlanDto.from(migrationPlan);
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());
    }
  }

  public void executeMigrationPlan(MigrationExecutionDto migrationExecution) {
    MigrationPlanDto migrationPlanDto = migrationExecution.getMigrationPlan();
    List<String> processInstanceIds = migrationExecution.getProcessInstanceIds();

    try {
      MigrationPlan migrationPlan = MigrationPlanDto.toMigrationPlan(processEngine, migrationPlanDto);
      processEngine.getRuntimeService()
        .newMigration(migrationPlan).processInstanceIds(processInstanceIds).execute();
    }
    catch (MigrationPlanValidationException e) {
      throw e;
    }
    catch (MigratingProcessInstanceValidationException e) {
      throw e;
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());
    }
  }

}
