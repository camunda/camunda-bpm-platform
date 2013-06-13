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
package org.camunda.bpm.engine.runtime;

/**
 * <p>A transition instance represents an execution token that 
 * has just completed a transition (sequence flow in BPMN) and 
 * is now about to start an activity. The execution token has not 
 * yet entered the activity which is why the corresponding activity 
 * instance does not yet exist.</p>
 * 
 * <p>Transition instances are usually the result of 
 * asynchronous continuations.</p>
 * 
 * @author Daniel Meyer
 *
 */
public interface TransitionInstance extends ProcessElementInstance {
    
  /** returns the id of the target activity */
  String getTargetActivityId();
        
  /** returns the id of of the execution that is 
   * currently executing this transition instance */
  String getExecutionId();
  
}
