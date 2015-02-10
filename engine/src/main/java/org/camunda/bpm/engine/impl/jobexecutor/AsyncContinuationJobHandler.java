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
package org.camunda.bpm.engine.impl.jobexecutor;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;

/**
 *
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class AsyncContinuationJobHandler implements JobHandler {

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

  public String getType() {
    return TYPE;
  }

  public void execute(String configuration, ExecutionEntity execution, CommandContext commandContext) {

    String operationName = null;
    String transitionId = null;

    if (configuration != null ) {

      if (configuration.contains("$")) {
        String[] configParts = configuration.split("\\$");
        if (configParts.length != 2) {
          throw new ProcessEngineException("Illegal async continuation job handler configuration: '" + configuration + "': exprecting two parts seperated by '$'.");
        }
        operationName = configParts[0];
        transitionId = configParts[1];

      } else {
        operationName = configuration;
      }

    }

    PvmAtomicOperation atomicOperation = findMatchingAtomicOperation(operationName);
    ensureNotNull("Cannot process job with configuration " + configuration, "atomicOperation", atomicOperation);

    // reset transition id.
    if (transitionId != null) {
      PvmActivity activity = execution.getActivity();
      TransitionImpl transition = (TransitionImpl) activity.findOutgoingTransition(transitionId);
      execution.setTransition(transition);
    }

    commandContext
      .performOperation(atomicOperation, execution);
  }

  protected PvmAtomicOperation findMatchingAtomicOperation(String configuration) {
    if (configuration == null) {
      // default operation for backwards compatibility
      return PvmAtomicOperation.TRANSITION_CREATE_SCOPE;
    } else {
      return supportedOperations.get(configuration);
    }
  }
}
