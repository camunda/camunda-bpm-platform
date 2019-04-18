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
package org.camunda.bpm.engine.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationExecutionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanGenerationDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanReportDto;

public interface MigrationRestService {

  String PATH = "/migration";

  @POST
  @Path("/generate")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  MigrationPlanDto generateMigrationPlan(MigrationPlanGenerationDto generationDto);

  @POST
  @Path("/validate")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  MigrationPlanReportDto validateMigrationPlan(MigrationPlanDto migrationPlanDto);

  @POST
  @Path("/execute")
  @Consumes(MediaType.APPLICATION_JSON)
  void executeMigrationPlan(MigrationExecutionDto migrationPlan);

  @POST
  @Path("/executeAsync")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  BatchDto executeMigrationPlanAsync(MigrationExecutionDto migrationPlan);

}
