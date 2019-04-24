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
package org.camunda.bpm.engine.impl.jobexecutor;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler.AsyncContinuationConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.LegacyBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;

/**
 *
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class AsyncContinuationJobHandler implements JobHandler<AsyncContinuationConfiguration> {

  public final static String TYPE = "async-continuation";

  private Map<String, PvmAtomicOperation> supportedOperations;

  public AsyncContinuationJobHandler() {
    supportedOperations = new HashMap<String, PvmAtomicOperation>();
    // async before activity
    supportedOperations.put(PvmAtomicOperation.TRANSITION_CREATE_SCOPE.getCanonicalName(), PvmAtomicOperation.TRANSITION_CREATE_SCOPE);
    supportedOperations.put(PvmAtomicOperation.ACTIVITY_START_CREATE_SCOPE.getCanonicalName(), PvmAtomicOperation.ACTIVITY_START_CREATE_SCOPE);
    // async before start event
    supportedOperations.put(PvmAtomicOperation.PROCESS_START.getCanonicalName(), PvmAtomicOperation.PROCESS_START);

    // async after activity depending if an outgoing sequence flow exists
    supportedOperations.put(PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE.getCanonicalName(), PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE);
    supportedOperations.put(PvmAtomicOperation.ACTIVITY_END.getCanonicalName(), PvmAtomicOperation.ACTIVITY_END);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void execute(AsyncContinuationConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {

    LegacyBehavior.repairMultiInstanceAsyncJob(execution);

    PvmAtomicOperation atomicOperation = findMatchingAtomicOperation(configuration.getAtomicOperation());
    ensureNotNull("Cannot process job with configuration " + configuration, "atomicOperation", atomicOperation);

    // reset transition id.
    String transitionId = configuration.getTransitionId();
    if (transitionId != null) {
      PvmActivity activity = execution.getActivity();
      TransitionImpl transition = (TransitionImpl) activity.findOutgoingTransition(transitionId);
      execution.setTransition(transition);
    }

    Context.getCommandInvocationContext()
      .performOperation(atomicOperation, execution);
  }

  public PvmAtomicOperation findMatchingAtomicOperation(String operationName) {
    if (operationName == null) {
      // default operation for backwards compatibility
      return PvmAtomicOperation.TRANSITION_CREATE_SCOPE;
    } else {
      return supportedOperations.get(operationName);
    }
  }

  protected boolean isSupported(PvmAtomicOperation atomicOperation) {
    return supportedOperations.containsKey(atomicOperation.getCanonicalName());
  }

  @Override
  public AsyncContinuationConfiguration newConfiguration(String canonicalString) {
    String[] configParts = tokenizeJobConfiguration(canonicalString);

    AsyncContinuationConfiguration configuration = new AsyncContinuationConfiguration();

    configuration.setAtomicOperation(configParts[0]);
    configuration.setTransitionId(configParts[1]);

    return configuration;
  }

  /**
   * @return an array of length two with the following contents:
   * <ul><li>First element: pvm atomic operation name
   * <li>Second element: transition id (may be null)
   */
  protected String[] tokenizeJobConfiguration(String jobConfiguration) {

    String[] configuration = new String[2];

    if (jobConfiguration != null ) {
      String[] configParts = jobConfiguration.split("\\$");
      if (configuration.length > 2) {
        throw new ProcessEngineException("Illegal async continuation job handler configuration: '" + jobConfiguration + "': exprecting one part or two parts seperated by '$'.");
      }
      configuration[0] = configParts[0];
      if (configParts.length == 2) {
        configuration[1] = configParts[1];
      }
    }

    return configuration;
  }

  public static class AsyncContinuationConfiguration implements JobHandlerConfiguration {

    protected String atomicOperation;
    protected String transitionId;

    public String getAtomicOperation() {
      return atomicOperation;
    }

    public void setAtomicOperation(String atomicOperation) {
      this.atomicOperation = atomicOperation;
    }

    public String getTransitionId() {
      return transitionId;
    }

    public void setTransitionId(String transitionId) {
      this.transitionId = transitionId;
    }

    @Override
    public String toCanonicalString() {
      String configuration = atomicOperation;

      if(transitionId != null) {
        // store id of selected transition in case this is async after.
        // id is not serialized with the execution -> we need to remember it as
        // job handler configuration.
        configuration += "$" + transitionId;
      }

      return configuration;
    }

  }

  public void onDelete(AsyncContinuationConfiguration configuration, JobEntity jobEntity) {
    // do nothing
  }
}
