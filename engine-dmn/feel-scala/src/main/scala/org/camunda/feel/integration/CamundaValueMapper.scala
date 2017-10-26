package org.camunda.feel.integration

import org.camunda.feel._
import org.camunda.feel.interpreter._
import org.camunda.feel.spi._
import scala.collection.JavaConverters._
import java.time._

class CamundaValueMapper extends CustomValueMapper {

  override def toVal(x: Any): Val = x match {
    // joda-time
    case x: org.joda.time.LocalDate => ValDate(LocalDate.of(x.getYear, x.getMonthOfYear, x.getDayOfMonth))
    case x: org.joda.time.LocalTime => ValLocalTime(LocalTime.of(x.getHourOfDay, x.getMinuteOfHour, x.getSecondOfMinute))
    case x: org.joda.time.LocalDateTime => ValLocalDateTime(LocalDateTime.of(x.getYear, x.getMonthOfYear, x.getDayOfMonth, x.getHourOfDay, x.getMinuteOfHour, x.getSecondOfMinute))
    case x: org.joda.time.Duration => ValDayTimeDuration( Duration.ofMillis( x.getMillis ) )
    case x: org.joda.time.Period => ValYearMonthDuration( Period.of(x.getYears, x.getMonths, 0) )
    // else
    case _ => super.toVal(x)
  }

  override def unpackVal(value: Val): Any = value match {
    case ValNumber(number) => number.doubleValue: java.lang.Double
    case ValLocalDateTime(dateTime) => java.util.Date.from(dateTime.atZone(ZoneId.systemDefault).toInstant): java.util.Date
    case ValDateTime(dateTime) => java.util.Date.from(dateTime.toInstant): java.util.Date
    case ValList(list) => (list map unpackVal).asJava: java.util.List[Any]
    case ValContext(dc: DefaultContext) => (dc.variables.map { case (key, value) => key -> unpackVal(toVal(value)) }.toMap).asJava: java.util.Map[String, Any]
    // else
    case _ => super.unpackVal(value)
  }

}
