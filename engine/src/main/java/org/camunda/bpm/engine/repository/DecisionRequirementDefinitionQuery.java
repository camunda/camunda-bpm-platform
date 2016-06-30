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

public interface DecisionRequirementDefinitionQuery extends Query<DecisionRequirementDefinitionQuery, DecisionRequirementDefinition> {

  /**
   * Only select decision requirement definition with the given id.
   *
   * @param id the id of the decision requirement definition
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionId(String id);

  /**
   * Only select decision requirement definitions with the given ids.
   *
   * @param ids list of decision requirement definition ids
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionIdIn(String... ids);

  /**
   * Only select decision requirement definitions with the given category.
   *
   * @param category the category of the decision requirement definition
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionCategory(String category);

  /**
   * Only select decision requirement definitions where the category matches the given parameter.
   * The syntax that should be used is the same as in SQL, e.g., %category%.
   *
   * @param categoryLike the pattern to match the decision requirement definition category
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionCategoryLike(String categoryLike);

  /**
   * Only select decision requirement definitions with the given name.
   *
   * @param name the name of the decision requirement definition
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionName(String name);

  /**
   * Only select decision requirement definitions where the name matches the given parameter.
   * The syntax that should be used is the same as in SQL, e.g., %name%.
   *
   * @param nameLike the pattern to match the decision requirement definition name
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionNameLike(String nameLike);

  /**
   * Only select decision requirement definition with the given key.
   *
   * @param key the key of the decision definition
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionKey(String key);

  /**
   * Only select decision requirement definitions where the key matches the given parameter.
   * The syntax that should be used is the same as in SQL, e.g., %key%.
   *
   * @param keyLike the pattern to match the decision requirement definition key
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionKeyLike(String keyLike);

  /**
   * Only select decision requirement definitions that are deployed in a deployment with the
   * given deployment id.
   *
   * @param deploymentId the id of the deployment
   */
  DecisionRequirementDefinitionQuery deploymentId(String deploymentId);

  /**
   * Only select decision requirement definition with a certain version.
   * Particularly useful when used in combination with {@link #decisionRequirementDefinitionKey(String)}
   *
   * @param version the version of the decision requirement definition
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionVersion(Integer version);

  /**
   * Only select the decision requirement definitions which are the latest deployed
   * (i.e. which have the highest version number for the given key).
   *
   * Can only be used in combination with {@link #decisionRequirementDefinitionKey(String)}
   * or {@link #decisionRequirementDefinitionKeyLike(String)}. Can also be used without any
   * other criteria (i.e. query.latest().list()), which will then give all the
   * latest versions of all the deployed decision requirement definitions.
   *
   */
  DecisionRequirementDefinitionQuery latestVersion();

  /**
   * Only select decision requirement definition with the given resource name.
   *
   * @param resourceName the name of the resource
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionResourceName(String resourceName);

  /**
   * Only select decision requirement definition with a resource name like the given.
   * The syntax that should be used is the same as in SQL, e.g., %resourceName%.
   *
   * @param resourceNameLike the pattern to match the resource name
   */
  DecisionRequirementDefinitionQuery decisionRequirementDefinitionResourceNameLike(String resourceNameLike);

  /** Only select decision requirement definitions with one of the given tenant ids. */
  DecisionRequirementDefinitionQuery tenantIdIn(String... tenantIds);

  /** Only select decision requirement definitions which have no tenant id. */
  DecisionRequirementDefinitionQuery withoutTenantId();

  /**
   * Select decision requirement definitions which have no tenant id. Can be used in
   * combination with {@link #tenantIdIn(String...)}.
   */
  DecisionRequirementDefinitionQuery includeDecisionRequirementDefinitionsWithoutTenantId();

  // ordering ////////////////////////////////////////////////////////////

  /** Order by the category of the decision requirement definitions (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  DecisionRequirementDefinitionQuery orderByDecisionRequirementDefinitionCategory();

  /** Order by decision requirement definition key (needs to be followed by {@link #asc()} or
   * {@link #desc()}). */
  DecisionRequirementDefinitionQuery orderByDecisionRequirementDefinitionKey();

  /** Order by the id of the decision requirement definitions (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  DecisionRequirementDefinitionQuery orderByDecisionRequirementDefinitionId();

  /** Order by the version of the decision requirement definitions (needs to be followed
   * by {@link #asc()} or {@link #desc()}). */
  DecisionRequirementDefinitionQuery orderByDecisionRequirementDefinitionVersion();

  /** Order by the name of the decision requirement definitions (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  DecisionRequirementDefinitionQuery orderByDecisionRequirementDefinitionName();

  /** Order by deployment id (needs to be followed by {@link #asc()}
   * or {@link #desc()}). */
  DecisionRequirementDefinitionQuery orderByDeploymentId();

  /** Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of decision requirement definitions without tenant id is database-specific. */
  DecisionRequirementDefinitionQuery orderByTenantId();

}
