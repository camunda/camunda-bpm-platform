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


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public interface PvmActivity extends PvmScope {

  boolean isAsync();

  /**
   * Indicates whether this activity is interrupting. If true, the activity
   * will interrupt and cancel all other activities inside the same scope
   * before it is executed.
   *
   * @return true if this activity is interrupting. False otherwise.
   */
  boolean isCancelScope();

  /**
   * Indicates whether this activity is concurrent. If true, the activity
   * will be executed concurrently to other activities which are part of
   * the same scope.
   *
   * @return true if this activity is concurrent. False otherwise.
   */
  boolean isConcurrent();

  /** returns the scope of this activity. Must contain this activity but may or
   * may not be the direct parent. */
  PvmScope getScope();

  boolean isScope();

  @Deprecated
  boolean isExclusive();

  PvmScope getParent();

  List<PvmTransition> getIncomingTransitions();

  List<PvmTransition> getOutgoingTransitions();

  PvmTransition findOutgoingTransition(String transitionId);
}
