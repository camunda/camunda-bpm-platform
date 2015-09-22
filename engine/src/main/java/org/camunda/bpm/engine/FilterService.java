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
package org.camunda.bpm.engine;

import java.util.List;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.query.Query;


/**
 * @author Sebastian Menski
 */
public interface FilterService {

  /**
   * Creates a new task filter.
   *
   * @return a new task filter
   * @throws AuthorizationException if the user has no {@link Permissions#CREATE} permissions on {@link Resources#FILTER}.
   */
  Filter newTaskFilter();

  /**
   * Creates a new task filter with a given name.
   *
   * @return a new task filter with a name
   * @throws AuthorizationException if the user has no {@link Permissions#CREATE} permissions on {@link Resources#FILTER}.
   */
  Filter newTaskFilter(String filterName);

  /**
   * Creates a new filter query
   *
   * @return a new query for filters
   */
  FilterQuery createFilterQuery();


  /**
   * Creates a new task filter query.
   *
   * @return a new query for task filters
   */
  FilterQuery createTaskFilterQuery();

  /**
   * Saves the filter in the database.
   *
   * @param filter the filter to save
   * @return return the saved filter
   * @throws AuthorizationException if the user has no {@link Permissions#CREATE} permissions on {@link Resources#FILTER} (save new filter)
   * or if user has no {@link Permissions#UPDATE} permissions on {@link Resources#FILTER} (update existing filter).
   * @throws BadUserRequestException
   *  <ul><li>When the filter query uses expressions and expression evaluation is deactivated for stored queries.
   *  Expression evaluation can be activated by setting the process engine configuration properties
   *  <code>enableExpressionsInAdhocQueries</code> (default <code>false</code>) and
   *  <code>enableExpressionsInStoredQueries</code> (default <code>true</code>) to <code>true</code>.
   */
  Filter saveFilter(Filter filter);

  /**
   * Returns the filter for the given filter id.
   *
   * @param filterId the id of the filter
   * @return the filter
   * @throws AuthorizationException if the user has no {@link Permissions#READ} permissions on {@link Resources#FILTER}.
   */
  Filter getFilter(String filterId);

  /**
   * Deletes a filter by its id.
   *
   * @param filterId the id of the filter
   * @throws AuthorizationException if the user has no {@link Permissions#DELETE} permissions on {@link Resources#FILTER}.
   */
  void deleteFilter(String filterId);

  /**
   * Executes the query of the filter and returns the result as list.
   *
   * @param filterId the the id of the filter
   * @return the query result as list
   * @throws AuthorizationException if the user has no {@link Permissions#READ} permissions on {@link Resources#FILTER}.
   * @throws BadUserRequestException
   *   <ul><li>When the filter query uses expressions and expression evaluation is deactivated for stored queries.
   *  Expression evaluation can be activated by setting the process engine configuration properties
   *  <code>enableExpressionsInAdhocQueries</code> (default <code>false</code>) and
   *  <code>enableExpressionsInStoredQueries</code> (default <code>true</code>) to <code>true</code>.
   */
  <T> List<T> list(String filterId);

  /**
   * Executes the extended query of a filter and returns the result as list.
   *
   * @param filterId the id of the filter
   * @param extendingQuery additional query to extend the filter query
   * @return the query result as list
   * @throws AuthorizationException if the user has no {@link Permissions#READ} permissions on {@link Resources#FILTER}.
   * @throws BadUserRequestException
   *   <ul><li>When the filter query uses expressions and expression evaluation is deactivated for stored queries.
   *   <li>When the extending query uses expressions and expression evaluation is deactivated for adhoc queries.
   *  Expression evaluation can be activated by setting the process engine configuration properties
   *  <code>enableExpressionsInAdhocQueries</code> (default <code>false</code>) and
   *  <code>enableExpressionsInStoredQueries</code> (default <code>true</code>) to <code>true</code>.
   */
  <T, Q extends Query<?, T>> List<T> list(String filterId, Q extendingQuery);

  /**
   * Executes the query of the filter and returns the result in the given boundaries as list.
   *
   * @param filterId the the id of the filter
   * @param firstResult first result to select
   * @param maxResults maximal number of results
   * @return the query result as list
   * @throws AuthorizationException if the user has no {@link Permissions#READ} permissions on {@link Resources#FILTER}.
   * @throws BadUserRequestException
   *  <ul><li>When the filter query uses expressions and expression evaluation is deactivated for stored queries.
   *  Expression evaluation can be activated by setting the process engine configuration properties
   *  <code>enableExpressionsInAdhocQueries</code> (default <code>false</code>) and
   *  <code>enableExpressionsInStoredQueries</code> (default <code>true</code>) to <code>true</code>.
   */
  <T> List<T> listPage(String filterId, int firstResult, int maxResults);

