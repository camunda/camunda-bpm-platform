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

package org.camunda.bpm.engine.rest.cache;

public interface Cache {

  /**
   * Put a resource into the cache.
   *
   * @param id the id of the resource
   * @param resource the resource to cache
   */
  void put(String id, Object resource);

  /**
   * Get a resource by id.
   *
   * @param id the id of the resource
   * @return the resource or null if non is found or the resource time to live expired
   */
  Object get(String id);

  /**
   * Destroy cache.
   */
  void destroy();

}
