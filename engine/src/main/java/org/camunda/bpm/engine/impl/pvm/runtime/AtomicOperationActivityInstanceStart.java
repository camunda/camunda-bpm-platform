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

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;




/**
 * @author Daniel Meyer
 * 
 */
public abstract class AtomicOperationActivityInstanceStart extends AbstractEventAtomicOperation {

  @Override
  protected void eventNotificationsStarted(InterpretableExecution execution) {
    execution.enterActivityInstance();
  }
  
  @Override
  protected void eventNotificationsCompleted(InterpretableExecution execution) {
    
    // hack around execution tree structure not being in sync with activity instance concept:
    // if we start a scope activity, remember current activity instance in parent
    ActivityExecution parent = execution.getParent();
    if(parent != null && execution.isScope() && ((ActivityImpl)execution.getActivity()).isScope()) {
      parent.setActivityInstanceId(execution.getActivityInstanceId());     
    }    
    
  }

}
