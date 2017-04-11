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
package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.joda.time.LocalTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupJobHandlerConfiguration implements JobHandlerConfiguration {

  private final static int MAX_BATCH_SIZE = 500;

  public final static int START_DELAY = 10;  //10 seconds
  public final static int MAX_DELAY = 60*60;  //hour

  public static final String JOB_CONFIG_BATCH_WINDOW_START_TIME = "batchWindowStartTime";
  public static final String JOB_CONFIG_BATCH_WINDOW_END_TIME = "batchWindowEndTime";
  public static final String JOB_CONFIG_BATCH_SIZE_THRESHOLD = "batchSizeThreshold";
  public static final String JOB_CONFIG_BATCH_SIZE = "batchSize";
  public static final String JOB_CONFIG_COUNT_EMPTY_RUNS = "countEmptyRuns";
  public static final String JOB_CONFIG_EXECUTE_AT_ONCE = "executeAtOnce";

  private static final SimpleDateFormat TIME_FORMAT_WITHOUT_SECONDS = new SimpleDateFormat("HH:mm");

  /**
   * Batch window start time.
   * Only time fields are meaningful, other fields (date, month, year) are being ignored
   */
  private Date batchWindowStartTime;

  /**
   * Batch window end time.
   * Only time fields are meaningful, other fields (date, month, year) are being ignored
   */
  private Date batchWindowEndTime;

  private int batchSize = MAX_BATCH_SIZE;

  /**
   * Indicates the minimal amount of data to trigger the cleanup. "data count is less than threshold" = "nothing to delete".
   */
  private int batchSizeThreshold;

  /**
   * Counts runs without data. Is used within batch window to calculate the delay between two job runs in case no data for cleanup was found.
   */
  private int countEmptyRuns = 0;

  /**
   * Indicated that the job was triggered manually and must be executed at once without waiting for batch window start time.
   */
  private boolean executeAtOnce;

  public HistoryCleanupJobHandlerConfiguration() {
  }

  public HistoryCleanupJobHandlerConfiguration(Date batchWindowStartTime, Date batchWindowEndTime, Integer historyCleanupBatchSize) {
    this.batchWindowStartTime = batchWindowStartTime;
    this.batchWindowEndTime = batchWindowEndTime;
    this.batchSize = Math.min(historyCleanupBatchSize, MAX_BATCH_SIZE);
  }

  public Date getBatchWindowStartTime() {
    return batchWindowStartTime;
  }

  public void setBatchWindowStartTime(Date batchWindowStartTime) {
    this.batchWindowStartTime = batchWindowStartTime;
  }

  public Date getBatchWindowEndTime() {
    return batchWindowEndTime;
  }

  public void setBatchWindowEndTime(Date batchWindowEndTime) {
    this.batchWindowEndTime = batchWindowEndTime;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  @Override
  public String toCanonicalString() {
    JSONObject json = new JSONObject();

    JsonUtil.addDateField(json, JOB_CONFIG_BATCH_WINDOW_START_TIME, batchWindowStartTime);
    JsonUtil.addDateField(json, JOB_CONFIG_BATCH_WINDOW_END_TIME, batchWindowEndTime);
    json.put(JOB_CONFIG_BATCH_SIZE_THRESHOLD, batchSizeThreshold);
    json.put(JOB_CONFIG_BATCH_SIZE, batchSize);
    json.put(JOB_CONFIG_COUNT_EMPTY_RUNS, countEmptyRuns);
    json.put(JOB_CONFIG_EXECUTE_AT_ONCE, executeAtOnce);

    return json.toString();
  }

  public static HistoryCleanupJobHandlerConfiguration fromJson(JSONObject jsonObject) {
    HistoryCleanupJobHandlerConfiguration config = new HistoryCleanupJobHandlerConfiguration();
    if (jsonObject.has(JOB_CONFIG_BATCH_WINDOW_START_TIME)) {
      config.setBatchWindowStartTime(JsonUtil.getDateField(jsonObject, JOB_CONFIG_BATCH_WINDOW_START_TIME));
    }
    if (jsonObject.has(JOB_CONFIG_BATCH_WINDOW_END_TIME)) {
      config.setBatchWindowEndTime(JsonUtil.getDateField(jsonObject, JOB_CONFIG_BATCH_WINDOW_END_TIME));
    }
    if (jsonObject.has(JOB_CONFIG_BATCH_SIZE_THRESHOLD)) {
      config.setBatchSizeThreshold(jsonObject.getInt(JOB_CONFIG_BATCH_SIZE_THRESHOLD));
    }
    if (jsonObject.has(JOB_CONFIG_BATCH_SIZE)) {
      config.setBatchSize(jsonObject.getInt(JOB_CONFIG_BATCH_SIZE));
    }
    if (jsonObject.has(JOB_CONFIG_COUNT_EMPTY_RUNS)) {
      config.setCountEmptyRuns(jsonObject.getInt(JOB_CONFIG_COUNT_EMPTY_RUNS));
    }
    if (jsonObject.has(JOB_CONFIG_EXECUTE_AT_ONCE)) {
      config.setExecuteAtOnce(jsonObject.getBoolean(JOB_CONFIG_EXECUTE_AT_ONCE));
    }
    return config;
  }

  public Date getNextRunWithinBatchWindow(Date date) {
    if (isBatchWindowConfigured()) {
      Date todayPossibleRun = updateTime(date, batchWindowStartTime);
      if (todayPossibleRun.after(date)) {
        return todayPossibleRun;
      } else {
        //tomorrow
        return addDays(todayPossibleRun, 1);
      }
    } else {
      //TODO svt
      return null;
    }
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

  private Date updateTime(Date now, Date newTime) {
    Calendar c = Calendar.getInstance();
    c.setTime(now);
    Calendar newTimeCalendar = Calendar.getInstance();
    newTimeCalendar.setTime(newTime);
    c.set(Calendar.HOUR_OF_DAY, newTimeCalendar.get(Calendar.HOUR_OF_DAY));
    c.set(Calendar.MINUTE, newTimeCalendar.get(Calendar.MINUTE));
    c.set(Calendar.SECOND, newTimeCalendar.get(Calendar.SECOND));
    c.set(Calendar.MILLISECOND, newTimeCalendar.get(Calendar.MILLISECOND));
    return c.getTime();
  }

  private Date addDays(Date date, int amount) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.DATE, amount);
    return c.getTime();
  }

  private Date addSeconds(Date date, int amount) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.SECOND, amount);
    return c.getTime();
  }

  /**
   * Checks if given date is within a batch window. Batch window start time is checked inclusively.
   * @param date
   * @return
   */
  public boolean isWithinBatchWindow(Date date) {
    if (isBatchWindowConfigured()) {
      Date todaysBatchWindowStartTime = updateTime(date, batchWindowStartTime);
      Date todaysBatchWindowEndTime = updateTime(date, batchWindowEndTime);
      if (todaysBatchWindowEndTime.after(todaysBatchWindowStartTime)) {
        //interval is within one day
        return (date.after(todaysBatchWindowStartTime) || date.equals(todaysBatchWindowStartTime)) && date.before(todaysBatchWindowEndTime);
      } else {
        return date.after(todaysBatchWindowStartTime) || date.equals(todaysBatchWindowStartTime) || date.before(todaysBatchWindowEndTime);
      }
    } else {
      return false;
    }
  }

  public boolean isBatchWindowConfigured() {
    return batchWindowStartTime != null;
  }

  public int getCountEmptyRuns() {
    return countEmptyRuns;
  }

  public void setCountEmptyRuns(int countEmptyRuns) {
    this.countEmptyRuns = countEmptyRuns;
  }

  public int getBatchSizeThreshold() {
    return batchSizeThreshold;
  }

  public void setBatchSizeThreshold(int batchSizeThreshold) {
    this.batchSizeThreshold = batchSizeThreshold;
  }

  public boolean isExecuteAtOnce() {
    return executeAtOnce;
  }

  public void setExecuteAtOnce(boolean executeAtOnce) {
    this.executeAtOnce = executeAtOnce;
  }

  public static Date parseTimeConfiguration(String time) throws ParseException {
    return TIME_FORMAT_WITHOUT_SECONDS.parse(time);
  }
}

