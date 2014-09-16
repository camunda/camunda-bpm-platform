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

import java.util.Map;
import org.camunda.bpm.engine.query.Query;

/**
 * @author Sebastian Menski
 */
public interface Filter {

  /**
   * @return the id of the filer
   */
  String getId();

  /**
   * @return the resource type fo the filter
   */
  String getResourceType();

  /**
   * @param resourceType the resource type of the filter
   * @return this filter
   */
  Filter setResourceType(String resourceType);

  /**
   * @return the name of the filter
   */
  String getName();

  /**
   * @param name the name of the filter
   * @return this filter
   */
  Filter setName(String name);

  /**
   * @return the owner of the filter
   */
  String getOwner();

  /**
   * @param owner the owner of the filter
   * @return this filter
   */
  Filter setOwner(String owner);

  /**
   * @return the saved query of the filter as JSON string
   */
  String getQuery();

  /**
   * @return the saved query as query object
   */
  <T extends Query<?, ?>> T getTypeQuery();

  /**
   * @param query the saved query as JSON string
   * @return this filter
   */
  Filter setQuery(String query);

  /**
   * @param query the saved query as query object
   * @return this filter
   */
  <T extends Query<?, ?>> Filter setQuery(T query);

  /**
   * Extends the query with the additional query. The query of the filter is therefore modified
   * and if the filter is saved the query is updated.
   *
   * @param extendingQuery the query to extend the filter with
   * @return a copy of this filter with the extended query
   */
  <T extends Query<?, ?>> Filter extend(T extendingQuery);

  /**
   * Extends the query with the additional query. The query of the filter is therefore modified
   * and if the filter is saved the query is updated.
   *
   * @param extendingQuery the query to extend the filter with
   * @return a copy of this filter with the extended query
   */
  Filter extend(String extendingQuery);

  /**
   * @return the properties of the filter as JSON string
   */
  String getProperties();

  /**
   * @return the properties as map
   */
  Map<String, Object> getPropertiesMap();

  /**
   * @param properties the properties to set as JSON string
   * @return this filter
   */
  Filter setProperties(String properties);

  /**
   * @param properties the properties to set as map
   * @return this filter
   */
  Filter setProperties(Map<String, Object> properties);

}
