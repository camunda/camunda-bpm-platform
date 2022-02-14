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
package org.camunda.bpm.engine.history;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.query.Query;

/**
 * Allows programmatic querying of {@link HistoricCaseInstance}s.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 */
public interface HistoricCaseInstanceQuery extends Query<HistoricCaseInstanceQuery, HistoricCaseInstance> {

  /** Only select historic case instances with the given case instance id. */
  HistoricCaseInstanceQuery caseInstanceId(String caseInstanceId);

  /** Only select historic case instances whose id is in the given set of ids. */
  HistoricCaseInstanceQuery caseInstanceIds(Set<String> caseInstanceIds);

  /** Only select historic case instances for the given case definition */
  HistoricCaseInstanceQuery caseDefinitionId(String caseDefinitionId);

  /** Only select historic case instances that are defined by a case definition with the given key.  */
  HistoricCaseInstanceQuery caseDefinitionKey(String caseDefinitionKey);

  /** Only select historic case instances that don't have a case definition of which the key is present in the given list */
  HistoricCaseInstanceQuery caseDefinitionKeyNotIn(List<String> caseDefinitionKeys);

  /** Only select historic case instances that are defined by a case definition with the given name.  */
  HistoricCaseInstanceQuery caseDefinitionName(String caseDefinitionName);

  /**
   * Only select historic case instances that are defined by case definition which name
   * is like the given value.
   *
   * @param nameLike The string can include the wildcard character '%' to express
   *    like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  HistoricCaseInstanceQuery caseDefinitionNameLike(String nameLike);

  /** Only select historic case instances with the given business key */
  HistoricCaseInstanceQuery caseInstanceBusinessKey(String caseInstanceBusinessKey);

  /**
   * Only select historic case instances which had a business key like the given value.
   *
   * @param caseInstanceBusinessKeyLike The string can include the wildcard character '%' to express
   *    like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  HistoricCaseInstanceQuery caseInstanceBusinessKeyLike(String caseInstanceBusinessKeyLike);

  /**
   * <p>Only selects historic case instances with historic case activity instances
   * in at least one of the given case activity ids.</p>
   */
  HistoricCaseInstanceQuery caseActivityIdIn(String... caseActivityIds);

  /** Only select historic case instances that were created before the given date. */
  HistoricCaseInstanceQuery createdBefore(Date date);

  /** Only select historic case instances that were created after the given date. */
  HistoricCaseInstanceQuery createdAfter(Date date);

  /** Only select historic case instances that were closed before the given date. */
  HistoricCaseInstanceQuery closedBefore(Date date);

  /** Only select historic case instances that were closed after the given date. */
  HistoricCaseInstanceQuery closedAfter(Date date);

  /** Only select historic case instance that are created by the given user. */
  HistoricCaseInstanceQuery createdBy(String userId);

  /** Only select historic case instances started by the given case instance. */
  HistoricCaseInstanceQuery superCaseInstanceId(String superCaseInstanceId);

  /** Only select historic case instances having a sub case instance
   * with the given case instance id.
   *
   * Note that there will always be maximum only <b>one</b>
   * such case instance that can be the result of this query.
   */
  HistoricCaseInstanceQuery subCaseInstanceId(String subCaseInstanceId);

  /** Only select historic case instances started by the given process instance. */
  HistoricCaseInstanceQuery superProcessInstanceId(String superProcessInstanceId);

  /** Only select historic case instances having a sub process instance
   * with the given process instance id.
   *
   * Note that there will always be maximum only <b>one</b>
   * such case instance that can be the result of this query.
   */
  HistoricCaseInstanceQuery subProcessInstanceId(String subProcessInstanceId);

  /** Only select historic case instances with one of the given tenant ids. */
  HistoricCaseInstanceQuery tenantIdIn(String... tenantIds);

  /** Only selects historic case instances which have no tenant id. */
  HistoricCaseInstanceQuery withoutTenantId();

  /** Only select historic case instances which are active */
  HistoricCaseInstanceQuery active();

