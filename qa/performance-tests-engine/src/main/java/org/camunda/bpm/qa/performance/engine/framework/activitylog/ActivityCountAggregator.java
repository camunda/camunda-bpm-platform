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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.camunda.bpm.qa.performance.engine.framework.PerfTestConfiguration;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestResult;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestResults;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultAggregator;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultSet;
import org.camunda.bpm.qa.performance.engine.framework.report.SectionedHtmlReportBuilder;
import org.camunda.bpm.qa.performance.engine.framework.report.SectionedHtmlReportBuilder.TableCell;

public class ActivityCountAggregator extends TabularResultAggregator {

  public static final long INTERVAL = 1000;
  public static final long TIME_UNIT = 1000;
  public static final long INTERVAL_SECONDS = INTERVAL / 1000;

  protected SectionedHtmlReportBuilder htmlBuilder;

  public ActivityCountAggregator(String resultsFolderPath, SectionedHtmlReportBuilder htmlBuilder) {
    super(resultsFolderPath);
    this.htmlBuilder = htmlBuilder;
    sortResults(false);
  }

  protected TabularResultSet createAggregatedResultsInstance() {
    return null;
  }

  protected void processResults(PerfTestResults results, TabularResultSet tabularResultSet) {
    PerfTestConfiguration configuration = results.getConfiguration();
    List<String> watchActivities = configuration.getWatchActivities();

    for (PerfTestResult passResult : results.getPassResults()) {
      String passTitle = getPassTitle(results.getTestName(), configuration, passResult);
      TabularResultSet result = processPassResult(watchActivities, passResult);
      htmlBuilder.addSection(passTitle, result);
    }
  }

  protected String getPassTitle(String testName, PerfTestConfiguration configuration, PerfTestResult passResult) {
    return testName + " (Runs: " + configuration.getNumberOfRuns() + ", Threads: " + passResult.getNumberOfThreads() + ", Duration: " + passResult.getDuration() + " ms)";
  }

  protected TabularResultSet processPassResult(List<String> watchActivities, PerfTestResult passResult) {
    TabularResultSet tabularResultSet = new TabularResultSet();
    addTableHeaders(tabularResultSet, watchActivities);
    addTableBody(tabularResultSet, watchActivities, passResult);
    return tabularResultSet;
  }

  protected void addTableHeaders(TabularResultSet tabularResultSet, List<String> watchActivities) {
    List<Object> row1 = new ArrayList<Object>();
    List<Object> row2 = new ArrayList<Object>();

    row1.add(new TableCell("", true));
    row2.add(new TableCell("seconds", true));
    for (String activity : watchActivities) {
      row1.add(new TableCell(activity, 5, true));
      row2.add(new TableCell("started", true));
      row2.add(new TableCell("&sum; started", true));
      row2.add(new TableCell("ended", true));
      row2.add(new TableCell("&sum; ended", true));
      row2.add(new TableCell("&Oslash; duration", true));
    }

    row1.add(new TableCell("", 2, true));
    row2.add(new TableCell("act/s", true));
    row2.add(new TableCell("&Oslash; act/s", true));

    tabularResultSet.addResultRow(row1);
    tabularResultSet.addResultRow(row2);
  }

