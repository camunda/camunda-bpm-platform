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
 * The result of one decision. Which is the list of its
 * decision outputs (see {@link DmnDecisionOutput}). In
 * context of a decision table this represents the output
 * of all matching decision rules.
 */
public interface DmnDecisionResult extends List<DmnDecisionOutput> {

  /**
   * Returns the first {@link DmnDecisionOutput}.
   *
   * @return the first decision output in the result or null if none exits
   */
  DmnDecisionOutput getFirstOutput();

  /**
   * Returns the single {@link DmnDecisionOutput} of the result. Which
   * asserts that only one decision output exist.
   *
   * @return the single decision output or null if none exists
   * @throws DmnResultException if  more than one decision output exists
   */
  DmnDecisionOutput getSingleOutput();

  /**
   * Collects the values for a output name. The list will contain
   * the value for the output name of every {@link DmnDecisionOutput}.
   * If the {@link DmnDecisionOutput} doesn't contain a value for the
   * output name the value will be null.
   *
   * @param outputName the name of the output to collect
   * @param <T> the type of the output values
   * @return the list of collected output values
   */
  <T> List<T> collectOutputValues(String outputName);

}
