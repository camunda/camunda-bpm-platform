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
package org.camunda.bpm.qa.performance.engine.benchmark;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.qa.performance.engine.framework.PerfTestResult;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestResults;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultAggregator;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultSet;

/**
 * The default benchmark aggregator records the duration
 *
 * @author Daniel Meyer
 *
 */
public class BenchmarkAggregator extends TabularResultAggregator {

  public static final String TEST_NAME = "Test Name";

  public BenchmarkAggregator(String resultsFolderPath) {
    super(resultsFolderPath);
  }

  protected TabularResultSet createAggregatedResultsInstance() {
    return new TabularResultSet();
  }

  protected void processResults(PerfTestResults results, TabularResultSet tabularResultSet) {

    List<Object> row = new ArrayList<Object>();
    row.add(results.getTestName());

    for (PerfTestResult passResult : results.getPassResults()) {
      processRow(row, passResult, results);
    }

    tabularResultSet.getResults().add(row);
  }

  protected void processRow(List<Object> row, PerfTestResult passResult, PerfTestResults results) {
    // add duration
    row.add(passResult.getDuration());

    // add throughput per second
    long duration = passResult.getDuration();
    float numberOfRuns = results.getConfiguration().getNumberOfRuns();
    float throughput = (numberOfRuns / duration) * 1000;
    row.add(throughput);

    // add speedup
    float durationForSequential = 0;
    for (PerfTestResult perfTestResult : results.getPassResults()) {
      if(perfTestResult.getNumberOfThreads() == 1) {
        durationForSequential = perfTestResult.getDuration();
      }
    }
    double speedUp = durationForSequential / passResult.getDuration();
    BigDecimal bigDecimalSpeedUp = new BigDecimal(speedUp);
    bigDecimalSpeedUp.setScale(1, BigDecimal.ROUND_HALF_UP);
    row.add(bigDecimalSpeedUp.doubleValue());
  }

  protected void postProcessResultSet(TabularResultSet tabularResultSet) {
    if(tabularResultSet.getResults().size() > 0) {
      int columnSize = tabularResultSet.getResults().get(0).size();

      ArrayList<String> columnNames = new ArrayList<String>();
      columnNames.add(TEST_NAME);
      for (int i = 1; i < columnSize; i++) {
        if((i-1)%3 == 0) {
          int numberOfThreads = (i/3) + 1;
          columnNames.add("T = "+numberOfThreads);
        } else {
          columnNames.add(" ");
        }
      }

      tabularResultSet.setResultColumnNames(columnNames);
    }

  }

}