  /**
   * Executes the extended query of a filter and returns the result in the given boundaries as list.
   *
   * @param extendingQuery additional query to extend the filter query
   * @param filterId the id of the filter
   * @param firstResult first result to select
   * @param maxResults maximal number of results
   * @return the query result as list
   * @throws AuthorizationException if the user has no {@link Permissions#READ} permissions on {@link Resources#FILTER}.
   * @throws BadUserRequestException
   *  <ul><li>When the filter query uses expressions and expression evaluation is deactivated for stored queries.
   *  <li>When the extending query uses expressions and expression evaluation is deactivated for adhoc queries.
   *  Expression evaluation can be activated by setting the process engine configuration properties
   *  <code>enableExpressionsInAdhocQueries</code> (default <code>false</code>) and
   *  <code>enableExpressionsInStoredQueries</code> (default <code>true</code>) to <code>true</code>.
   */
  <T, Q extends Query<?, T>> List<T> listPage(String filterId, Q extendingQuery, int firstResult, int maxResults);

  /**
   * Executes the query of the filter and returns the a single result.
   *
   * @param filterId the the id of the filter
   * @return the single query result
   * @throws AuthorizationException if the user has no {@link Permissions#READ} permissions on {@link Resources#FILTER}.
   * @throws BadUserRequestException
   *  <ul><li>When the filter query uses expressions and expression evaluation is deactivated for stored queries.
   *  Expression evaluation can be activated by setting the process engine configuration properties
   *  <code>enableExpressionsInAdhocQueries</code> (default <code>false</code>) and
   *  <code>enableExpressionsInStoredQueries</code> (default <code>true</code>) to <code>true</code>.
   */
  <T> T singleResult(String filterId);

  /**
   * Executes the extended query of the filter and returns the a single result.
   *
   * @param filterId the the id of the filter
   * @param extendingQuery additional query to extend the filter query
   * @return the single query result
   * @throws AuthorizationException if the user has no {@link Permissions#READ} permissions on {@link Resources#FILTER}.
   * @throws BadUserRequestException
   *  <ul><li>When the filter query uses expressions and expression evaluation is deactivated for stored queries.
   *  <li>When the extending query uses expressions and expression evaluation is deactivated for adhoc queries.
   *  Expression evaluation can be activated by setting the process engine configuration properties
   *  <code>enableExpressionsInAdhocQueries</code> (default <code>false</code>) and
   *  <code>enableExpressionsInStoredQueries</code> (default <code>true</code>) to <code>true</code>.
   */
  <T, Q extends Query<?, T>> T singleResult(String filterId, Q extendingQuery);

  /**
   * Executes the query of the filter and returns the result count.
   *
   * @param filterId the the id of the filter
   * @return the result count
   * @throws AuthorizationException if the user has no {@link Permissions#READ} permissions on {@link Resources#FILTER}.
   * @throws BadUserRequestException
   *  <ul><li>When the filter query uses expressions and expression evaluation is deactivated for stored queries.
   *  Expression evaluation can be activated by setting the process engine configuration properties
   *  <code>enableExpressionsInAdhocQueries</code> (default <code>false</code>) and
   *  <code>enableExpressionsInStoredQueries</code> (default <code>true</code>) to <code>true</code>.
   */
  Long count(String filterId);

  /**
   * Executes the extended query of the filter and returns the result count.
   *
   * @param filterId the the id of the filter
   * @param extendingQuery additional query to extend the filter query
   * @return the result count
   * @throws AuthorizationException if the user has no {@link Permissions#READ} permissions on {@link Resources#FILTER}.
   * @throws BadUserRequestException
   *  <ul><li>When the filter query uses expressions and expression evaluation is deactivated for stored queries.
   *  <li>When the extending query uses expressions and expression evaluation is deactivated for adhoc queries.
   *  Expression evaluation can be activated by setting the process engine configuration properties
   *  <code>enableExpressionsInAdhocQueries</code> (default <code>false</code>) and
   *  <code>enableExpressionsInStoredQueries</code> (default <code>true</code>) to <code>true</code>.
   */
  Long count(String filterId, Query<?, ?> extendingQuery);

}
