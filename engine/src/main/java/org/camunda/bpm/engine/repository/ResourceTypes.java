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
package org.camunda.bpm.engine.repository;

public enum ResourceTypes implements ResourceType {

  REPOSITORY("REPOSITORY", 1),

  RUNTIME("RUNTIME", 2),

  HISTORY("HISTORY", 3);

  // implmentation //////////////////////////

  private String name;
  private Integer id;

  private ResourceTypes(String name, Integer id) {
    this.name = name;
    this.id = id;
  }

  @Override
  public String toString() {
    return name;
  }

  public String getName() {
    return name;
  }

  public Integer getValue() {
    return id;
  }

  public static ResourceType forName(String name) {
    ResourceType type = valueOf(name);
    return type;
  }

}
