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
package org.camunda.bpm.engine.impl.util;

import java.util.Arrays;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;

public class DatabaseUtil {

  /**
   * Checks if the currently used database is of a given database type.
   *
   * @param databaseTypes to check for
   * @return true if any of the provided types match the used one. Otherwise false.
   */
  public static boolean checkDatabaseType(String... databaseTypes) {
    return checkDatabaseType(Context.getCommandContext().getProcessEngineConfiguration(), databaseTypes);
  }

  /**
   * Checks if the currently used database is of a given database type.
   *
   * @param configuration for the Process Engine, when a Context is not available
   * @param databaseTypes to check for
   * @return true if any of the provided types match the used one. Otherwise false.
   */
  public static boolean checkDatabaseType(ProcessEngineConfigurationImpl configuration, String... databaseTypes) {
    String dbType = configuration.getDatabaseType();
    return Arrays.stream(databaseTypes).anyMatch(dbType::equals);
  }
}