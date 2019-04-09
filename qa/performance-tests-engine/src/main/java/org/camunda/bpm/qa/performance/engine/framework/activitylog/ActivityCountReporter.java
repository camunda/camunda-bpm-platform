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
package org.camunda.bpm.qa.performance.engine.framework.activitylog;

import java.io.File;

import org.camunda.bpm.qa.performance.engine.framework.report.SectionedHtmlReportBuilder;
import org.camunda.bpm.qa.performance.engine.util.FileUtil;

public class ActivityCountReporter {

  public static void main(String[] args) {

    final String resultsFolder = "target"+ File.separatorChar+"results";
    final String reportsFolder = "target"+File.separatorChar+"reports";

    final String htmlReportFilename = reportsFolder + File.separatorChar + "activity-count-report.html";

    // make sure reports folder exists
    File reportsFolderFile = new File(reportsFolder);
    if(!reportsFolderFile.exists()) {
      reportsFolderFile.mkdir();
    }

    SectionedHtmlReportBuilder htmlBuilder = new SectionedHtmlReportBuilder("Activity Count Report");

    ActivityCountAggregator activityCountAggregator = new ActivityCountAggregator(resultsFolder, htmlBuilder);
    activityCountAggregator.execute();

    String report = htmlBuilder.execute();
    FileUtil.writeStringToFile(report, htmlReportFilename);
  }

}
