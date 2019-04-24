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
package org.camunda.bpm.engine.impl.migration;

import java.util.List;

import org.camunda.bpm.engine.impl.migration.validation.activity.MigrationActivityValidator;
import org.camunda.bpm.engine.impl.migration.validation.instruction.MigrationInstructionValidator;
import org.camunda.bpm.engine.impl.migration.validation.instruction.ValidatingMigrationInstructions;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;

/**
 * Generates all migration instructions which represent a direct one
 * to one mapping of mapped entities in two process definitions. See
 * also {@link MigrationActivityMatcher}.
 *
 * @author Thorben Lindhauer
 *
 */
public interface MigrationInstructionGenerator {

  /**
   * Sets the list of migration activity validators which validate that a activity
   * is a candidate for the migration.
   *
   * @param migrationActivityValidators the list of validators to check
   * @return this generator instance
   */
  MigrationInstructionGenerator migrationActivityValidators(List<MigrationActivityValidator> migrationActivityValidators);

  /**
   * Sets the list of migration instruction validators currently used by the process engine.
   * Implementations may use these to restrict the search space.
   *
   * @return this
   */
  MigrationInstructionGenerator migrationInstructionValidators(List<MigrationInstructionValidator> migrationInstructionValidators);

  /**
   * Generate all migration instructions for mapped activities between two process definitions. A activity can be mapped
   * if the {@link MigrationActivityMatcher} matches it with an activity from the target process definition.
   *
   * @param sourceProcessDefinition the source process definition
   * @param targetProcessDefinition the target process definiton
   * @return the list of generated instructions
   */
  ValidatingMigrationInstructions generate(ProcessDefinitionImpl sourceProcessDefinition,
                                           ProcessDefinitionImpl targetProcessDefinition,
                                           boolean updateEventTriggers);

}
