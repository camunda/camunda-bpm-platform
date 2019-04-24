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
package org.camunda.bpm.qa.performance.engine.framework;

import static org.camunda.bpm.qa.performance.engine.framework.activitylog.ActivityPerfTestWatcher.WATCH_ALL_ACTIVITIES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;


/**
 * Configuration of a performance test
 *
 * @author Daniel Meyer, Ingo Richtsmeier
 *
 */
public class PerfTestConfiguration {

  protected int numberOfThreads = 2;
  protected int numberOfRuns = 1000;
  protected String databaseName = "";

  protected String testWatchers = null;
  protected String historyLevel;

  protected List<String> watchActivities = null;

  protected Date startTime;

  protected String platform;

  public PerfTestConfiguration(Properties properties) {
    numberOfRuns = Integer.parseInt(properties.getProperty("numberOfRuns"));
    numberOfThreads =  Integer.parseInt(properties.getProperty("numberOfThreads"));
    testWatchers = properties.getProperty("testWatchers", null);
    databaseName = properties.getProperty("databaseDriver", null);
    historyLevel = properties.getProperty("historyLevel");
    watchActivities = parseWatchActivities(properties.getProperty("watchActivities", null));
  }

  public PerfTestConfiguration() {
  }

  public int getNumberOfThreads() {
    return numberOfThreads;
  }

  public void setNumberOfThreads(int numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
  }

  public int getNumberOfRuns() {
    return numberOfRuns;
  }

  public void setNumberOfRuns(int numberOfExecutions) {
    this.numberOfRuns = numberOfExecutions;
  }

  public String getTestWatchers() {
    return testWatchers;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setTestWatchers(String testWatchers) {
    this.testWatchers = testWatchers;
  }

  public String getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(String historyLevel) {
    this.historyLevel = historyLevel;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public List<String> getWatchActivities() {
    return watchActivities;
  }

  public void setWatchActivities(List<String> watchActivities) {
    this.watchActivities = watchActivities;
  }

  protected List<String> parseWatchActivities(String watchActivitiesString) {
    if (watchActivitiesString == null || watchActivitiesString.trim().isEmpty()) {
      return null;
    }
    else if (watchActivitiesString.trim().equalsIgnoreCase(WATCH_ALL_ACTIVITIES.get(0))) {
      return WATCH_ALL_ACTIVITIES;
    }
    else {
      List<String> watchActivities = new ArrayList<String>();
      String[] parts = watchActivitiesString.split(",");
      for (String part : parts) {
        part = part.trim();
        if (!part.isEmpty()) {
          watchActivities.add(part);
        }
      }
      return Collections.unmodifiableList(watchActivities);
    }
  }
}
