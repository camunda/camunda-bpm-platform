package org.camunda.feel.integration.transformer

import org.camunda.feel._
import org.camunda.bpm.dmn.engine.impl.spi.`type`.DmnDataTypeTransformer
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.engine.variable.Variables
import java.time.ZoneId
import java.time.OffsetDateTime

class FeelLocalDateTimeTypeTransformer extends DmnDataTypeTransformer {
  
  def transform(value: Any): TypedValue = {
    
    val localDateTime: LocalDateTime = value match {
      case x: LocalDateTime => x
      case x: DateTime => x.toLocalDateTime
      case x: OffsetDateTime => x.toLocalDateTime()
      case x: java.util.Date => x.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime
      case other => throw new IllegalArgumentException(s"Cannot transform '$other' to FEEL local-date-time.")
    }
    
    Variables.untypedValue(localDateTime);
    
  }
  
}