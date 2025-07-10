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

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;

/**
 * A cron timer implementation that uses cronutils library for parsing and evaluation.
 */
public class CronTimer {

  private final Cron cron;
  private int repetitions;

  public CronTimer(final Cron cron) {
    this.cron = cron;
  }

  public int getRepetitions() {
    return repetitions;
  }

  public long getDueDate(final long fromEpochMilli) {
    // set default value to -1
    repetitions = -1;

    final var next =
        ExecutionTime.forCron(cron)
            .nextExecution(
                ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(fromEpochMilli), ZoneId.systemDefault()))
            .map(ZonedDateTime::toInstant)
            .map(Instant::toEpochMilli);

    // set `repetitions` to 0 when the next execution time does not exist
    if (next.isEmpty()) {
      repetitions = 0;
    }

    return next.orElse(fromEpochMilli);
  }

  public static CronTimer parse(final String text) throws ParseException {
    try {
      final var cron =
          new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53))
              .parse(text);
      return new CronTimer(cron);
    } catch (final IllegalArgumentException | NullPointerException ex) {
      throw new ParseException(ex.getMessage(), 0);
    }
  }
}
