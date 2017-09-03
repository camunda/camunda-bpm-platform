package org.camunda.feel.integration

import org.camunda.feel._
import org.camunda.feel.interpreter._
import org.camunda.feel.spi._
import scala.collection.JavaConverters._

class CamundaValueMapper extends DefaultValueMapper {
  
	override def unpackVal(value: Val): Any = value match {
    case ValNumber(number) => number.doubleValue: java.lang.Double
    case ValList(list) => (list map unpackVal).asJava: java.util.List[Any]
    case ValContext(context) => (context map { case (key, value) => key -> unpackVal(value) } toMap).asJava: java.util.Map[String, Any]
    case _ => super.unpackVal(value)
  }
	
}