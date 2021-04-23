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
package org.camunda.bpm.engine.impl.migration.batch;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.migration.MigrationPlan;

public class MigrationBatchConfiguration extends BatchConfiguration {

  protected MigrationPlan migrationPlan;
  protected boolean isSkipCustomListeners;
  protected boolean isSkipIoMappings;

  public MigrationBatchConfiguration(List<String> ids,
                                     MigrationPlan migrationPlan,
                                     boolean isSkipCustomListeners,
                                     boolean isSkipIoMappings,
                                     String batchId) {
    this(ids, null, migrationPlan, isSkipCustomListeners, isSkipIoMappings, batchId);
  }

  public MigrationBatchConfiguration(List<String> ids,
                                     DeploymentMappings mappings,
                                     MigrationPlan migrationPlan,
                                     boolean isSkipCustomListeners,
                                     boolean isSkipIoMappings,
                                     String batchId) {
    super(ids, mappings);
    this.migrationPlan = migrationPlan;
    this.isSkipCustomListeners = isSkipCustomListeners;
    this.isSkipIoMappings = isSkipIoMappings;
    this.batchId = batchId;
  }

  public MigrationBatchConfiguration(List<String> ids,
                                     DeploymentMappings mappings,
                                     MigrationPlan migrationPlan,
                                     boolean isSkipCustomListeners,
                                     boolean isSkipIoMappings) {
    this(ids, mappings, migrationPlan, isSkipCustomListeners, isSkipIoMappings, null);
  }

  public MigrationPlan getMigrationPlan() {
    return migrationPlan;
  }

  public void setMigrationPlan(MigrationPlan migrationPlan) {
    this.migrationPlan = migrationPlan;
  }

  public boolean isSkipCustomListeners() {
    return isSkipCustomListeners;
  }

  public void setSkipCustomListeners(boolean isSkipCustomListeners) {
    this.isSkipCustomListeners = isSkipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return isSkipIoMappings;
  }

  public void setSkipIoMappings(boolean isSkipIoMappings) {
    this.isSkipIoMappings = isSkipIoMappings;
  }


}
