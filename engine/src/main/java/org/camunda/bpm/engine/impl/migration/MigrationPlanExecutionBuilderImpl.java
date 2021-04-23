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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.migration.batch.MigrateProcessInstanceBatchCmd;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

public class MigrationPlanExecutionBuilderImpl implements MigrationPlanExecutionBuilder {

  protected CommandExecutor commandExecutor;
  protected MigrationPlan migrationPlan;
  protected List<String> processInstanceIds;
  protected ProcessInstanceQuery processInstanceQuery;
  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;

  public MigrationPlanExecutionBuilderImpl(CommandExecutor commandExecutor, MigrationPlan migrationPlan) {
    this.commandExecutor = commandExecutor;
    this.migrationPlan = migrationPlan;
  }

  public MigrationPlan getMigrationPlan() {
    return migrationPlan;
  }

  public MigrationPlanExecutionBuilder processInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  @Override
  public MigrationPlanExecutionBuilder processInstanceIds(String... processInstanceIds) {
    if (processInstanceIds == null) {
      this.processInstanceIds = Collections.emptyList();
    }
    else {
      this.processInstanceIds = Arrays.asList(processInstanceIds);
    }
    return this;
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public MigrationPlanExecutionBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
    return this;
  }

  public ProcessInstanceQuery getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public MigrationPlanExecutionBuilder skipCustomListeners() {
    this.skipCustomListeners = true;
    return this;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public MigrationPlanExecutionBuilder skipIoMappings() {
    this.skipIoMappings = true;
    return this;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

  public void execute() {
    commandExecutor.execute(new MigrateProcessInstanceCmd(this, false));
  }

  public Batch executeAsync() {
    return commandExecutor.execute(new MigrateProcessInstanceBatchCmd(this));
  }

}
