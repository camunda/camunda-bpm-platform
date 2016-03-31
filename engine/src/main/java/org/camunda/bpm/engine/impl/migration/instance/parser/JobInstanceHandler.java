/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.migration.instance.parser;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.migration.instance.EmergingJobInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingJobInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingTimerJobInstance;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobInstanceHandler implements MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<JobEntity>> {

  @Override
  public void handle(MigratingInstanceParseContext parseContext, MigratingActivityInstance activityInstance, List<JobEntity> elements) {

    List<String> migratingActivityIds = new ArrayList<String>();

    for (JobEntity job : elements) {
      if (!isTimerJob(job)) {
        // skip non timer jobs
        continue;
      }

      MigrationInstruction timerJobMigrationInstruction = parseContext.getInstructionFor(job.getActivityId());

      if (timerJobMigrationInstruction != null) {
        // the timer job is migrated
        ActivityImpl timerJobTargetActivity = parseContext.getTargetProcessDefinition().findActivity(timerJobMigrationInstruction.getTargetActivityId());
        migratingActivityIds.add(timerJobTargetActivity.getId());
        JobDefinitionEntity targetJobDefinitionEntity = parseContext.getTargetJobDefinition(timerJobTargetActivity.getActivityId(), job.getJobHandlerType());

        MigratingJobInstance migratingTimerJobInstance =
            new MigratingTimerJobInstance(job, targetJobDefinitionEntity, timerJobTargetActivity);
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

    List<TimerDeclarationImpl> emergingTimerDeclarations = new ArrayList<TimerDeclarationImpl>();

    if (activityInstance.getTargetScope() != null) {
      for (TimerDeclarationImpl timerDeclaration : TimerDeclarationImpl.getDeclarationsForScope(activityInstance.getTargetScope())) {
        if (!migratingActivityIds.contains(timerDeclaration.getActivityId())) {
          emergingTimerDeclarations.add(timerDeclaration);
        }
      }
    }

    for (TimerDeclarationImpl emergingDeclaration : emergingTimerDeclarations) {
      activityInstance.addEmergingDependentInstance(new EmergingJobInstance(emergingDeclaration));
    }
  }

  protected static boolean isTimerJob(JobEntity job) {
    return job != null && job.getType().equals(TimerEntity.TYPE);
  }

}