  protected void addTableBody(TabularResultSet tabularResultSet, List<String> watchActivities, PerfTestResult passResult) {
    // get first and last timestamp
    Date firstStartTime = null;
    Date lastEndTime = null;

    for (List<ActivityPerfTestResult> activityResults : passResult.getActivityResults().values()) {
      for (ActivityPerfTestResult activityResult : activityResults) {
        if (firstStartTime == null || activityResult.getStartTime().before(firstStartTime)) {
          firstStartTime = activityResult.getStartTime();
        }

        if (lastEndTime == null || activityResult.getEndTime().after(lastEndTime)) {
          lastEndTime = activityResult.getEndTime();
        }
      }
    }

    long firstTimestamp = firstStartTime.getTime();
    long lastTimestamp = lastEndTime.getTime();
    List<Map<String, ActivityCount>> resultTable = new ArrayList<Map<String, ActivityCount>>();

    for (long t = firstTimestamp; t <= lastTimestamp + INTERVAL; t += INTERVAL) {
      Map<String, ActivityCount> activitiesMap = new HashMap<String, ActivityCount>();
      for (String activity : watchActivities) {
        activitiesMap.put(activity, new ActivityCount());
      }
      resultTable.add(activitiesMap);
    }


    for (List<ActivityPerfTestResult> activityResults : passResult.getActivityResults().values()) {
      for (ActivityPerfTestResult activityResult : activityResults) {
        String activityId = activityResult.getActivityId();
        int startSlot = calculateTimeSlot(activityResult.getStartTime(), firstTimestamp);
        int endSlot = calculateTimeSlot(activityResult.getEndTime(), firstTimestamp);
        resultTable.get(startSlot).get(activityId).incrementStarted();
        resultTable.get(endSlot).get(activityId).incrementEnded();
        resultTable.get(endSlot).get(activityId).addDuration(activityResult.getDuration());
      }
    }

    ArrayList<Object> row = null;
    Map<String, ActivityCount> sumMap = new HashMap<String, ActivityCount>();
    for (String activity : watchActivities) {
      sumMap.put(activity, new ActivityCount());
    }

    long sumActivitiesEnded = 0;
    for (int i = 0; i < resultTable.size(); i++) {
      row = new ArrayList<Object>();
      row.add(i * INTERVAL / TIME_UNIT);
      long currentActivitiesEnded = 0;
      for (String activity : watchActivities) {
        sumMap.get(activity).addStarted(resultTable.get(i).get(activity).getStarted());
        sumMap.get(activity).addEnded(resultTable.get(i).get(activity).getEnded());
        sumMap.get(activity).addDuration(resultTable.get(i).get(activity).getDuration());
        currentActivitiesEnded += resultTable.get(i).get(activity).getEnded();
      }
      for (String activity : watchActivities) {
        long started = resultTable.get(i).get(activity).getStarted();
        long ended = resultTable.get(i).get(activity).getEnded();
        double endedFraction = 0;
        long avgDuration = 0;

        if (sumMap.get(activity).getEnded() > 0) {
          avgDuration = sumMap.get(activity).getDuration() / sumMap.get(activity).getEnded();
        }

        if (currentActivitiesEnded > 0) {
          endedFraction = ended * 100.0 / currentActivitiesEnded;
        }

        row.add(started);
        row.add(sumMap.get(activity).getStarted());
        row.add(String.format("%d (%.1f%%)", ended, endedFraction));
        row.add(sumMap.get(activity).getEnded());
        row.add(avgDuration + " ms");
      }
      sumActivitiesEnded += currentActivitiesEnded;
      row.add(currentActivitiesEnded / INTERVAL_SECONDS);
      row.add(sumActivitiesEnded / ((i + 1) * INTERVAL_SECONDS));
      tabularResultSet.addResultRow(row);
    }
  }

  protected int calculateTimeSlot(Date date, long firstTimestamp) {
    return Math.round((date.getTime() - firstTimestamp) / INTERVAL);
  }

  class ActivityCount {
    int started = 0;
    int ended = 0;
    long duration = 0;

    public void incrementStarted() {
      ++started;
    }

    public void addStarted(int started) {
      this.started += started;
    }

    public int getStarted() {
      return started;
    }

    public void incrementEnded() {
      ++ended;
    }

    public void addEnded(int ended) {
      this.ended += ended;
    }

    public int getEnded() {
      return ended;
    }

    public void addDuration(Long duration) {
      if (duration != null) {
        this.duration += duration;
      }
    }

    public long getDuration() {
      return duration;
    }

  }

}
