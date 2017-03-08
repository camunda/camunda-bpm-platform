package org.camunda.feel.integration

import scala.collection.JavaConversions._
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration

/**
 * @author Philipp Ossler
 */
class CamundaDmnFeelEngineIntegrationTest extends FlatSpec with Matchers {
  
  val DMN_FILE = "/org/camunda/feel/integration/programmingLanguages.dmn"
  
  val camundaFeelEngineFactory = new CamundaFeelEngineFactory
  
  "A camunda DMN engine with the Feel Engine Factory" should "evaluate a decision table with Feel input entries" in {
  
     val dmnEngineConfig = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().asInstanceOf[DefaultDmnEngineConfiguration]     
     val dmnEngine = dmnEngineConfig.feelEngineFactory(camundaFeelEngineFactory).buildEngine()
     
     val inputStream = getClass.getResourceAsStream(DMN_FILE)
     Option(inputStream) should not be(None)
     
     val decision = dmnEngine.parseDecision("decision", inputStream)
     
     val variables: java.util.Map[String,Object] = Map("language" -> "scala")
     
     val result = dmnEngine.evaluateDecisionTable(decision, variables)
     
     result.size should be(1)
     result.getSingleResult.getSingleEntry.asInstanceOf[String] should be("just cool")
  }
  
}