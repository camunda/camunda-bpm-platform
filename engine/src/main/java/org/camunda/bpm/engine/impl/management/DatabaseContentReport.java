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
package org.camunda.bpm.engine.impl.management;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DatabaseContentReport {

  public static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
      "ACT_GE_PROPERTY",
      "ACT_GE_SCHEMA_LOG"
      );

  Map<String, Long> databaseTableCounts = new HashMap<>();

  public void addDatabaseInformation(String tableName, Long entityCount) {
    databaseTableCounts.put(tableName, entityCount);
  }

  public Map<String, Long> getReport() {
    return databaseTableCounts;
  }

  /**
   * Returns the database table counts excluding tables listed in TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.
   */
  public Map<String, Long> getReportExcludingIgnoredTables() {
    return databaseTableCounts.entrySet().stream()
        .filter(d -> !TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(d.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Returns database table counts only for tables with content including tables listed in TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.
   */
  public Map<String, Long> getReportOnlyIncludingDirtyTables(){
    return databaseTableCounts.entrySet().stream()
        .filter(d -> d.getValue() > 0)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Checks if the database is clean.
   *
   * @param ignoreExcludedTables if true, the content from tables listed in TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK is ignored.
   */
  public boolean isDatabaseClean(boolean ignoreExcludedTables) {
    for (Entry<String, Long> table : databaseTableCounts.entrySet()) {
      if (!(ignoreExcludedTables && TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(table.getKey()))) {
        if (table.getValue() > 0) {
          return false;
        }
      }
    }
    return true;
  }
}
