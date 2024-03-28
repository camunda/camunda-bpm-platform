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
package org.camunda.bpm.engine.impl.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.management.DatabaseContentReport;

public class GetDatabaseCountsCmd implements Command<DatabaseContentReport> {

  private static final String EMPTY_STRING = "";
  private static final String TABLE_NAME = "tableName";
  private static final String SELECT_TABLE_COUNT = "selectTableCount";

  @Override
  public DatabaseContentReport execute(CommandContext commandContext) {
    DatabaseContentReport contentReport = new DatabaseContentReport();
    DbEntityManager dbEntityManager = commandContext.getDbEntityManager();

    List<String> tablesNames = dbEntityManager.getTableNamesPresentInDatabase();
    String databaseTablePrefix = commandContext.getProcessEngineConfiguration().getDatabaseTablePrefix().trim();

    for (String tableName : tablesNames) {
      String tableNameWithoutPrefix = tableName.replace(databaseTablePrefix, EMPTY_STRING);
      Long count = (Long) dbEntityManager.selectOne(SELECT_TABLE_COUNT, mapOf(TABLE_NAME, tableName));

      contentReport.addTableCountEntry(tableNameWithoutPrefix, count);
    }

    return contentReport;
  }

  protected Map<String, String> mapOf(String key, String value) {
    Map<String, String> result = new HashMap<>();
    result.put(key, value);
    return result;
  }

}
