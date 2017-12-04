package org.camunda.feel.integration.transformer

import org.camunda.feel._
import org.camunda.bpm.dmn.engine.impl.spi.`type`.DmnDataTypeTransformer
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.engine.variable.Variables
import java.time.ZoneId

class FeelDateTimeTypeTransformer extends DmnDataTypeTransformer {
  
  def transform(value: Any): TypedValue = {
    
    val dateTime: DateTime = value match {
      case x: DateTime => x
      case x: LocalDateTime => x.atZone(ZoneId.systemDefault).toOffsetDateTime
      case x: java.util.Date => x.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime
      case other => throw new IllegalArgumentException(s"Cannot transform '$other' to FEEL date-time.")
    }
    
    Variables.untypedValue(dateTime);
    
  }
  
}