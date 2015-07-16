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

import org.camunda.bpm.engine.query.Query;

public interface DecisionDefinitionQuery extends Query<DecisionDefinitionQuery, DecisionDefinition> {

  /**
   * Only select decision definition with the given id.
   *
   * @param decisionDefinitionId the id of the decision definition
   */
  DecisionDefinitionQuery decisionDefinitionId(String decisionDefinitionId);

  /**
   * Only select decision definitions with the given ids.
   *
   * @param ids list of decision definition ids
   */
  DecisionDefinitionQuery decisionDefinitionIdIn(String... ids);

  /**
   * Only select decision definitions with the given category.
   *
   * @param decisionDefinitionCategory the category of the decision definition
   */
  DecisionDefinitionQuery decisionDefinitionCategory(String decisionDefinitionCategory);

  /**
   * Only select decision definitions where the category matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param decisionDefinitionCategoryLike the pattern to match the decision definition category
   */
  DecisionDefinitionQuery decisionDefinitionCategoryLike(String decisionDefinitionCategoryLike);

  /**
   * Only select decision definitions with the given name.
   *
   * @param decisionDefinitionName the name of the decision definition
   */
  DecisionDefinitionQuery decisionDefinitionName(String decisionDefinitionName);

  /**
   * Only select decision definition with the given key.
   *
   * @param decisionDefinitionKey the key of the decision definition
   */
  DecisionDefinitionQuery decisionDefinitionKey(String decisionDefinitionKey);

  /**
   * Only select decision definitions where the key matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param decisionDefinitionKeyLike the pattern to match the decision definition key
   */
  DecisionDefinitionQuery decisionDefinitionKeyLike(String decisionDefinitionKeyLike);

  /**
   * Only select decision definitions where the name matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param decisionDefinitionNameLike the pattern to match the decision definition name
   */
  DecisionDefinitionQuery decisionDefinitionNameLike(String decisionDefinitionNameLike);

  /**
   * Only select decision definitions that are deployed in a deployment with the
   * given deployment id.
   *
   * @param deploymentId the id of the deployment
   */
  DecisionDefinitionQuery deploymentId(String deploymentId);

  /**
   * Only select decision definition with a certain version.
   * Particularly useful when used in combination with {@link #decisionDefinitionKey(String)}
   *
   * @param decisionDefinitionVersion the version of the decision definition
   */
  DecisionDefinitionQuery decisionDefinitionVersion(Integer decisionDefinitionVersion);

  /**
   * Only select the decision definitions which are the latest deployed
   * (ie. which have the highest version number for the given key).
   *
   * Can only be used in combination with {@link #decisionDefinitionKey(String)}
   * or {@link #decisionDefinitionKeyLike(String)}. Can also be used without any
   * other criteria (ie. query.latest().list()), which will then give all the
   * latest versions of all the deployed decision definitions.
   *
   */
  DecisionDefinitionQuery latestVersion();

  /**
   * Only select decision definition with the given resource name.
   *
   * @param resourceName the name of the resource
   */
  DecisionDefinitionQuery decisionDefinitionResourceName(String resourceName);

  /**
   * Only select decision definition with a resource name like the given.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param resourceNameLike the pattern to match the resource name
   */
  DecisionDefinitionQuery decisionDefinitionResourceNameLike(String resourceNameLike);

  // ordering ////////////////////////////////////////////////////////////

  /** Order by the category of the decision definitions (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  DecisionDefinitionQuery orderByDecisionDefinitionCategory();

  /** Order by decision definition key (needs to be followed by {@link #asc()} or
   * {@link #desc()}). */
  DecisionDefinitionQuery orderByDecisionDefinitionKey();

  /** Order by the id of the decision definitions (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  DecisionDefinitionQuery orderByDecisionDefinitionId();

  /** Order by the version of the decision definitions (needs to be followed
   * by {@link #asc()} or {@link #desc()}). */
  DecisionDefinitionQuery orderByDecisionDefinitionVersion();

  /** Order by the name of the decision definitions (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  DecisionDefinitionQuery orderByDecisionDefinitionName();

  /** Order by deployment id (needs to be followed by {@link #asc()}
   * or {@link #desc()}). */
  DecisionDefinitionQuery orderByDeploymentId();

}
