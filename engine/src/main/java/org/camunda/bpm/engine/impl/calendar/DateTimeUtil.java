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

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Utility for {@link DateTime} that uses the JVM timezone
 * for date / time related operations.
 *
 * This is important as the JVM timezone and the system timezone may
 * differ which leads to different behavior in
 * {@link java.text.SimpleDateFormat} (using JVM default timezone) and
 * JODA time (using system default timezone).
 *
 * @author Nico Rehwaldt
 *
 * @see CAM-1170
 */
public class DateTimeUtil {

  private static final DateTimeZone JVM_DEFAULT_DATE_TIME_ZONE = DateTimeZone.forTimeZone(TimeZone.getDefault());

  private static DateTimeFormatter DATE_TIME_FORMATER;

  private static DateTimeFormatter getDataTimeFormater() {
    if (DATE_TIME_FORMATER == null) {
      DATE_TIME_FORMATER = ISODateTimeFormat.dateTimeParser().withZone(JVM_DEFAULT_DATE_TIME_ZONE);
    }

    return DATE_TIME_FORMATER;
  }

  public static DateTime now() {
    return new DateTime(JVM_DEFAULT_DATE_TIME_ZONE);
  }

  public static DateTime parseDateTime(String date) {
    return getDataTimeFormater().parseDateTime(date);
  }

  public static Date parseDate(String date) {
    return parseDateTime(date).toDate();
  }

}
