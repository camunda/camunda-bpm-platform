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

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler.AsyncContinuationConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;
import org.camunda.bpm.engine.management.JobDefinition;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingAsyncJobInstance extends MigratingJobInstance {

  public MigratingAsyncJobInstance(JobEntity jobEntity, JobDefinitionEntity jobDefinitionEntity, ScopeImpl targetScope) {
    super(jobEntity, jobDefinitionEntity, targetScope);
  }

  @Override
  protected void migrateJobHandlerConfiguration() {
    AsyncContinuationConfiguration configuration = (AsyncContinuationConfiguration) jobEntity.getJobHandlerConfiguration();

    if (isAsyncAfter()) {
      updateAsyncAfterTargetConfiguration(configuration);
    }
    else {
      updateAsyncBeforeTargetConfiguration();
    }
  }


  public boolean isAsyncAfter() {
    JobDefinition jobDefinition = jobEntity.getJobDefinition();
    return MessageJobDeclaration.ASYNC_AFTER.equals(jobDefinition.getJobConfiguration());
  }

  public boolean isAsyncBefore() {
    return !isAsyncAfter();
  }

  protected void updateAsyncBeforeTargetConfiguration() {

    AsyncContinuationConfiguration targetConfiguration = new AsyncContinuationConfiguration();
    AsyncContinuationConfiguration currentConfiguration = (AsyncContinuationConfiguration) jobEntity.getJobHandlerConfiguration();

    if (PvmAtomicOperation.PROCESS_START.getCanonicalName().equals(currentConfiguration.getAtomicOperation())) {
      // process start always stays process start
      targetConfiguration.setAtomicOperation(PvmAtomicOperation.PROCESS_START.getCanonicalName());
    }
    else {
      if (((ActivityImpl) targetScope).getIncomingTransitions().isEmpty()) {
        targetConfiguration.setAtomicOperation(PvmAtomicOperation.ACTIVITY_START_CREATE_SCOPE.getCanonicalName());
      }
      else {
        targetConfiguration.setAtomicOperation(PvmAtomicOperation.TRANSITION_CREATE_SCOPE.getCanonicalName());
      }
    }


    jobEntity.setJobHandlerConfiguration(targetConfiguration);
  }

  protected void updateAsyncAfterTargetConfiguration(AsyncContinuationConfiguration currentConfiguration) {
    ActivityImpl targetActivity = (ActivityImpl) targetScope;
    List<PvmTransition> outgoingTransitions = targetActivity.getOutgoingTransitions();

    AsyncContinuationConfiguration targetConfiguration = new AsyncContinuationConfiguration();

    if (outgoingTransitions.isEmpty()) {
      targetConfiguration.setAtomicOperation(PvmAtomicOperation.ACTIVITY_END.getCanonicalName());
    }
    else {
      targetConfiguration.setAtomicOperation(PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE.getCanonicalName());

      if (outgoingTransitions.size() == 1) {
        targetConfiguration.setTransitionId(outgoingTransitions.get(0).getId());
      }
      else {
        TransitionImpl matchingTargetTransition = null;
        String currentTransitionId = currentConfiguration.getTransitionId();
        if (currentTransitionId != null) {
          matchingTargetTransition = targetActivity.findOutgoingTransition(currentTransitionId);
        }

        if (matchingTargetTransition != null) {
          targetConfiguration.setTransitionId(matchingTargetTransition.getId());
        }
        else {
          // should not happen since it is avoided by validation
          throw new ProcessEngineException("Cannot determine matching outgoing sequence flow");
        }
      }
    }

    jobEntity.setJobHandlerConfiguration(targetConfiguration);
  }

}
