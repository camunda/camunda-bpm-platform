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

package org.camunda.bpm.engine.impl;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.query.QueryProperty;

/**
 * Contains the possible properties that can be used in an {@link AuthorizationQuery}.
 * 
 * @author Daniel Meyer
 */
public class AuthorizationQueryProperty implements QueryProperty {
  
  private static final long serialVersionUID = 1L;

  private static final Map<String, AuthorizationQueryProperty> properties = new HashMap<String, AuthorizationQueryProperty>();

  public static final AuthorizationQueryProperty RESOURCE_TYPE = new AuthorizationQueryProperty("RES.RESOURCE_TYPE_");
  public static final AuthorizationQueryProperty RESOURCE_ID = new AuthorizationQueryProperty("RES.RESOURCE_ID_");
  
  private String name;

  public AuthorizationQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }
  
  public static AuthorizationQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
