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

package org.camunda.bpm.engine.impl.filter;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.query.QueryProperty;

/**
 * @author Sebastian Menski
 */
public class FilterQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, FilterQueryProperty> properties = new HashMap<String, FilterQueryProperty>();

  public static final FilterQueryProperty FILTER_ID = new FilterQueryProperty("RES.ID_");
  public static final FilterQueryProperty RESOURCE_TYPE = new FilterQueryProperty("RES.RESOURCE_TYPE_");
  public static final FilterQueryProperty NAME = new FilterQueryProperty("RES.NAME_");
  public static final FilterQueryProperty OWNER = new FilterQueryProperty("RES.OWNER_");
  public static final FilterQueryProperty QUERY = new FilterQueryProperty("RES.QUERY_");
  public static final FilterQueryProperty PROPERTIES = new FilterQueryProperty("RES.PROPERTIES_");

  private String name;

  public FilterQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }

  public static FilterQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
