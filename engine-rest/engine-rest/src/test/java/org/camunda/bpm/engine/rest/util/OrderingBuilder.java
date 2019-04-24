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
package org.camunda.bpm.engine.rest.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 *
 */
public class OrderingBuilder {

  protected List<Map<String, Object>> orderings = new ArrayList<Map<String, Object>>();
  protected Map<String, Object> currentOrdering;

  public static OrderingBuilder create() {
    return new OrderingBuilder();
  }

  public OrderingBuilder orderBy(String property) {
    currentOrdering = new HashMap<String, Object>();
    orderings.add(currentOrdering);
    currentOrdering.put("sortBy", property);
    return this;
  }

  public OrderingBuilder desc() {
    currentOrdering.put("sortOrder", "desc");
    return this;
  }

  public OrderingBuilder asc() {
    currentOrdering.put("sortOrder", "asc");
    return this;
  }

  @SuppressWarnings("unchecked")
  public OrderingBuilder parameter(String key, Object value) {
    Map<String, Object> parameters = (Map<String, Object>) currentOrdering.get("parameters");

    if (parameters == null) {
      parameters = new HashMap<String, Object>();
      currentOrdering.put("parameters", parameters);
    }

    parameters.put(key, value);
    return this;
  }

  public List<Map<String, Object>> getJson() {
    return orderings;
  }
}
