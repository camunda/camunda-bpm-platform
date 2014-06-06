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

/**
 * @author Roman Smirnov
 *
 */
public interface CaseDefinitionQuery extends Query<CaseDefinitionQuery, CaseDefinition> {

  /** Only select case definition with the given id.  */
  CaseDefinitionQuery caseDefinitionId(String caseDefinitionId);

  /** Only select case definitions with the given category. */
  CaseDefinitionQuery caseDefinitionCategory(String caseDefinitionCategory);

  /**
   * Only select case definitions where the category matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  CaseDefinitionQuery caseDefinitionCategoryLike(String caseDefinitionCategoryLike);

  /** Only select case definitions with the given name. */
  CaseDefinitionQuery caseDefinitionName(String caseDefinitionName);

  /**
   * Only select case definition with the given key.
   */
  CaseDefinitionQuery caseDefinitionKey(String caseDefinitionKey);

  /**
   * Only select case definitions where the key matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  CaseDefinitionQuery caseDefinitionKeyLike(String caseDefinitionKeyLike);

  /**
   * Only select case definitions where the name matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  CaseDefinitionQuery caseDefinitionNameLike(String caseDefinitionNameLike);

  /**
   * Only select case definitions that are deployed in a deployment with the
   * given deployment id.
   */
  CaseDefinitionQuery deploymentId(String deploymentId);

  /**
   * Only select case definition with a certain version.
   * Particularly useful when used in combination with {@link #caseDefinitionKey(String)}
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

  /** Only select case definition with the given resource name. */
  CaseDefinitionQuery caseDefinitionResourceName(String resourceName);

  /** Only select case definition with a resource name like the given . */
  CaseDefinitionQuery caseDefinitionResourceNameLike(String resourceNameLike);

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

}
