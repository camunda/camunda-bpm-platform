package org.camunda.bpm.dmn.feel.impl.scala

import org.camunda.bpm.dmn.feel.impl.FeelEngineFactory
import org.camunda.bpm.dmn.feel.impl.FeelEngine

/**
  * @author Philipp Ossler
  */
class CamundaFeelEngineFactory extends FeelEngineFactory {

  def createInstance(): FeelEngine = new CamundaFeelEngine

}
