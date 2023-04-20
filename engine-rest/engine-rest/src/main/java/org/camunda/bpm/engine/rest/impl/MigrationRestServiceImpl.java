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
package org.camunda.bpm.engine.rest.impl;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.migration.MigrationInstructionsBuilder;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.rest.MigrationRestService;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationExecutionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanGenerationDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanReportDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.camunda.bpm.engine.rest.dto.VariableValueDto.toMap;

public class MigrationRestServiceImpl extends AbstractRestProcessEngineAware implements MigrationRestService {

  public MigrationRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  public MigrationPlanDto generateMigrationPlan(MigrationPlanGenerationDto generationDto) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();

    String sourceProcessDefinitionId = generationDto.getSourceProcessDefinitionId();
    String targetProcessDefinitionId = generationDto.getTargetProcessDefinitionId();

    try {
      MigrationInstructionsBuilder instructionsBuilder = runtimeService.createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
        .mapEqualActivities();

      if (generationDto.isUpdateEventTriggers()) {
        instructionsBuilder = instructionsBuilder.updateEventTriggers();
      }

      Map<String, VariableValueDto> variableDtos = generationDto.getVariables();
      if (variableDtos != null) {
        instructionsBuilder.setVariables(toMap(variableDtos, getProcessEngine(), objectMapper));
      }

      MigrationPlan migrationPlan = instructionsBuilder.build();

      return MigrationPlanDto.from(migrationPlan);
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());
    }
  }

  public MigrationPlanReportDto validateMigrationPlan(MigrationPlanDto migrationPlanDto) {
    try {
      createMigrationPlan(migrationPlanDto);
      // return an empty report if not errors are found
      return MigrationPlanReportDto.emptyReport();
    }
    catch (MigrationPlanValidationException e) {
     return MigrationPlanReportDto.form(e.getValidationReport());
    }
  }

  public void executeMigrationPlan(MigrationExecutionDto migrationExecution) {
    createMigrationPlanExecutionBuilder(migrationExecution).execute();
  }

  public BatchDto executeMigrationPlanAsync(MigrationExecutionDto migrationExecution) {
    Batch batch = createMigrationPlanExecutionBuilder(migrationExecution).executeAsync();
    return BatchDto.fromBatch(batch);
  }

  protected MigrationPlanExecutionBuilder createMigrationPlanExecutionBuilder(MigrationExecutionDto migrationExecution) {
    MigrationPlan migrationPlan = createMigrationPlan(migrationExecution.getMigrationPlan());
    List<String> processInstanceIds = migrationExecution.getProcessInstanceIds();

    MigrationPlanExecutionBuilder executionBuilder = getProcessEngine().getRuntimeService()
      .newMigration(migrationPlan).processInstanceIds(processInstanceIds);

    ProcessInstanceQueryDto processInstanceQueryDto = migrationExecution.getProcessInstanceQuery();
    if (processInstanceQueryDto != null) {
      ProcessInstanceQuery processInstanceQuery = processInstanceQueryDto.toQuery(getProcessEngine());
      executionBuilder.processInstanceQuery(processInstanceQuery);
    }

    if (migrationExecution.isSkipCustomListeners()) {
      executionBuilder.skipCustomListeners();
    }

    if (migrationExecution.isSkipIoMappings()) {
      executionBuilder.skipIoMappings();
    }

    return executionBuilder;
  }

  protected MigrationPlan createMigrationPlan(MigrationPlanDto migrationPlanDto) {
    try {
      return MigrationPlanDto.toMigrationPlan(getProcessEngine(), objectMapper, migrationPlanDto);
    }
    catch (MigrationPlanValidationException e) {
      throw e;
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());
    }
  }

}
