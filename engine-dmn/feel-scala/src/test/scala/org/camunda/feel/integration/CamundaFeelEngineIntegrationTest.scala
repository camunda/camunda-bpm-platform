package org.camunda.feel.integration

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.camunda.bpm.dmn.feel.impl.FeelException

/**
 * @author Philipp Ossler
 */
class CamundaFeelEngineIntegrationTest extends FlatSpec with Matchers {
  
  val camundaFeelEngineFactory = new CamundaFeelEngineFactory  
  
  "A Camunda FEEL Engine Factory" should "create a new FEEL Engine" in {
    
    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    
    Option(camundaFeelEngine) should not be None
    camundaFeelEngine.getClass should be (classOf[CamundaFeelEngine])
  }
  
  "A Camunda FEEL Engine" should "evaluate simple unary tests" in {
    
    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map("input" -> 3))
    
    camundaFeelEngine.evaluateSimpleUnaryTests("< 2", "input", context) should be(false)
    camundaFeelEngine.evaluateSimpleUnaryTests("[2..5]", "input", context) should be(true)
  }
  
  it should "evaluate a simple expression" in {
    
    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map("a" -> 2))
    
    camundaFeelEngine.evaluateSimpleExpression[Int]("a + 2", context) should be(4)
    camundaFeelEngine.evaluateSimpleExpression[Int]("a * 4", context) should be(8)
  }
  
}