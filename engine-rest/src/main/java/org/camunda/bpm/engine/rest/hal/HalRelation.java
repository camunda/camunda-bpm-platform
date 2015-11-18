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
package org.camunda.bpm.engine.rest.hal;

import javax.ws.rs.core.UriBuilder;

/**
 * @author Daniel Meyer
 *
 */
public class HalRelation {

  /** the name of the relation */
  protected String relName;

  /** the url template used by the relation to construct links */
  protected UriBuilder uriTemplate;

  /** the type of the resource we build a relation to. */
  protected Class<?> resourceType;

  /**
   * Build a relation to a resource.
   *
   * @param relName the name of the relation.
   * @param resourceType the type of the resource
   * @return the relation
   */
  public static HalRelation build(String relName, Class<?> resourceType, UriBuilder urlTemplate) {
    HalRelation relation = new HalRelation();
    relation.relName = relName;
    relation.uriTemplate = urlTemplate;
    relation.resourceType = resourceType;
    return relation;
  }

  public String getRelName() {
    return relName;
  }

  public UriBuilder getUriTemplate() {
    return uriTemplate;
  }

  public Class<?> getResourceType() {
    return resourceType;
  }

}
