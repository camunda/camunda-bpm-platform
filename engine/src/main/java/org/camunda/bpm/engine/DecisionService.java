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

package org.camunda.bpm.engine;

import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.dmn.DecisionEvaluationBuilder;
import org.camunda.bpm.engine.dmn.DecisionsEvaluationBuilder;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;

/**
 * Service to evaluate decisions inside the DMN engine.
 *
 * @author Philipp Ossler
 */
public interface DecisionService {

  /**
   * Evaluates the decision with the given id.
   *
   * @param decisionDefinitionId
   *          the id of the decision definition, cannot be null.
   * @param variables
   *          the input values of the decision.
   * @return the result of the evaluation.
   *
   * @throws NotFoundException
   *           when no decision definition is deployed with the given id.
   *
   * @throws NotValidException
   *           when the given decision definition id is null.
   *
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#CREATE_INSTANCE} permission
   *           on {@link Resources#DECISION_DEFINITION}.
   */
  DmnDecisionTableResult evaluateDecisionTableById(String decisionDefinitionId, Map<String, Object> variables);

  /**
   * Evaluates the decision with the given key in the latest version.
   *
   * @param decisionDefinitionKey
   *          the key of the decision definition, cannot be null.
   * @param variables
   *          the input values of the decision.
   * @return the result of the evaluation.
   *
   * @throws NotFoundException
   *           when no decision definition is deployed with the given key.
   *
   * @throws NotValidException
   *           when the given decision definition key is null.
   *
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#CREATE_INSTANCE} permission
   *           on {@link Resources#DECISION_DEFINITION}.
   */
  DmnDecisionTableResult evaluateDecisionTableByKey(String decisionDefinitionKey, Map<String, Object> variables);

  /**
   * Evaluates the decision with the given key in the specified version. If no
   * version is provided then the latest version of the decision definition is
   * taken.
   *
   * @param decisionDefinitionKey
   *          the key of the decision definition, cannot be null.
   * @param version
   *          the version of the decision definition. If <code>null</code> then
   *          the latest version is taken.
   * @param variables
   *          the input values of the decision.
   * @return the result of the evaluation.
   *
   * @throws NotFoundException
   *           when no decision definition is deployed with the given key and
   *           version.
   *
   * @throws NotValidException
   *           when the given decision definition key is null.
   *
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#CREATE_INSTANCE} permission
   *           on {@link Resources#DECISION_DEFINITION}.
   */
  DmnDecisionTableResult evaluateDecisionTableByKeyAndVersion(String decisionDefinitionKey, Integer version, Map<String, Object> variables);

  /**
   * Returns a fluent builder to evaluate the decision table with the given key.
   * The builder can be used to set further properties and specify evaluation
   * instructions.
   *
   * @param decisionDefinitionKey
   *          the key of the decision definition, cannot be <code>null</code>.
   *
   * @return a builder to evaluate a decision table
   *
   * @see #evaluateDecisionByKey(String)
   */
  DecisionEvaluationBuilder evaluateDecisionTableByKey(String decisionDefinitionKey);

  /**
   * Returns a fluent builder to evaluate the decision table with the given id.
   * The builder can be used to set further properties and specify evaluation
   * instructions.
   *
   * @param decisionDefinitionId
   *          the id of the decision definition, cannot be <code>null<code>.
   *
   * @return a builder to evaluate a decision table
   *
   * @see #evaluateDecisionById(String)
   */
  DecisionEvaluationBuilder evaluateDecisionTableById(String decisionDefinitionId);

  /**
   * Returns a fluent builder to evaluate the decision with the given key.
   * The builder can be used to set further properties and specify evaluation
   * instructions.
   *
   * @param decisionDefinitionKey
   *          the key of the decision definition, cannot be <code>null</code>.
   *
   * @return a builder to evaluate a decision
   */
  DecisionsEvaluationBuilder evaluateDecisionByKey(String decisionDefinitionKey);

  /**
   * Returns a fluent builder to evaluate the decision with the given id.
   * The builder can be used to set further properties and specify evaluation
   * instructions.
   *
   * @param decisionDefinitionId
   *          the id of the decision definition, cannot be <code>null<code>.
   *
   * @return a builder to evaluate a decision
   */
  DecisionsEvaluationBuilder evaluateDecisionById(String decisionDefinitionId);

}
