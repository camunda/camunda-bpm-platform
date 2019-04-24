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

/**
 * @author Roman Smirnov
 *
 */
public interface CaseDefinitionQuery extends Query<CaseDefinitionQuery, CaseDefinition> {

  /**
   * Only select case definition with the given id.
   *
   * @param caseDefinitionId the id of the case definition
   */
  CaseDefinitionQuery caseDefinitionId(String caseDefinitionId);

  /**
   * Only select case definitions with the given ids.
   *
   * @param ids list of case definition ids
   */
  CaseDefinitionQuery caseDefinitionIdIn(String... ids);

  /**
   * Only select case definitions with the given category.
   *
   * @param caseDefinitionCategory the category of the case definition
   */
  CaseDefinitionQuery caseDefinitionCategory(String caseDefinitionCategory);

  /**
   * Only select case definitions where the category matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param caseDefinitionCategoryLike the pattern to match the case definition category
   */
  CaseDefinitionQuery caseDefinitionCategoryLike(String caseDefinitionCategoryLike);

  /**
   * Only select case definitions with the given name.
   *
   * @param caseDefinitionName the name of the case definition
   */
  CaseDefinitionQuery caseDefinitionName(String caseDefinitionName);

  /**
   * Only select case definition with the given key.
   *
   * @param caseDefinitionKey the key of the case definition
   */
  CaseDefinitionQuery caseDefinitionKey(String caseDefinitionKey);

  /**
   * Only select case definitions where the key matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param caseDefinitionKeyLike the pattern to match the case definition key
   */
  CaseDefinitionQuery caseDefinitionKeyLike(String caseDefinitionKeyLike);

  /**
   * Only select case definitions where the name matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param caseDefinitionNameLike the pattern to match the case definition name
   */
  CaseDefinitionQuery caseDefinitionNameLike(String caseDefinitionNameLike);

  /**
   * Only select case definitions that are deployed in a deployment with the
   * given deployment id.
   *
   * @param deploymentId the id of the deployment
   */
  CaseDefinitionQuery deploymentId(String deploymentId);

  /**
   * Only select case definition with a certain version.
   * Particularly useful when used in combination with {@link #caseDefinitionKey(String)}
   *
   * @param caseDefinitionVersion the version of the case definition
   */
  CaseDefinitionQuery caseDefinitionVersion(Integer caseDefinitionVersion);

  /**
   * Only select the case definitions which are the latest deployed
   * (ie. which have the highest version number for the given key).
   *
   * Can only be used in combination with {@link #caseDefinitionKey(String)}
   * or {@link #caseDefinitionKeyLike(String)}. Can also be used without any
   * other criteria (ie. query.latest().list()), which will then give all the
   * latest versions of all the deployed case definitions.
   *
   */
  CaseDefinitionQuery latestVersion();

  /**
   * Only select case definition with the given resource name.
   *
   * @param resourceName the name of the resource
   */
  CaseDefinitionQuery caseDefinitionResourceName(String resourceName);

  /**
   * Only select case definition with a resource name like the given.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param resourceNameLike the pattern to match the resource name
   */
  CaseDefinitionQuery caseDefinitionResourceNameLike(String resourceNameLike);

  /** Only select case definitions with one of the given tenant ids. */
  CaseDefinitionQuery tenantIdIn(String... tenantIds);

  /** Only select case definitions which have no tenant id. */
  CaseDefinitionQuery withoutTenantId();

  /**
   * Select case definitions which have no tenant id. Can be used in
   * combination with {@link #tenantIdIn(String...)}.
   */
  CaseDefinitionQuery includeCaseDefinitionsWithoutTenantId();

  // ordering ////////////////////////////////////////////////////////////

  /** Order by the category of the case definitions (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  CaseDefinitionQuery orderByCaseDefinitionCategory();

  /** Order by case definition key (needs to be followed by {@link #asc()} or
   * {@link #desc()}). */
  CaseDefinitionQuery orderByCaseDefinitionKey();

  /** Order by the id of the case definitions (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  CaseDefinitionQuery orderByCaseDefinitionId();

  /** Order by the version of the case definitions (needs to be followed
   * by {@link #asc()} or {@link #desc()}). */
  CaseDefinitionQuery orderByCaseDefinitionVersion();

  /** Order by the name of the case definitions (needs to be followed by
   * {@link #asc()} or {@link #desc()}). */
  CaseDefinitionQuery orderByCaseDefinitionName();

  /** Order by deployment id (needs to be followed by {@link #asc()}
   * or {@link #desc()}). */
  CaseDefinitionQuery orderByDeploymentId();

  /** Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of case instances without tenant id is database-specific. */
  CaseDefinitionQuery orderByTenantId();

}
