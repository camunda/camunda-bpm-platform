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
package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Calendar;
import java.util.Date;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import com.google.gson.JsonObject;
import org.camunda.bpm.engine.impl.util.JsonUtil;

/**
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupJobHandlerConfiguration implements JobHandlerConfiguration {

  public final static int START_DELAY = 10;  //10 seconds
  public final static int MAX_DELAY = 60*60;  //hour

  public static final String JOB_CONFIG_COUNT_EMPTY_RUNS = "countEmptyRuns";
  public static final String JOB_CONFIG_EXECUTE_AT_ONCE = "immediatelyDue";
  public static final String JOB_CONFIG_MINUTE_FROM = "minuteFrom";
  public static final String JOB_CONFIG_MINUTE_TO = "minuteTo";

  /**
   * Counts runs without data. Is used within batch window to calculate the delay between two job runs in case no data for cleanup was found.
   */
  private int countEmptyRuns = 0;

  /**
   * Indicated that the job was triggered manually and must be executed at once without waiting for batch window start time.
   */
  private boolean immediatelyDue;

  /**
   * Process definition id.
   */
  private int minuteFrom = 0;

  private int minuteTo = 59;

  public HistoryCleanupJobHandlerConfiguration() {
  }

  @Override
  public String toCanonicalString() {
    JsonObject json = JsonUtil.createObject();
    JsonUtil.addField(json, JOB_CONFIG_COUNT_EMPTY_RUNS, countEmptyRuns);
    JsonUtil.addField(json, JOB_CONFIG_EXECUTE_AT_ONCE, immediatelyDue);
    JsonUtil.addField(json, JOB_CONFIG_MINUTE_FROM, minuteFrom);
    JsonUtil.addField(json, JOB_CONFIG_MINUTE_TO, minuteTo);
    return json.toString();
  }

  public static HistoryCleanupJobHandlerConfiguration fromJson(JsonObject jsonObject) {
    HistoryCleanupJobHandlerConfiguration config = new HistoryCleanupJobHandlerConfiguration();
    if (jsonObject.has(JOB_CONFIG_COUNT_EMPTY_RUNS)) {
      config.setCountEmptyRuns(JsonUtil.getInt(jsonObject, JOB_CONFIG_COUNT_EMPTY_RUNS));
    }
    if (jsonObject.has(JOB_CONFIG_EXECUTE_AT_ONCE)) {
      config.setImmediatelyDue(JsonUtil.getBoolean(jsonObject, JOB_CONFIG_EXECUTE_AT_ONCE));
    }
    config.setMinuteFrom(JsonUtil.getInt(jsonObject, JOB_CONFIG_MINUTE_FROM));
    config.setMinuteTo(JsonUtil.getInt(jsonObject, JOB_CONFIG_MINUTE_TO));
    return config;
  }

  /**
   * The delay between two "empty" runs increases twice each time until it reaches {@link HistoryCleanupJobHandlerConfiguration#MAX_DELAY} value.
   * @param date date to count delay from
   * @return date with delay
   */
  public Date getNextRunWithDelay(Date date) {
    Date result = addSeconds(date, Math.min((int)(Math.pow(2., (double)countEmptyRuns) * START_DELAY), MAX_DELAY));
    return result;
  }

  private Date addSeconds(Date date, int amount) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.SECOND, amount);
    return c.getTime();
  }

  public int getCountEmptyRuns() {
    return countEmptyRuns;
  }

  public void setCountEmptyRuns(int countEmptyRuns) {
    this.countEmptyRuns = countEmptyRuns;
  }

  public boolean isImmediatelyDue() {
    return immediatelyDue;
  }

  public void setImmediatelyDue(boolean immediatelyDue) {
    this.immediatelyDue = immediatelyDue;
  }

  public int getMinuteFrom() {
    return minuteFrom;
  }

  public void setMinuteFrom(int minuteFrom) {
    this.minuteFrom = minuteFrom;
  }

  public int getMinuteTo() {
    return minuteTo;
  }

  public void setMinuteTo(int minuteTo) {
    this.minuteTo = minuteTo;
  }
}

