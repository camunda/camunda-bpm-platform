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

package org.camunda.bpm.engine.impl.pvm.runtime;

import java.util.List;

import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class AtomicOperationProcessStart extends AbstractEventAtomicOperation {

  @Override
  public boolean isAsync(InterpretableExecution execution) {
    ProcessInstanceStartContext startContext = execution.getProcessInstanceStartContext();
    if (startContext != null && startContext.isAsync()) {
      return true;
    }
    return false;
  }
  
  @Override
  protected ScopeImpl getScope(InterpretableExecution execution) {
    return execution.getProcessDefinition();
  }

  @Override
  protected String getEventName() {
    return org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START;
  }

  @Override
  protected void eventNotificationsCompleted(InterpretableExecution execution) {
    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    ProcessInstanceStartContext processInstanceStartContext = execution.getProcessInstanceStartContext();
    List<ActivityImpl> initialActivityStack = processDefinition.getInitialActivityStack(processInstanceStartContext.getInitial());  
    execution.setActivity(initialActivityStack.get(0));
    execution.performOperation(PROCESS_START_INITIAL);
  }

  @Override
  public String getCanonicalName() {
    return "process-start";
  }
  
}
