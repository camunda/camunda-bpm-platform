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

package org.camunda.bpm.engine.impl.dmn.result;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * Mapping function for the decision table result.
 *
 * @author Philipp Ossler
 */
public interface DecisionTableResultMapper {

  /**
   * Maps the decision result into a value that can set as process variable.
   *
   * @param decisionTableResult
   *          the result of the evaluated decision
   * @return the value that should set as process variable
   * @throws ProcessEngineException
   *           if the decision result can not be mapped
   */
  Object mapDecisionTableResult(DmnDecisionTableResult decisionTableResult);

}
