/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.dmn.engine;

import java.util.List;

/**
 * A decision of the DMN Engine.
 *
 * <p>
 * Decisions can be implement in different ways. To check if the decision is implemented
 * as a Decision Table see {@link #isDecisionTable()}.
 * </p>
 */
public interface DmnDecision {

  /**
   * The unique identifier of the element if exists.
   *
   * @return the identifier or null if not set
   */
  String getKey();

  /**
   * The human readable name of the element if exists.
   *
   * @return the name or null if not set
   */
  String getName();

  /**
   * Check if the decision is implemented as Decision Table.
   * 
   *  @return true if the decision is implement as Decision Table, false otherwise
   */
  boolean isDecisionTable();
  
  /**
   * Gets the list of required decisions. 
   * 
   * @return the list of required decisions.
   */
  List<DmnDecision> getRequiredDecisions(); 

}
