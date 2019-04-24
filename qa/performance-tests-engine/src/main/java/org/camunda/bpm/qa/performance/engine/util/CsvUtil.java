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
package org.camunda.bpm.qa.performance.engine.util;

import java.util.List;

import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultSet;

/**
 * <p>Provides export functionality for exporting a {@link TabularResultSet}
 * to CSV (Comma Separated Values).</p>
 *
 * @author Daniel Meyer
 *
 */
public class CsvUtil {

  public static String resultSetAsCsv(TabularResultSet resultSet) {
    return resultSetAsCsv(resultSet, true);
  }
  
  public static String resultSetAsCsvLine(TabularResultSet resultSet) {
    return resultSetAsCsv(resultSet, false);
  }

  private static String resultSetAsCsv(TabularResultSet resultSet, boolean writeHeadline) {
    StringBuilder builder = new StringBuilder();
  
    if (writeHeadline) {
      // write headline
      List<String> resultColumnNames = resultSet.getResultColumnNames();
      for (int i = 0; i < resultColumnNames.size(); i++) {
        builder.append(resultColumnNames.get(i));
        builder.append(";");
      }
      builder.append("\n");
    }

    // write results
    List<List<Object>> results = resultSet.getResults();
    for (List<Object> row : results) {
      for (Object object : row) {
        builder.append(String.valueOf(object));
        builder.append(";");
      }
      builder.append("\n");
    }

    return builder.toString();
  }

  public static void saveResultSetToFile(String fileName, TabularResultSet resultSet) {
    FileUtil.writeStringToFile(resultSetAsCsv(resultSet), fileName);
  }

}
