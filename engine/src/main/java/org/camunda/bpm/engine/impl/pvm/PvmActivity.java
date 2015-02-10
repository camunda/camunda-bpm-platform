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

package org.camunda.bpm.engine.impl.pvm;

import java.util.List;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior;


/**
 * Defines an activity insisde a process. Note that the term "activity" is meant to be
 * understood in a broader sense than in BPMN: everything inside a process which can have incoming
 * or outgoing sequence flows (transitions) are activities. Examples: events, tasks, gateways,
 * subprocesses ...
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public interface PvmActivity extends PvmScope {

  /**
   * The inner behavior of an activity. The inner behavior is the logic which is executed after
   * the {@link ExecutionListener#EVENTNAME_START start} listeners have been executed.
   *
   * In case the activity {@link #isScope() is scope}, a new execution will be created
   *
   * @return the inner behavior of the activity
   */
  ActivityBehavior getActivityBehavior();

  /**
   * The start behavior of an activity. The start behavior is executed before the
   * {@link ExecutionListener#EVENTNAME_START start} listeners of the activity are executed.
   *
   * @return the start behavior of an activity.
   */
  ActivityStartBehavior getActivityStartBehavior();

  /**
   * Finds and returns an outgoing sequence flow (transition) by it's id.
   * @param transitionId the id of the transition to find
   * @return the transition or null in case it cannot be found
   */
  PvmTransition findOutgoingTransition(String transitionId);

  /**
   * @return the list of outgoing sequence flows (transitions)
   */
  List<PvmTransition> getOutgoingTransitions();

  /**
   * @return the list of incoming sequence flows (transitions)
   */
  List<PvmTransition> getIncomingTransitions();

  /**
   * Indicates whether the activity is executed asynchronously.
   * This can be done <em>after</em> the {@link #getActivityStartBehavior() activity start behavior} and
   * <em>before</em> the {@link ExecutionListener#EVENTNAME_START start} listeners are invoked.
   *
   * @return true if the activity is executed asynchronously.
   */
  boolean isAsyncBefore();

  /**
   * Indicates whether execution after this execution should continue asynchronously.
   * This can be done <em>after</em> the {@link ExecutionListener#EVENTNAME_END end} listeners are invoked.
   * @return true if execution after this activity continues asynchronously.
   */
  boolean isAsyncAfter();
}
