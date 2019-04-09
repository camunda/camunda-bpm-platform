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
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingTransitionInstance;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;

/**
 * @author Thorben Lindhauer
 *
 */
public class AsyncProcessStartMigrationValidator implements MigratingTransitionInstanceValidator {

  @Override
  public void validate(MigratingTransitionInstance migratingInstance, MigratingProcessInstance migratingProcessInstance,
      MigratingTransitionInstanceValidationReportImpl instanceReport) {

    ActivityImpl targetActivity = (ActivityImpl) migratingInstance.getTargetScope();

    if (targetActivity != null) {
      if (isProcessStartJob(migratingInstance.getJobInstance().getJobEntity()) && !isTopLevelActivity(targetActivity)) {
        instanceReport.addFailure("A transition instance that instantiates the process can only be migrated to a process-level flow node");
      }
    }
  }

  protected boolean isProcessStartJob(JobEntity job) {
    AsyncContinuationConfiguration configuration = (AsyncContinuationConfiguration) job.getJobHandlerConfiguration();
    return PvmAtomicOperation.PROCESS_START.getCanonicalName().equals(configuration.getAtomicOperation());
  }

  protected boolean isTopLevelActivity(ActivityImpl activity) {
    return activity.getFlowScope() == activity.getProcessDefinition();
  }
}