  /** Only select historic case instances which are completed */
  HistoricCaseInstanceQuery completed();

  /** Only select historic case instances which are terminated */
  HistoricCaseInstanceQuery terminated();

  /** Only select historic case instances which are closed */
  HistoricCaseInstanceQuery closed();

  /** Only select historic case instance that are not yet closed. */
  HistoricCaseInstanceQuery notClosed();

  /**
   * The query will match the names of variables in a case-insensitive way.
   */
  HistoricCaseInstanceQuery matchVariableNamesIgnoreCase();

  /**
   * The query will match the values of variables in a case-insensitive way.
   */
  HistoricCaseInstanceQuery matchVariableValuesIgnoreCase();

  /**
   * Only select case instances which have a global variable with the given value.
   *
   * @param name the name of the variable
   * @param value the value of the variable
   * @throws NotValidException if the name is null
   */
  HistoricCaseInstanceQuery variableValueEquals(String name, Object value);

  /**
   * Only select case instances which have a global variable with the given name
   * but a different value.
   *
   * @param name the name of the variable
   * @param value the value of the variable
   * @throws NotValidException if the name is null
   */
  HistoricCaseInstanceQuery variableValueNotEquals(String name, Object value);

  /**
   * Only select case instances which had a global variable with the given name
   * and a value greater than the given value when they where closed.
   *
   * @param name the name of the variable
   * @param value the value of the variable
   * @throws NotValidException if the name or value is null
   */
  HistoricCaseInstanceQuery variableValueGreaterThan(String name, Object value);

  /**
   * Only select case instances which have a global variable with the given name
   * and a value greater or equal than the given value.
   *
   * @param name the name of the variable
   * @param value the value of the variable
   * @throws NotValidException if the name or value is null
   */
  HistoricCaseInstanceQuery variableValueGreaterThanOrEqual(String name, Object value);

  /**
   * Only select case instances which have a global variable with the given name
   * and a value less than the given value.
   *
   * @param name the name of the variable
   * @param value the value of the variable
   * @throws NotValidException if the name or value is null
   */
  HistoricCaseInstanceQuery variableValueLessThan(String name, Object value);

  /**
   * Only select case instances which have a global variable with the given name
   * and a value less or equal than the given value.
   *
   * @param name the name of the variable
   * @param value the value of the variable
   * @throws NotValidException if the name or value is null
   */
  HistoricCaseInstanceQuery variableValueLessThanOrEqual(String name, Object value);

  /**
   * Only select case instances which have a global variable with the given name
   * and a value like given value.
   *
   * @param name the name of the variable
   * @param value the value of the variable, it can include the wildcard character '%'
   *              to express like-strategy: starts with (string%), ends with (%string),
   *              contains (%string%)
   * @throws NotValidException if the name or value is null
   */
  HistoricCaseInstanceQuery variableValueLike(String name, String value);

  /**
   * Only select case instances which have a global variable
   * with the given name and not matching the given value.
   * The syntax is that of SQL: for example usage: valueNotLike(%value%)
   * @param name the name of the variable
   * @param value the value of the variable, it can include the wildcard character '%'
   *              to express like-strategy: starts with (string%), ends with (%string),
   *              contains (%string%)
   * @throws NotValidException if the name or value is null or a null-value or a boolean-value is used
   */
  HistoricCaseInstanceQuery variableValueNotLike(String name, String value);

  /** Order by the case instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseInstanceQuery orderByCaseInstanceId();

  /** Order by the case definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseInstanceQuery orderByCaseDefinitionId();

  /** Order by the business key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseInstanceQuery orderByCaseInstanceBusinessKey();

  /** Order by the create time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseInstanceQuery orderByCaseInstanceCreateTime();

  /** Order by the close time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseInstanceQuery orderByCaseInstanceCloseTime();

  /** Order by the duration of the case instance (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseInstanceQuery orderByCaseInstanceDuration();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of historic case instances without tenant id is database-specific.
   */
  HistoricCaseInstanceQuery orderByTenantId();

}
