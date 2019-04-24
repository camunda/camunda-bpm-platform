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

import java.util.List;

import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.migration.instance.MigratingAsyncJobInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingTransitionInstance;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class TransitionInstanceJobHandler implements MigratingDependentInstanceParseHandler<MigratingTransitionInstance, List<JobEntity>> {

  @Override
  public void handle(MigratingInstanceParseContext parseContext, MigratingTransitionInstance transitionInstance, List<JobEntity> elements) {

    for (JobEntity job : elements) {
      if (!isAsyncContinuation(job)) {
        continue;
      }

      ScopeImpl targetScope = transitionInstance.getTargetScope();
      if (targetScope != null) {
        JobDefinitionEntity targetJobDefinitionEntity = parseContext.getTargetJobDefinition(transitionInstance.getTargetScope().getId(), job.getJobHandlerType());

        MigratingAsyncJobInstance migratingJobInstance =
            new MigratingAsyncJobInstance(job, targetJobDefinitionEntity, transitionInstance.getTargetScope());

        transitionInstance.setDependentJobInstance(migratingJobInstance);
        parseContext.submit(migratingJobInstance);
      }

      parseContext.consume(job);
    }
  }

  protected static boolean isAsyncContinuation(JobEntity job) {
    return job != null && AsyncContinuationJobHandler.TYPE.equals(job.getJobHandlerType());
  }

}
