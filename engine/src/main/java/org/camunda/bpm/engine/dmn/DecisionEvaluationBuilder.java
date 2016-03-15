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
package org.camunda.bpm.engine.dmn;

import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;

/**
 * @author Kristin Polenz
 *
 */
public interface DecisionEvaluationBuilder {

  /**
   * Specify the id of the tenant the decision definition belongs to. Can only be
   * used when the definition is referenced by <code>key</code> and not by <code>id</code>.
   */
  DecisionEvaluationBuilder decisionDefinitionTenantId(String tenantId);

  /**
   * Specify that the decision definition belongs to no tenant. Can only be
   * used when the definition is referenced by <code>key</code> and not by <code>id</code>.
   */
  DecisionEvaluationBuilder decisionDefinitionWithoutTenantId();

  /**
   * Set the version of the decision definition. If <code>null</code> then
   * the latest version is taken.
   */
  DecisionEvaluationBuilder version(Integer version);

  /**
   * Set the input values of the decision.
   */
  DecisionEvaluationBuilder variables(Map<String, Object> variables);

  DmnDecisionTableResult evaluate();

}
