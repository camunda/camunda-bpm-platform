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
package org.camunda.bpm.engine.query;

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * Describes basic methods for querying.
 *
 * @author Frederik Heremans
 */
public interface Query<T extends Query< ? , ? >, U extends Object> {

  /**
   * Order the results ascending on the given property as defined in this
   * class (needs to come after a call to one of the orderByXxxx methods).
   */
  T asc();

  /**
   * Order the results descending on the given property as defined in this
   * class (needs to come after a call to one of the orderByXxxx methods).
   */
  T desc();

  /** Executes the query and returns the number of results */
  long count();

  /**
   * Executes the query and returns the resulting entity or null if no
   * entity matches the query criteria.
   * @throws ProcessEngineException when the query results in more than one
   * entities.
   */
  U singleResult();

  /**
   * Executes the query and get a list of entities as the result.
   *
   * @return a list of results
   * @throws BadUserRequestException
   *   When a maximum results limit is specified. A maximum results limit can be specified with
   *   the process engine configuration property <code>queryMaxResultsLimit</code> (default
   *   {@link Integer#MAX_VALUE}).
   *   Please use {@link #listPage(int, int)} instead.
   */
  List<U> list();

  /**
   * Executes the query. No limitation checks are performed (e. g. query limit).
   *
   * @return a list of results
   */
  List<U> unlimitedList();

  /**
   * Executes the query and get a list of entities as the result.
   *
   * @param firstResult the index of the first result
   * @param maxResults the maximum number of results
   * @return a list of results
   * @throws BadUserRequestException
   *   When {@param maxResults} exceeds the maximum results limit. A maximum results limit can
   *   be specified with the process engine configuration property <code>queryMaxResultsLimit</code>
   *   (default {@link Integer#MAX_VALUE}).
   */
  List<U> listPage(int firstResult, int maxResults);

}
