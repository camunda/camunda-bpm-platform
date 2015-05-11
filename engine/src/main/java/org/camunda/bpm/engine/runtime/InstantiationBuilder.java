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

import org.camunda.bpm.engine.ProcessEngineException;

/**
 * @author Thorben Lindhauer
 */
public interface InstantiationBuilder<T extends InstantiationBuilder<T>> {

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Start before the specified activity.</p>
   *
   * <p>In particular:
   *   <ul>
   *     <li>In the parent activity hierarchy, determine the closest existing ancestor activity instance</li>
   *     <li>Instantiate all parent activities up to the ancestor's activity</li>
   *     <li>Instantiate and execute the given activity (respects the asyncBefore
   *       attribute of the activity)</li>
   *   </ul>
   * </p>
   *
   * @param activityId the activity to instantiate
   * @throws ProcessEngineException if more than one possible ancestor activity instance exists
   */
  T startBeforeActivity(String activityId);

  /**
   * Submits an instruction that behaves like {@link #startTransition(String)} and always instantiates
   * the single outgoing sequence flow of the given activity. Does not consider asyncAfter.
   *
   * @param activityId the activity for which the outgoing flow should be executed
   * @throws ProcessEngineException if the activity has 0 or more than 1 outgoing sequence flows
   */
  T startAfterActivity(String activityId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Start a sequence flow.</p>
   *
   * <p>In particular:
   *   <ul>
   *     <li>In the parent activity hierarchy, determine the closest existing ancestor activity instance</li>
   *     <li>Instantiate all parent activities up to the ancestor's activity</li>
   *     <li>Execute the given transition (does not consider sequence flow conditions)</li>
   *   </ul>
   * </p>
   *
   * @param transitionId the sequence flow to execute
   * @throws ProcessEngineException if more than one possible ancestor activity instance exists
   */
  T startTransition(String transitionId);
}
