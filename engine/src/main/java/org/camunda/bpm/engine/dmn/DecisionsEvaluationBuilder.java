/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.dmn;

import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;

/**
 * Fluent builder to evaluate a decision.
 */
public interface DecisionsEvaluationBuilder {

  /**
   * Specify the id of the tenant the decision definition belongs to. Can only be
   * used when the definition is referenced by <code>key</code> and not by <code>id</code>.
   */
  DecisionsEvaluationBuilder decisionDefinitionTenantId(String tenantId);

  /**
   * Specify that the decision definition belongs to no tenant. Can only be
   * used when the definition is referenced by <code>key</code> and not by <code>id</code>.
   */
  DecisionsEvaluationBuilder decisionDefinitionWithoutTenantId();

  /**
   * Set the version of the decision definition. If <code>null</code> then
   * the latest version is taken.
   */
  DecisionsEvaluationBuilder version(Integer version);

  /**
   * Set the input values of the decision.
   */
  DecisionsEvaluationBuilder variables(Map<String, Object> variables);

  /**
   * Evaluates the decision.
   *
   * @return the result of the evaluation.
   *
   * @throws NotFoundException
   *           when no decision definition is deployed with the given id / key.
   *
   * @throws NotValidException
   *           when the given decision definition id / key is null.
   *
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#CREATE_INSTANCE} permission
   *           on {@link Resources#DECISION_DEFINITION}.
   */
  DmnDecisionResult evaluate();

}
