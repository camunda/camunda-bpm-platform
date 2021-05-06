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
package org.camunda.bpm.engine.migration;

import java.util.List;
import java.util.Map;

/**
 * Collects the migration validation reports for
 * all instructions and variables of the migration plan which contain failures.
 */
public interface MigrationPlanValidationReport {

  /**
   * @return the migration plan of the validation report
   */
  MigrationPlan getMigrationPlan();

  /**
   * @return {@code true} if either instruction or variable reports exist, {@code false} otherwise
   */
  boolean hasReports();

  /**
   * @return true if instructions reports exist, false otherwise
   */
  boolean hasInstructionReports();

  /**
   * @return {@code true} if variable reports exist, {@code false} otherwise
   */
  boolean hasVariableReports();

  /**
   * @return all instruction reports
   */
  List<MigrationInstructionValidationReport> getInstructionReports();

  /**
   * @return all variable reports
   */
  Map<String, MigrationVariableValidationReport> getVariableReports();

}
