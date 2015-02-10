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
package org.camunda.bpm.engine.impl.pvm.process;

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * Defines the start behavior for {@link ActivityImpl activities}.
 *
 * @author Daniel Meyer
 *
 */
public enum ActivityStartBehavior {

  /**
   * Default start behavior for an activity is to "do nothing special". Meaning:
   * the activity is executed by the execution which enters it.
   *
   * NOTE: Only activities contained in normal flow can have DEFALUT start behavior.
   */
  DEFAULT,

  /**
   * Used for activities which {@link PvmExecutionImpl#interrupt(String) interrupt}
   * their {@link PvmActivity#getFlowScope() flow scope}. Examples:
   * - Terminate end event
   * - Cancel end event
   *
   * NOTE: can only be used for activities contained in normal flow
   */
  INTERRUPT_FLOW_SCOPE,

  /**
   * Used for activities which are executed concurrently to activities
   * within the same {@link ActivityImpl#getFlowScope() flowScope}.
   */
  CONCURRENT_IN_FLOW_SCOPE,

  /**
   * Used for activities which {@link PvmExecutionImpl#interrupt(String) interrupt}
   * their {@link PvmActivity#getEventScope() event scope}
   *
   * NOTE: cannot only be used for activities contained in normal flow
   */
  INTERRUPT_EVENT_SCOPE,

  /**
   * Used for activities which cancel their {@link PvmActivity#getEventScope() event scope}.
   * - Boundary events with cancelActivity=true
   *
   * NOTE: cannot only be used for activities contained in normal flow
   */
  CANCEL_EVENT_SCOPE

}
