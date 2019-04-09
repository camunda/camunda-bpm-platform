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
package org.camunda.bpm.engine.rest.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class DateTimeUtils {

  public static final SimpleDateFormat DATE_FORMAT_WITHOUT_TIMEZONE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  public static final SimpleDateFormat DATE_FORMAT_WITH_TIMEZONE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  /**
   * Converts date string without timezone to the one with timezone.
   * @param dateString
   * @return
   */
  public static String withTimezone(String dateString) {
    final Date parse;
    try {
      parse = DATE_FORMAT_WITHOUT_TIMEZONE.parse(dateString);
      return withTimezone(parse);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static String withTimezone(Date date) {
      return DATE_FORMAT_WITH_TIMEZONE.format(date);
  }

  public static Date updateTime(Date now, Date newTime) {
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

  public static Date addDays(Date date, int amount) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.DATE, amount);
    return c.getTime();
  }
}
