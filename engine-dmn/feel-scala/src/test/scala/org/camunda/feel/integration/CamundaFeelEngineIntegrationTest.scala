package org.camunda.feel.integration

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.camunda.bpm.dmn.feel.impl.FeelException

/**
 * @author Philipp Ossler
 */
class CamundaFeelEngineIntegrationTest extends FlatSpec with Matchers {
  
  val camundaFeelEngineFactory = new CamundaFeelEngineFactory  
  
  "A camunda Feel Engine Factory" should "create a new Feel Engine" in {
    
    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    
    Option(camundaFeelEngine) should not be None
    camundaFeelEngine.getClass should be (classOf[CamundaFeelEngine])
  }
  
  "A camunda Feel Engine" should "evaluate a simple unary test" in {
    
    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map("input" -> 2))
    
    camundaFeelEngine.evaluateSimpleUnaryTests("< 4", "input", context) should be(true)
  }
  
  it should "evaluate a simple expression" in {
    
    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map())
    
    camundaFeelEngine.evaluateSimpleExpression[Int]("2+4", context) should be(6)
  }
  
}