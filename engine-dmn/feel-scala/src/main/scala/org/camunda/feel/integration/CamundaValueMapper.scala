package org.camunda.feel.integration

import org.camunda.feel._
import org.camunda.feel.interpreter._
import org.camunda.feel.spi._
import scala.collection.JavaConverters._
import java.time.ZoneId

class CamundaValueMapper extends CustomValueMapper {

  override def unpackVal(value: Val): Any = value match {
    case ValNumber(number) => number.doubleValue: java.lang.Double
    case ValDateTime(dateTime) => java.util.Date.from(dateTime.atZone(ZoneId.systemDefault).toInstant): java.util.Date
    case ValList(list) => (list map unpackVal).asJava: java.util.List[Any]
    case ValContext(dc: DefaultContext) => (dc.variables map { case (key, value) => key -> unpackVal(toVal(value)) } toMap).asJava: java.util.Map[String, Any]
    case _ => super.unpackVal(value)
  }

}
