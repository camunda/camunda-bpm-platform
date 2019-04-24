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

import java.io.File;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultAggregator;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultSet;
import org.camunda.bpm.qa.performance.engine.framework.report.HtmlReportBuilder;
import org.camunda.bpm.qa.performance.engine.util.CsvUtil;
import org.camunda.bpm.qa.performance.engine.util.FileUtil;
import org.camunda.bpm.qa.performance.engine.util.JsonUtil;

/**
 * @author Daniel Meyer, Ingo Richtsmeier
 *
 */
public class BenchmarkReport {

  public static void main(String[] args) {

    final String resultsFolder = "target"+File.separatorChar+"results";
    final String reportsFolder = "target"+File.separatorChar+"reports";
    
    String longTermBenchmarkResultFilename = System.getProperty("longTermBenchmarkResultFile");
    if (longTermBenchmarkResultFilename != null) {
      File longTermBenchmarkResultFile = new File(longTermBenchmarkResultFilename);
      longTermBenchmarkResultFile.getParentFile().mkdirs();
      if (longTermBenchmarkResultFile.exists()) {
        // Do nothing, append current results later 
      } else {
        FileUtil.appendStringToFile(
            "name;"
            + "number of runs;"
            + "database;"
            + "history level;"
            + "starttime;"
            + "platform;"
            + "number of threads;"
            + "duration;"
            + "throughput", 
            longTermBenchmarkResultFilename);
      }
      TabularResultSet longTermResultTable = 
          new BenchmarkLongtermAggregator(resultsFolder)
              .execute();
      FileUtil.appendStringToFile(
          CsvUtil.resultSetAsCsv(longTermResultTable), 
          longTermBenchmarkResultFilename);
    }

    writeReport(
        resultsFolder,
        reportsFolder,
        "benchmark",
        new BenchmarkAggregator(resultsFolder),
        "Benchmark Duration Report");
  }

  private static void writeReport(String resultsFolder,
      String reportsFolder,
      String benchmarkName,
      TabularResultAggregator aggregator,
      String reportDescription) {

    final String htmlReportFilename = reportsFolder + File.separatorChar + benchmarkName+"-report.html";

    final String jsonReportFilename = benchmarkName+"-report.json";
    final String jsonReportPath = reportsFolder + File.separatorChar + jsonReportFilename;

    final String csvReportFilename = benchmarkName+"-report.csv";
    final String csvReportPath = reportsFolder + File.separatorChar + csvReportFilename;

    // make sure reports folder exists
    File reportsFolderFile = new File(reportsFolder);
    if(!reportsFolderFile.exists()) {
      reportsFolderFile.mkdir();
    }

    TabularResultSet aggregatedResults = aggregator.execute();

    // write Json report
    JsonUtil.writeObjectToFile(jsonReportPath, aggregatedResults);

    // format HTML report
    HtmlReportBuilder reportWriter = new HtmlReportBuilder(aggregatedResults)
      .name(reportDescription)
      .resultDetailsFolder(".."+File.separatorChar+"results"+File.separatorChar)
      .createImageLinks(true)
      .jsonSource(jsonReportFilename)
      .csvSource(csvReportFilename);

    String report = reportWriter.execute();
    FileUtil.writeStringToFile(report, htmlReportFilename);

    // write CSV report
    CsvUtil.saveResultSetToFile(csvReportPath, aggregatedResults);
  }
}
