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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;

/**
 * 
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class AsyncContinuationJobHandler implements JobHandler {
  
  public final static String TYPE = "async-continuation";
  
  private Map<String, AtomicOperation> supportedOperations;

  public AsyncContinuationJobHandler() {
    supportedOperations = new HashMap<String, AtomicOperation>();
    supportedOperations.put(AtomicOperation.TRANSITION_CREATE_SCOPE.getCanonicalName(), AtomicOperation.TRANSITION_CREATE_SCOPE);
    supportedOperations.put(AtomicOperation.PROCESS_START.getCanonicalName(), AtomicOperation.PROCESS_START);
  }
  
  public String getType() {
    return TYPE;
  }

  public void execute(String configuration, ExecutionEntity execution, CommandContext commandContext) {
    AtomicOperation atomicOperation = findMatchingAtomicOperation(configuration);
    if (atomicOperation == null) {
      throw new ProcessEngineException("Cannot process job with configuration " + configuration);
    }
    
    commandContext
      .performOperation(atomicOperation, execution);
  }
  
  protected AtomicOperation findMatchingAtomicOperation(String configuration) {
    if (configuration == null) {
      // default operation for backwards compatibility
      return AtomicOperation.TRANSITION_CREATE_SCOPE;
    } else {
      return supportedOperations.get(configuration);
    }
  }
}
