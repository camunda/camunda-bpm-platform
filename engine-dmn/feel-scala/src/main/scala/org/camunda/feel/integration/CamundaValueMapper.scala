package org.camunda.feel.integration

import org.camunda.feel._
import org.camunda.feel.interpreter._
import org.camunda.feel.spi._
import scala.collection.JavaConverters._
import java.time._
import org.camunda.spin.json.SpinJsonNode

class CamundaValueMapper extends CustomValueMapper {

  override def toVal(x: Any): Val = x match {
    // joda-time
    case x: org.joda.time.LocalDate => ValDate(LocalDate.of(x.getYear, x.getMonthOfYear, x.getDayOfMonth))
    case x: org.joda.time.LocalTime => ValLocalTime(LocalTime.of(x.getHourOfDay, x.getMinuteOfHour, x.getSecondOfMinute))
    case x: org.joda.time.LocalDateTime => ValLocalDateTime(LocalDateTime.of(x.getYear, x.getMonthOfYear, x.getDayOfMonth, x.getHourOfDay, x.getMinuteOfHour, x.getSecondOfMinute))
    case x: org.joda.time.Duration => ValDayTimeDuration( Duration.ofMillis( x.getMillis ) )
    case x: org.joda.time.Period => ValYearMonthDuration( Period.of(x.getYears, x.getMonths, 0) )
    // Camunda Spin JSON
    case x: SpinJsonNode  => spinJsonToVal(x)    
    // else
    case _ => super.toVal(x)
  }

  override def unpackVal(value: Val): Any = value match {
    case ValNumber(number) => 
    {
      if (number.isWhole()) {
        number.longValue: java.lang.Long
      } else {
          number.doubleValue: java.lang.Double  
      }
    }
    case ValList(list) => (list map unpackVal).asJava: java.util.List[Any]
    case ValContext(dc: DefaultContext) => (dc.variables.map { case (key, value) => key -> unpackVal(toVal(value)) }.toMap).asJava: java.util.Map[String, Any]
    // else
    case _ => super.unpackVal(value)
  }
  
  private def spinJsonToVal(node: SpinJsonNode): Val = node match {
    case n if (n.isObject()) => 
    {
      val fields = n.fieldNames().asScala
      val pairs = fields.map(field => field -> spinJsonToVal(n.prop(field)))
      
      ValContext(DefaultContext(pairs.toMap))  
    }
    case n if (n.isArray())  => 
    {
      val elements = n.elements().asScala
      val values = elements.map(spinJsonToVal).toList
      
      ValList(values)  
    }
    case n if (n.isNull())   => ValNull
    case n                   => toVal(n.value())
  }

}
