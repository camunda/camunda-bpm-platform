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

import java.util.Date;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.query.Query;

/**
 * Allows programmatic querying of {@link ProcessDefinition}s.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Saeid Mirzaei
 */
public interface ProcessDefinitionQuery extends Query<ProcessDefinitionQuery, ProcessDefinition> {

  /** Only select process definiton with the given id.  */
  ProcessDefinitionQuery processDefinitionId(String processDefinitionId);

  /** Only select process definiton with the given id.  */
  ProcessDefinitionQuery processDefinitionIdIn(String... ids);

  /** Only select process definitions with the given category. */
  ProcessDefinitionQuery processDefinitionCategory(String processDefinitionCategory);

  /**
   * Only select process definitions where the category matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  ProcessDefinitionQuery processDefinitionCategoryLike(String processDefinitionCategoryLike);

  /** Only select process definitions with the given name. */
  ProcessDefinitionQuery processDefinitionName(String processDefinitionName);

  /**
   * Only select process definitions where the name matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  ProcessDefinitionQuery processDefinitionNameLike(String processDefinitionNameLike);

  /**
   * Only select process definitions that are deployed in a deployment with the
   * given deployment id
   */
  ProcessDefinitionQuery deploymentId(String deploymentId);

  /**
   * Only select process definitions that were deployed after the given Date (exclusive).
   */
  ProcessDefinitionQuery deployedAfter(Date deployedAfter);

  /**
   * Only select process definitions that were deployed at the given Date.
   */
  ProcessDefinitionQuery deployedAt(Date deployedAt);

  /**
   * Only select process definition with the given key.
   */
  ProcessDefinitionQuery processDefinitionKey(String processDefinitionKey);

  /**
   * Only select process definitions with the given keys
   */
  ProcessDefinitionQueryImpl processDefinitionKeysIn(String... processDefinitionKeys);

  /**
   * Only select process definitions where the key matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  ProcessDefinitionQuery processDefinitionKeyLike(String processDefinitionKeyLike);

  /**
   * Only select process definition with a certain version.
   * Particulary useful when used in combination with {@link #processDefinitionKey(String)}
   */
  ProcessDefinitionQuery processDefinitionVersion(Integer processDefinitionVersion);

  /**
   * <p>
   * Only select the process definitions which are the latest deployed (ie.
   * which have the highest version number for the given key).
   * </p>
   *
   * <p>
   * Can only be used in combination with {@link #processDefinitionKey(String)}
   * of {@link #processDefinitionKeyLike(String)}. Can also be used without any
   * other criteria (ie. query.latest().list()), which will then give all the
   * latest versions of all the deployed process definitions.
   * </p>
   *
   * <p>For multi-tenancy: select the latest deployed process definitions for each
   * tenant. If a process definition is deployed for multiple tenants then all
   * process definitions are selected.</p>
   *
   * @throws ProcessEngineException
   *           if used in combination with {@link #groupId(string)},
   *           {@link #processDefinitionVersion(int)} or
   *           {@link #deploymentId(String)}
   */
  ProcessDefinitionQuery latestVersion();

  /** Only select process definition with the given resource name. */
  ProcessDefinitionQuery processDefinitionResourceName(String resourceName);

  /** Only select process definition with a resource name like the given . */
  ProcessDefinitionQuery processDefinitionResourceNameLike(String resourceNameLike);

  /**
   * Only selects process definitions which given userId is authorized to start
   */
  ProcessDefinitionQuery startableByUser(String userId);

  /**
   * Only selects process definitions which are suspended
   */
  ProcessDefinitionQuery suspended();

  /**
   * Only selects process definitions which are active
   */
  ProcessDefinitionQuery active();

  /**
   * Only selects process definitions with the given incident type.
   */
  ProcessDefinitionQuery incidentType(String incidentType);

  /**
   * Only selects process definitions with the given incident id.
   */
  ProcessDefinitionQuery incidentId(String incidentId);

  /**
   * Only selects process definitions with the given incident message.
   */
  ProcessDefinitionQuery incidentMessage(String incidentMessage);

  /**
   * Only selects process definitions with an incident message like the given.
   */
  ProcessDefinitionQuery incidentMessageLike(String incidentMessageLike);

  /**
   * Only selects process definitions with a specific version tag
   */
  ProcessDefinitionQuery versionTag(String versionTag);

  /**
   * Only selects process definitions with a version tag like the given
   */
  ProcessDefinitionQuery versionTagLike(String versionTagLike);

  /**
   * Only selects process definitions without a version tag
   */
  ProcessDefinitionQuery withoutVersionTag();

  // Support for event subscriptions /////////////////////////////////////

  /**
   * @see #messageEventSubscriptionName(String)
   */
  @Deprecated
  ProcessDefinitionQuery messageEventSubscription(String messageName);

  /**
   * Selects the single process definition which has a start message event
   * with the messageName.
   */
  ProcessDefinitionQuery messageEventSubscriptionName(String messageName);

  /** Only select process definitions with one of the given tenant ids. */
  ProcessDefinitionQuery tenantIdIn(String... tenantIds);

  /** Only select process definitions which have no tenant id. */
  ProcessDefinitionQuery withoutTenantId();

  /**
   * Select process definitions which have no tenant id. Can be used in
   * combination with {@link #tenantIdIn(String...)}.
   */
  ProcessDefinitionQuery includeProcessDefinitionsWithoutTenantId();

  /**
   * Select process definitions which could be started in Tasklist.
   */
  ProcessDefinitionQuery startableInTasklist();

  /**
   * Select process definitions which could not be started in Tasklist.
   */
  ProcessDefinitionQuery notStartableInTasklist();

  ProcessDefinitionQuery startablePermissionCheck();

  // ordering ////////////////////////////////////////////////////////////

  /** Order by the category of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByProcessDefinitionCategory();

  /** Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByProcessDefinitionKey();

  /** Order by the id of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByProcessDefinitionId();

  /** Order by the version of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByProcessDefinitionVersion();

  /** Order by the name of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByProcessDefinitionName();

  /** Order by deployment id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByDeploymentId();

  /** Order by deployment time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByDeploymentTime();

  /** Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of process instances without tenant id is database-specific. */
  ProcessDefinitionQuery orderByTenantId();

  /**
   * Order by version tag (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * <strong>Note:</strong> sorting by versionTag is a string based sort.
   * There is no interpretation of the version which can lead to a sorting like:
   * v0.1.0 v0.10.0 v0.2.0.
   */
  ProcessDefinitionQuery orderByVersionTag();

}
