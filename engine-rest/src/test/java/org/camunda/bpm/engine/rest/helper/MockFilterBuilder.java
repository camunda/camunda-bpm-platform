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

package org.camunda.bpm.engine.rest.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.query.Query;

/**
 * @author Sebastian Menski
 */
public class MockFilterBuilder {

  protected String id;
  protected String resourceType;
  protected String name;
  protected String owner;
  protected Query query;
  protected Map<String, Object> properties;

  public MockFilterBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockFilterBuilder resourceType(String resourceType) {
    this.resourceType = resourceType;
    return this;
  }

  public MockFilterBuilder name(String name) {
    this.name = name;
    return this;
  }

  public MockFilterBuilder owner(String owner) {
    this.owner = owner;
    return this;
  }

  public MockFilterBuilder query(Query<?, ?> query) {
    this.query = query;
    return this;
  }

  public MockFilterBuilder properties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  @SuppressWarnings("unchecked")
  public Filter build() {
    Filter filter = mock(Filter.class);
    when(filter.getId()).thenReturn(id);
    when(filter.getResourceType()).thenReturn(resourceType);
    when(filter.getName()).thenReturn(name);
    when(filter.getOwner()).thenReturn(owner);
    when(filter.getQuery()).thenReturn(query);
    when(filter.getProperties()).thenReturn(properties);
    return filter;
  }

}
