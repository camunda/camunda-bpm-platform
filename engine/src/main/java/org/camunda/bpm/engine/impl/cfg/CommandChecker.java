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

package org.camunda.bpm.engine.impl.cfg;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * Is invoked while executing a command to check if the current operation is
 * allowed on the entity. If it is not allowed, the checker throws a
 * {@link ProcessEngineException}.
 */
public interface CommandChecker {

  /**
   * Checks if it is allowed to evaluate the given decision.
   */
  void checkEvaluateDecision(DecisionDefinition decisionDefinition);

  /**
   * Checks if it is allowed to create an instance of the given process definition.
   */
  void checkCreateProcessInstance(ProcessDefinition processDefinition);

  /**
   * Checks if it is allowed to read the given process definition.
   */
  void checkReadProcessDefinition(ProcessDefinition processDefinition);

  /**
   * Checks if it is allowed to create an instance of the given case definition.
   */
  void checkCreateCaseInstance(CaseDefinition caseDefinition);

}
