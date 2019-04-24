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
import org.camunda.bpm.engine.impl.cfg.BatchWindowConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * @author Svetlana Dorokhova.
 */
public class DefaultBatchWindowManager implements BatchWindowManager {

  public BatchWindow getPreviousDayBatchWindow(Date date, ProcessEngineConfigurationImpl configuration) {
    Date previousDay = addDays(date, -1);
    return getBatchWindowForDate(previousDay, configuration);
  }

  private BatchWindow getBatchWindowForDate(Date date, ProcessEngineConfigurationImpl configuration) {

    //get configuration for given day of week
    BatchWindowConfiguration batchWindowConfiguration = configuration.getHistoryCleanupBatchWindows().get(dayOfWeek(date));
    if (batchWindowConfiguration == null && configuration.getHistoryCleanupBatchWindowStartTime() != null) {
      batchWindowConfiguration = new BatchWindowConfiguration(configuration.getHistoryCleanupBatchWindowStartTime(), configuration.getHistoryCleanupBatchWindowEndTime());
    }

    if (batchWindowConfiguration == null) {
      return null;
    }

    Date startTime = updateTime(date, batchWindowConfiguration.getStartTimeAsDate());
    Date endTime = updateTime(date, batchWindowConfiguration.getEndTimeAsDate());
    if (!endTime.after(startTime)) {
      endTime = addDays(endTime, 1);
    }

    return new BatchWindow(startTime, endTime);
  }

  private Integer dayOfWeek(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar.get(Calendar.DAY_OF_WEEK);
  }

  @Override
  public BatchWindow getCurrentOrNextBatchWindow(Date date, ProcessEngineConfigurationImpl configuration) {
    final BatchWindow previousDayBatchWindow = getPreviousDayBatchWindow(date, configuration);
    if (previousDayBatchWindow != null && previousDayBatchWindow.isWithin(date)) {
      return previousDayBatchWindow;
    }

    final BatchWindow currentDayBatchWindow = getBatchWindowForDate(date, configuration);
    if (currentDayBatchWindow!= null && (currentDayBatchWindow.isWithin(date) || date.before(currentDayBatchWindow.getStart()))) {
      return currentDayBatchWindow;
    }

    //check next week
    for (int i=1; i<=7; i++ ) {
      Date dateToCheck = addDays(date, i);
      final BatchWindow batchWindowForDate = getBatchWindowForDate(dateToCheck, configuration);
      if (batchWindowForDate != null) {
        return batchWindowForDate;
      }
    }

    return null;
  }

  @Override
  public BatchWindow getNextBatchWindow(Date date, ProcessEngineConfigurationImpl configuration) {
    final BatchWindow currentDayBatchWindow = getBatchWindowForDate(date, configuration);
    if (currentDayBatchWindow != null && date.before(currentDayBatchWindow.getStart())) {
      return currentDayBatchWindow;
    } else {
      //check next week
      for (int i=1; i<=7; i++ ) {
        Date dateToCheck = addDays(date, i);
        final BatchWindow batchWindowForDate = getBatchWindowForDate(dateToCheck, configuration);
        if (batchWindowForDate != null) {
          return batchWindowForDate;
        }
      }
    }
    return null;
  }

  @Override
  public boolean isBatchWindowConfigured(ProcessEngineConfigurationImpl configuration) {
    return configuration.getHistoryCleanupBatchWindowStartTimeAsDate() != null ||
      !configuration.getHistoryCleanupBatchWindows().isEmpty();
  }

  private static Date updateTime(Date now, Date newTime) {
    Calendar c = Calendar.getInstance();
    c.setTime(now);
    Calendar newTimeCalendar = Calendar.getInstance();
    newTimeCalendar.setTime(newTime);
    c.set(Calendar.ZONE_OFFSET, newTimeCalendar.get(Calendar.ZONE_OFFSET));
    c.set(Calendar.DST_OFFSET, newTimeCalendar.get(Calendar.DST_OFFSET));
    c.set(Calendar.HOUR_OF_DAY, newTimeCalendar.get(Calendar.HOUR_OF_DAY));
    c.set(Calendar.MINUTE, newTimeCalendar.get(Calendar.MINUTE));
    c.set(Calendar.SECOND, newTimeCalendar.get(Calendar.SECOND));
    c.set(Calendar.MILLISECOND, newTimeCalendar.get(Calendar.MILLISECOND));
    return c.getTime();
  }

  private static Date addDays(Date date, int amount) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.DATE, amount);
    return c.getTime();
  }

}
