package org.camunda.feel.integration

import java.time._

import org.camunda.feel.interpreter._
import org.camunda.feel.spi._

class CamundaValueMapper extends CustomValueMapper {

  override def toVal(x: Any, innerValueMapper: Any => Val): Option[Val] =
    x match {
      // joda-time
      case x: org.joda.time.LocalDate =>
        Some(
          ValDate(LocalDate.of(x.getYear, x.getMonthOfYear, x.getDayOfMonth)))

      case x: org.joda.time.LocalTime =>
        Some(ValLocalTime(
          LocalTime.of(x.getHourOfDay, x.getMinuteOfHour, x.getSecondOfMinute)))

      case x: org.joda.time.LocalDateTime =>
        Some(
          ValLocalDateTime(
            LocalDateTime.of(x.getYear,
                             x.getMonthOfYear,
                             x.getDayOfMonth,
                             x.getHourOfDay,
                             x.getMinuteOfHour,
                             x.getSecondOfMinute)))

      case x: org.joda.time.DateTime =>
        Some(
          ValDateTime(
            ZonedDateTime.of(LocalDateTime.of(x.getYear,
                                              x.getMonthOfYear,
                                              x.getDayOfMonth,
                                              x.getHourOfDay,
                                              x.getMinuteOfHour,
                                              x.getSecondOfMinute),
                             ZoneId.of(x.getZone.getID))
          )
        )

      case x: org.joda.time.Duration =>
        Some(ValDayTimeDuration(Duration.ofMillis(x.getMillis)))

      case x: org.joda.time.Period =>
        Some(ValYearMonthDuration(Period.of(x.getYears, x.getMonths, 0)))

      case _ => None
    }

  override def unpackVal(value: Val,
                         innerValueMapper: Val => Any): Option[Any] =
    None

  override val priority: Int = 20
}
