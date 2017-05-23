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
   * The syntax that should be used is the same as in SQL, eg. %category%
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
   * The syntax that should be used is the same as in SQL, eg. %key%
   *
   * @param decisionDefinitionKeyLike the pattern to match the decision definition key
   */
  DecisionDefinitionQuery decisionDefinitionKeyLike(String decisionDefinitionKeyLike);

  /**
   * Only select decision definitions where the name matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %name%
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
   * The syntax that should be used is the same as in SQL, eg. %resourceName%
   *
   * @param resourceNameLike the pattern to match the resource name
   */
  DecisionDefinitionQuery decisionDefinitionResourceNameLike(String resourceNameLike);

  /**
   * Only select decision definitions which belongs to a decision requirements definition with the given id.
   *
   * @param decisionRequirementsDefinitionId id of the related decision requirements definition
   */
  DecisionDefinitionQuery decisionRequirementsDefinitionId(String decisionRequirementsDefinitionId);

  /**
   * Only select decision definitions which belongs to a decision requirements definition with the given key.
   *
   * @param decisionRequirementsDefinitionKey key of the related decision requirements definition
   */
  DecisionDefinitionQuery decisionRequirementsDefinitionKey(String decisionRequirementsDefinitionKey);

  /**
   * Only select decision definitions which belongs to no decision requirements definition.
   */
  DecisionDefinitionQuery withoutDecisionRequirementsDefinition();

  /** Only select decision definitions with one of the given tenant ids. */
  DecisionDefinitionQuery tenantIdIn(String... tenantIds);

  /** Only select decision definitions which have no tenant id. */
  DecisionDefinitionQuery withoutTenantId();

  /**
   * Select decision definitions which have no tenant id. Can be used in
   * combination with {@link #tenantIdIn(String...)}.
   */
  DecisionDefinitionQuery includeDecisionDefinitionsWithoutTenantId();

  /**
   * Only selects decision definitions with a specific version tag
   */
  DecisionDefinitionQuery versionTag(String versionTag);

  /**
   * Only selects decision definitions with a version tag like the given
   */
  DecisionDefinitionQuery versionTagLike(String versionTagLike);

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

  /** Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of decision definitions without tenant id is database-specific. */
  DecisionDefinitionQuery orderByTenantId();

  /**
   * Order by version tag (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * <strong>Note:</strong> sorting by versionTag is a string based sort.
   * There is no interpretation of the version which can lead to a sorting like:
   * v0.1.0 v0.10.0 v0.2.0.
   */
  DecisionDefinitionQuery orderByVersionTag();

}
