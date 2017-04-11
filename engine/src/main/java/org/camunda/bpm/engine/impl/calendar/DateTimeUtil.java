package org.camunda.bpm.engine.impl.calendar;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
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
