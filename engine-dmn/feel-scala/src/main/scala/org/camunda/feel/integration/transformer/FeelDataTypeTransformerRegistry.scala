package org.camunda.feel.integration.transformer

import org.camunda.bpm.dmn.engine.impl.spi.`type`.DmnDataTypeTransformer
import org.camunda.bpm.dmn.engine.impl.spi.`type`.DmnDataTypeTransformerRegistry

object FeelDataTypeTransformerRegistry {

  val dateTimeTransformer = new FeelDateTimeTypeTransformer
  val localDateTimeTransformer = new FeelLocalDateTimeTypeTransformer

  val timeTransformer = new FeelTimeTypeTransformer
  val localTimeTransformer = new FeelLocalTimeTypeTransformer

  val dateTransformer = new FeelDateTypeTransformer

  def registerBy(registry: DmnDataTypeTransformerRegistry) {
    registry.addTransformer("feel-date-time", dateTimeTransformer)
    registry.addTransformer("feel-local-date-time", localDateTimeTransformer)
    registry.addTransformer("feel-time", timeTransformer)
    registry.addTransformer("feel-local-time", localTimeTransformer)
    registry.addTransformer("feel-date", dateTransformer)
  }

}
