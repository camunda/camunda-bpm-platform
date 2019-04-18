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
package org.camunda.bpm.engine.impl.migration.instance.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.migration.instance.EmergingJobInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingJobInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingTimerJobInstance;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityInstanceJobHandler implements MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<JobEntity>> {

  @Override
  public void handle(MigratingInstanceParseContext parseContext, MigratingActivityInstance activityInstance, List<JobEntity> elements) {

    Map<String, TimerDeclarationImpl> sourceTimerDeclarationsInEventScope = getTimerDeclarationsByTriggeringActivity(activityInstance.getSourceScope());
    Map<String, TimerDeclarationImpl> targetTimerDeclarationsInEventScope = getTimerDeclarationsByTriggeringActivity(activityInstance.getTargetScope());

    for (JobEntity job : elements) {
      if (!isTimerJob(job)) {
        // skip non timer jobs
        continue;
      }

      MigrationInstruction migrationInstruction = parseContext.findSingleMigrationInstruction(job.getActivityId());
      ActivityImpl targetActivity = parseContext.getTargetActivity(migrationInstruction);

      if (targetActivity != null && activityInstance.migratesTo(targetActivity.getEventScope())) {
        // the timer job is migrated
        JobDefinitionEntity targetJobDefinitionEntity = parseContext.getTargetJobDefinition(targetActivity.getActivityId(), job.getJobHandlerType());

        TimerDeclarationImpl targetTimerDeclaration = targetTimerDeclarationsInEventScope.remove(targetActivity.getId());

        MigratingJobInstance migratingTimerJobInstance =
            new MigratingTimerJobInstance(
                job,
                targetJobDefinitionEntity,
                targetActivity,
                migrationInstruction.isUpdateEventTrigger(),
                targetTimerDeclaration);
        activityInstance.addMigratingDependentInstance(migratingTimerJobInstance);
        parseContext.submit(migratingTimerJobInstance);

      }
      else {
        // the timer job is removed
        MigratingJobInstance removingJobInstance = new MigratingTimerJobInstance(job);
        activityInstance.addRemovingDependentInstance(removingJobInstance);
        parseContext.submit(removingJobInstance);

      }

      parseContext.consume(job);
    }

    if (activityInstance.migrates()) {
      addEmergingTimerJobs(parseContext, activityInstance, sourceTimerDeclarationsInEventScope, targetTimerDeclarationsInEventScope);
    }
  }

  protected static boolean isTimerJob(JobEntity job) {
    return job != null && job.getType().equals(TimerEntity.TYPE);
  }

  protected void addEmergingTimerJobs(MigratingInstanceParseContext parseContext, MigratingActivityInstance activityInstance,
      Map<String, TimerDeclarationImpl> sourceTimerDeclarationsInEventScope, Map<String, TimerDeclarationImpl> targetTimerDeclarationsInEventScope) {
    for (TimerDeclarationImpl targetTimerDeclaration : targetTimerDeclarationsInEventScope.values()) {
      if(!isNonInterruptingTimerTriggeredAlready(parseContext, sourceTimerDeclarationsInEventScope, targetTimerDeclaration)) {
        activityInstance.addEmergingDependentInstance(new EmergingJobInstance(targetTimerDeclaration));
      }
    }
  }

  protected boolean isNonInterruptingTimerTriggeredAlready(MigratingInstanceParseContext parseContext,
      Map<String, TimerDeclarationImpl> sourceTimerDeclarationsInEventScope, TimerDeclarationImpl targetTimerDeclaration) {
    if (targetTimerDeclaration.isInterruptingTimer() || targetTimerDeclaration.getJobHandlerType() != TimerExecuteNestedActivityJobHandler.TYPE || sourceTimerDeclarationsInEventScope.values().size() == 0) {
      return false;
    }
    for (TimerDeclarationImpl sourceTimerDeclaration : sourceTimerDeclarationsInEventScope.values()) {
      MigrationInstruction migrationInstruction = parseContext.findSingleMigrationInstruction(sourceTimerDeclaration.getActivityId());
      ActivityImpl targetActivity = parseContext.getTargetActivity(migrationInstruction);

      if(targetActivity != null && targetTimerDeclaration.getActivityId().equals(targetActivity.getActivityId())) {
        return true;
      }
    }
    return false;
  }

  protected Map<String, TimerDeclarationImpl> getTimerDeclarationsByTriggeringActivity(ScopeImpl scope) {
    return new HashMap<String, TimerDeclarationImpl>(TimerDeclarationImpl.getDeclarationsForScope(scope));
  }

}
