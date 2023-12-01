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

package org.camunda.bpm.client.task;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class that encapsulates the client's configuration of createTime ordering.
 */
public class OrderingConfig {

  protected final Map<String, Direction> propertyByConfig;

  protected OrderingConfig(Map<String, Direction> configuration) {
    this.propertyByConfig = configuration;
  }

  /**
   * Returns an empty config.
   */
  public static OrderingConfig empty() {
    return new OrderingConfig(new LinkedHashMap<>());
  }

  public void configureField(SortingField field) {
    propertyByConfig.putIfAbsent(field.getName(), null);
  }

  /**
   * Configures the {@link Direction} for the last configured field on this {@link OrderingConfig}.
   * @param direction the given direction, nullable.
   */
  public void configureDirectionOnLastField(Direction direction) {
    String lastField = getLastField();
    propertyByConfig.put(lastField, direction);
  }

  /**
   * Converts this {@link OrderingConfig} to a list of {@link SortingDto}s.
   */
  public List<SortingDto> toSortingDtos() {
    return propertyByConfig.entrySet().stream()
        .map(SortingDto::fromMapEntry)
        .collect(Collectors.toList());
  }

  /**
   * Returns the last configured field in this {@link OrderingConfig}.
   */
  protected String getLastField() {
    String lastElement = null;

    for (String element : propertyByConfig.keySet()) {
      lastElement = element;
    }

    return lastElement;
  }

  /**
   * The field to sort by.
   */
  public enum SortingField {

    CREATE_TIME("createTime");

    private final String name;

    SortingField(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }
  }

  /**
   * The direction of createTime.
   */
  public enum Direction {
    ASC, DESC;

    public String asString() {
      return super.name().toLowerCase();
    }
  }

}