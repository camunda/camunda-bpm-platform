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
package org.camunda.bpm.engine.impl.migration.instance;

import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventJobHandler.TimerJobConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingTimerJobInstance extends MigratingJobInstance {

  protected ScopeImpl timerTriggerTargetScope;
  protected TimerDeclarationImpl targetJobDeclaration;
  protected boolean updateEvent;

  public MigratingTimerJobInstance(JobEntity jobEntity) {
    super(jobEntity);
  }

  public MigratingTimerJobInstance(JobEntity jobEntity,
      JobDefinitionEntity jobDefinitionEntity,
      ScopeImpl targetScope,
      boolean updateEvent,
      TimerDeclarationImpl targetTimerDeclaration) {
    super(jobEntity, jobDefinitionEntity, targetScope);
    timerTriggerTargetScope = determineTimerTriggerTargetScope(jobEntity, targetScope);
    this.updateEvent = updateEvent;
    this.targetJobDeclaration = targetTimerDeclaration;
  }

  protected ScopeImpl determineTimerTriggerTargetScope(JobEntity jobEntity, ScopeImpl targetScope) {
    if (TimerStartEventSubprocessJobHandler.TYPE.equals(jobEntity.getJobHandlerType())) {
      // for event subprocess start jobs, the job handler configuration references the subprocess while
      // the job references the start event
      return targetScope.getFlowScope();
    }
    else {
      return targetScope;
    }
  }

  @Override
  protected void migrateJobHandlerConfiguration() {
    TimerJobConfiguration configuration = (TimerJobConfiguration) jobEntity.getJobHandlerConfiguration();
    configuration.setTimerElementKey(timerTriggerTargetScope.getId());
    jobEntity.setJobHandlerConfiguration(configuration);

    if (updateEvent) {
      targetJobDeclaration.updateJob((TimerEntity) jobEntity);
    }
  }

}
