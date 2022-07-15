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
package org.camunda.commons.logging;

public enum Level {

  ERROR("ERROR"),
  WARN("WARN"),
  INFO("INFO"),
  DEBUG("DEBUG"),
  TRACE("TRACE");

  private final String value;

  Level(final String value) {
    this.value = value;
  }

  /**
   * Parse value or fallback to default level
   *
   * @param value        the value to parse
   * @param defaultLevel the fallback default value
   * @return the parsed log level
   */
  public static Level parse(String value, Level defaultLevel) {
    if (value == null) return defaultLevel;
    try {
      return valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return defaultLevel;
    }
  }

  /**
   * @return the string value of the level
   */
  public String getValue() {
    return value;
  }
}
