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
package org.camunda.bpm.qa.performance.engine.sqlstatementlog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.camunda.bpm.qa.performance.engine.framework.PerfTestResults;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestStepResult;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultAggregator;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultSet;
import org.camunda.bpm.qa.performance.engine.sqlstatementlog.StatementLogSqlSession.SqlStatementType;

/**
 * Aggregates the results from a Sql Statement Test run.
 *
 * This aggregator will count the statement types for each {@link SqlStatementType}
 * and add the counts to the resultset.
 *
 * @author Daniel Meyer
 *
 */
public class SqlStatementCountAggregator extends TabularResultAggregator {

  public static final String TEST_NAME = "Test Name";
  public static final String INSERTS = "Inserts";
  public static final String DELETES = "Deletes";
  public static final String UPDATES = "Updates";
  public static final String SELECTS = "Selects";

  public SqlStatementCountAggregator(String resultsFolderPath) {
    super(resultsFolderPath);
  }

  protected TabularResultSet createAggregatedResultsInstance() {
    TabularResultSet tabularResultSet = new TabularResultSet();

    List<String> resultColumnNames = tabularResultSet.getResultColumnNames();
    resultColumnNames.add(TEST_NAME);
    resultColumnNames.add(INSERTS);
    resultColumnNames.add(DELETES);
    resultColumnNames.add(UPDATES);
    resultColumnNames.add(SELECTS);

    return tabularResultSet;
  }

  @SuppressWarnings("unchecked")
  protected void processResults(PerfTestResults results, TabularResultSet tabularResultSet) {
    ArrayList<Object> row = new ArrayList<Object>();

    row.add(results.getTestName());

    int insertCount = 0;
    int deleteCount = 0;
    int updateCount = 0;
    int selectCount = 0;

    if(results.getPassResults().isEmpty()) {
      return;
    }

    List<PerfTestStepResult> stepResults = results.getPassResults().get(0).getStepResults();
    for (PerfTestStepResult stepResult : stepResults) {
      List<LinkedHashMap<String, String>> statementLogs = (List<LinkedHashMap<String, String>>) stepResult.getResultData();
      for (LinkedHashMap<String, String> statementLog : statementLogs) {
        String type = statementLog.get("statementType");
        SqlStatementType statementType = SqlStatementType.valueOf(type);

        switch (statementType) {
        case DELETE:
          deleteCount++;
          break;
        case INSERT:
          insertCount++;
          break;
        case UPDATE:
          updateCount++;
          break;
        default:
          selectCount++;
          break;
        }
      }
    }

    row.add(insertCount);
    row.add(deleteCount);
    row.add(updateCount);
    row.add(selectCount);

    tabularResultSet.addResultRow(row);
  }

}
