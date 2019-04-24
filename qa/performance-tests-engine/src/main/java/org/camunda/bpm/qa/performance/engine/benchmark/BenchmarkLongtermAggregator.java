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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.qa.performance.engine.framework.PerfTestConfiguration;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestResult;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestResults;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultAggregator;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultSet;

/**
 * @author Ingo Richtsmeier
 *
 */
public class BenchmarkLongtermAggregator extends TabularResultAggregator {

  public BenchmarkLongtermAggregator(String resultsFolderPath) {
    super(resultsFolderPath);
  }

  @Override
  protected TabularResultSet createAggregatedResultsInstance() {
    return new TabularResultSet();
  }

  @Override
  protected void processResults(PerfTestResults results, TabularResultSet tabularResultSet) {

    for (PerfTestResult passResult : results.getPassResults()) {
      tabularResultSet
        .getResults()
        .add(
            processRow(passResult, results));
    }

  }

  protected List<Object> processRow(PerfTestResult passResult, PerfTestResults results) {
    List<Object> row = new ArrayList<Object>();
    PerfTestConfiguration configuration = results.getConfiguration();
    
    // test name
    row.add(results.getTestName());

    // number of runs
    int numberOfRuns = configuration.getNumberOfRuns();
    row.add(numberOfRuns);
    
    // database
    row.add(configuration.getDatabaseName());
    
    // history level
    row.add(configuration.getHistoryLevel());
    
    // start time
    row.add(configuration.getStartTime());
    
    // platform
    row.add(configuration.getPlatform());
    
    // number of threads
    row.add(passResult.getNumberOfThreads());
    
    // add duration
    long duration = passResult.getDuration();
    row.add(duration);
    
    // throughput
    float numberOfRunsFloat = numberOfRuns;
    float throughput = (numberOfRunsFloat / duration) * 1000;
    row.add(throughput);
    
    return row;
  }

}
