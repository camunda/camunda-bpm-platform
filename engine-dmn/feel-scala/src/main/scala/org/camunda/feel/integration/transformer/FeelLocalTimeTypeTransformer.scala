package org.camunda.feel.integration.transformer

import org.camunda.feel._
import org.camunda.bpm.dmn.engine.impl.spi.`type`.DmnDataTypeTransformer
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.engine.variable.Variables
import java.time.ZoneId

class FeelLocalTimeTypeTransformer extends DmnDataTypeTransformer {
  
  def transform(value: Any): TypedValue = {
    
    val localTime: LocalTime = value match {
      case x: LocalTime => x
      case x: Time => x.toLocalTime
      case other => throw new IllegalArgumentException(s"Cannot transform '$other' to FEEL local-time.")
    }
    
    Variables.untypedValue(localTime);    
  }
  
}