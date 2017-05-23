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
package org.camunda.bpm.engine.repository;

/**
 * Definition of a decision resource
 */
public interface DecisionDefinition extends ResourceDefinition {

  /**
   * Returns the id of the related decision requirements definition. Can be
   * <code>null</code> if the decision has no relations to other decisions.
   *
   * @return the id of the decision requirements definition if exists.
   */
  String getDecisionRequirementsDefinitionId();

  /**
   * Returns the key of the related decision requirements definition. Can be
   * <code>null</code> if the decision has no relations to other decisions.
   *
   * @return the key of the decision requirements definition if exists.
   */
  String getDecisionRequirementsDefinitionKey();

  /** Version tag of the decision definition. */
  String getVersionTag();

}
