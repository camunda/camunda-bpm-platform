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
package org.camunda.bpm.engine.impl.migration.validation.instance;

import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler.AsyncContinuationConfiguration;
import org.camunda.bpm.engine.impl.migration.instance.MigratingJobInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingTransitionInstance;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

public class AsyncAfterMigrationValidator implements MigratingTransitionInstanceValidator {

  @Override
  public void validate(MigratingTransitionInstance migratingInstance, MigratingProcessInstance migratingProcessInstance,
      MigratingTransitionInstanceValidationReportImpl instanceReport) {
    ActivityImpl targetActivity = (ActivityImpl) migratingInstance.getTargetScope();

    if (targetActivity != null && migratingInstance.isAsyncAfter()) {
      MigratingJobInstance jobInstance = migratingInstance.getJobInstance();
      AsyncContinuationConfiguration config = (AsyncContinuationConfiguration) jobInstance.getJobEntity().getJobHandlerConfiguration();
      String sourceTransitionId = config.getTransitionId();

      if (targetActivity.getOutgoingTransitions().size() > 1) {
        if (sourceTransitionId == null) {
          instanceReport.addFailure("Transition instance is assigned to no sequence flow"
              + " and target activity has more than one outgoing sequence flow");
        }
        else {
          TransitionImpl matchingOutgoingTransition = targetActivity.findOutgoingTransition(sourceTransitionId);
          if (matchingOutgoingTransition == null) {
            instanceReport.addFailure("Transition instance is assigned to a sequence flow"
              + " that cannot be matched in the target activity");
          }
        }
      }
    }

  }

}
