/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.filter;


import org.camunda.bpm.engine.query.Query;

/**
 * @author Sebastian Menski
 */
public interface FilterQuery extends Query<FilterQuery, Filter> {

  /**
   * @param filterId set the filter id to query
   * @return this query
   */
  FilterQuery filterId(String filterId);

  /**
   * @param resourceType set the filter resource type to query
   * @return this query
   */
  FilterQuery filterResourceType(String resourceType);

  /**
   * @param name set the filter name to query
   * @return this query
   */
  FilterQuery filterName(String name);

  /**
   * @param nameLike set the filter name like to query
   * @return this query
   */
  FilterQuery filterNameLike(String nameLike);

  /**
   * @param owner set the filter owner to query
   * @return this query
   */
  FilterQuery filterOwner(String owner);

  // ordering ////////////////////////////////////////////////////////////

  /** Order by filter id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  FilterQuery orderByFilterId();

  /** Order by filter id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  FilterQuery orderByFilterResourceType();

  /** Order by filter id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  FilterQuery orderByFilterName();

  /** Order by filter id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  FilterQuery orderByFilterOwner();

}
