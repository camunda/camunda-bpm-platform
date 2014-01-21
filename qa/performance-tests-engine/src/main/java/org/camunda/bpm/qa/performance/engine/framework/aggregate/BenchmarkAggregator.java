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
package org.camunda.bpm.qa.performance.engine.framework.aggregate;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.qa.performance.engine.framework.PerfTestResult;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestResults;

/**
 * @author Daniel Meyer
 *
 */
public class BenchmarkAggregator extends TabularResultAggregator {

  public static final String TEST_NAME = "Test Name";

  public BenchmarkAggregator(String resultsFolderPath) {
    super(resultsFolderPath);
  }

  protected TabularResultSet createAggrgatedResultsInstance() {
    return new TabularResultSet();
  }

  protected void processResults(PerfTestResults results, TabularResultSet tabularResultSet) {

    List<Object> row = new ArrayList<Object>();
    row.add(results.getTestName());

    for (PerfTestResult passResult : results.getPassResults()) {
      row.add(passResult.getDuration());
    }

    tabularResultSet.getResults().add(row);
  }

  protected void postProcessResultSet(TabularResultSet tabularResultSet) {
    if(tabularResultSet.getResults().size() > 0) {
      int columnSize = tabularResultSet.getResults().get(0).size();

      ArrayList<String> columnNames = new ArrayList<String>();
      columnNames.add(TEST_NAME);
      for (int i = 1; i < columnSize; i++) {
        columnNames.add("T = "+i);
      }

      tabularResultSet.setResultColumnNames(columnNames);
    }

  }

}
