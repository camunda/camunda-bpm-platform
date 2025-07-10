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
package org.camunda.bpm.engine.impl.calendar;

import java.text.ParseException;
import java.util.Date;

/**
 * A cron timer implementation that wraps the existing CronExpression functionality
 * and provides an interface similar to the Zeebe CronTimer.
 */
public class CronTimer {

  private final CronExpression cronExpression;
  private int repetitions;

  public CronTimer(final CronExpression cronExpression) {
    this.cronExpression = cronExpression;
  }

  public int getRepetitions() {
    return repetitions;
  }

  public long getDueDate(final long fromEpochMilli) {
    // set default value to -1
    repetitions = -1;

    final Date fromDate = new Date(fromEpochMilli);
    final Date nextExecution = cronExpression.getTimeAfter(fromDate);

    // set `repetitions` to 0 when the next execution time does not exist
    if (nextExecution == null) {
      repetitions = 0;
      return fromEpochMilli;
    }

    return nextExecution.getTime();
  }

  public static CronTimer parse(final String text) throws ParseException {
    try {
      final CronExpression cronExpression = new CronExpression(text);
      return new CronTimer(cronExpression);
    } catch (final ParseException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new ParseException(ex.getMessage(), 0);
    }
  }
}
