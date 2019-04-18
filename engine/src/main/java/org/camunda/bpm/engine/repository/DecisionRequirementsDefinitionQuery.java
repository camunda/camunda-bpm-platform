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
package org.camunda.bpm.engine.repository;

import org.camunda.bpm.engine.query.Query;

public interface DecisionRequirementsDefinitionQuery extends Query<DecisionRequirementsDefinitionQuery, DecisionRequirementsDefinition> {

  /**
   * Only select decision requirements definition with the given id.
   *
   * @param id the id of the decision requirements definition
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionId(String id);

  /**
   * Only select decision requirements definition with the given ids.
   *
   * @param ids list of decision requirements definition ids
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionIdIn(String... ids);

  /**
   * Only select decision requirements definition with the given category.
   *
   * @param category the category of the decision requirements definition
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionCategory(String category);

  /**
   * Only select decision requirements definition where the category matches the given parameter.
   * The syntax that should be used is the same as in SQL, e.g., %category%.
   *
   * @param categoryLike the pattern to match the decision requirements definition category
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionCategoryLike(String categoryLike);

  /**
   * Only select decision requirements definition with the given name.
   *
   * @param name the name of the decision requirements definition
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionName(String name);

  /**
   * Only select decision requirements definition where the name matches the given parameter.
   * The syntax that should be used is the same as in SQL, e.g., %name%.
   *
   * @param nameLike the pattern to match the decision requirements definition name
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionNameLike(String nameLike);

  /**
   * Only select decision requirements definition with the given key.
   *
   * @param key the key of the decision definition
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionKey(String key);

  /**
   * Only select decision requirements definition where the key matches the given parameter.
   * The syntax that should be used is the same as in SQL, e.g., %key%.
   *
   * @param keyLike the pattern to match the decision requirements definition key
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionKeyLike(String keyLike);

  /**
   * Only select decision requirements definition that are deployed in a deployment with the
   * given deployment id.
   *
   * @param deploymentId the id of the deployment
   */
  DecisionRequirementsDefinitionQuery deploymentId(String deploymentId);

  /**
   * Only select decision requirements definition with a certain version.
   * Particularly useful when used in combination with {@link #decisionRequirementsDefinitionKey(String)}
   *
   * @param version the version of the decision requirements definition
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionVersion(Integer version);

  /**
   * Only select the decision requirements definition which are the latest deployed
   * (i.e. which have the highest version number for the given key).
   *
   * Can only be used in combination with {@link #decisionRequirementsDefinitionKey(String)}
   * or {@link #decisionRequirementsDefinitionKeyLike(String)}. Can also be used without any
   * other criteria (i.e. query.latest().list()), which will then give all the
   * latest versions of all the deployed decision requirements definition.
   *
   */
  DecisionRequirementsDefinitionQuery latestVersion();

  /**
   * Only select decision requirements definition with the given resource name.
   *
   * @param resourceName the name of the resource
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionResourceName(String resourceName);

  /**
   * Only select decision requirements definition with a resource name like the given.
   * The syntax that should be used is the same as in SQL, e.g., %resourceName%.
   *
   * @param resourceNameLike the pattern to match the resource name
   */
  DecisionRequirementsDefinitionQuery decisionRequirementsDefinitionResourceNameLike(String resourceNameLike);

  /** Only select decision requirements definition with one of the given tenant ids. */
  DecisionRequirementsDefinitionQuery tenantIdIn(String... tenantIds);

  /** Only select decision requirements definition which have no tenant id. */
  DecisionRequirementsDefinitionQuery withoutTenantId();

  /**
   * Select decision requirements definition which have no tenant id. Can be used in
   * combination with {@link #tenantIdIn(String...)}.
   */
  DecisionRequirementsDefinitionQuery includeDecisionRequirementsDefinitionsWithoutTenantId();

  // ordering ////////////////////////////////////////////////////////////

  /** Order by the category of the decision requirements definition (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  DecisionRequirementsDefinitionQuery orderByDecisionRequirementsDefinitionCategory();

  /** Order by decision requirements definition key (needs to be followed by {@link #asc()} or
   * {@link #desc()}). */
  DecisionRequirementsDefinitionQuery orderByDecisionRequirementsDefinitionKey();

  /** Order by the id of the decision requirements definition (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  DecisionRequirementsDefinitionQuery orderByDecisionRequirementsDefinitionId();

  /** Order by the version of the decision requirements definition (needs to be followed
   * by {@link #asc()} or {@link #desc()}). */
  DecisionRequirementsDefinitionQuery orderByDecisionRequirementsDefinitionVersion();

  /** Order by the name of the decision requirements definition (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  DecisionRequirementsDefinitionQuery orderByDecisionRequirementsDefinitionName();

  /** Order by deployment id (needs to be followed by {@link #asc()}
   * or {@link #desc()}). */
  DecisionRequirementsDefinitionQuery orderByDeploymentId();

  /** Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of decision requirements definition without tenant id is database-specific. */
  DecisionRequirementsDefinitionQuery orderByTenantId();

}
