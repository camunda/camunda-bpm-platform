package org.camunda.feel.integration.transformer

import org.camunda.feel._
import org.camunda.bpm.dmn.engine.impl.spi.`type`.DmnDataTypeTransformer
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.engine.variable.Variables
import java.time.ZoneId
import java.time.ZoneOffset

class FeelDateTypeTransformer extends DmnDataTypeTransformer {
  
  def transform(value: Any): TypedValue = {
    
    val date: Date = value match {
      case x: Date => x
      case other => throw new IllegalArgumentException(s"Cannot transform '$other' to FEEL date.")
    }
    
    Variables.untypedValue(date);    
  }
  
}