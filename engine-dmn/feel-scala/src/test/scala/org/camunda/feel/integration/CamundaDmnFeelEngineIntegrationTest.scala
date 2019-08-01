package org.camunda.feel.integration

import scala.collection.JavaConverters._
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration

/**
  * @author Philipp Ossler
  */
class CamundaDmnFeelEngineIntegrationTest extends FlatSpec with Matchers {

  val DMN_DINNER = "/dinnerDecisions.dmn"
  val DMN_VALIDATION = "/validationDecision.dmn"

  val camundaFeelEngineFactory = new CamundaFeelEngineFactory

  val dmnEngineConfig = DmnEngineConfiguration
    .createDefaultDmnEngineConfiguration()
    .asInstanceOf[DefaultDmnEngineConfiguration]

  "A Camunda DMN engine with FEEL Engine Factory" should "evaluate a decision table with FEEL input entries" in {

    dmnEngineConfig.setDefaultInputEntryExpressionLanguage("feel")

    val dmnEngine =
      dmnEngineConfig.feelEngineFactory(camundaFeelEngineFactory).buildEngine()

    val inputStream = getClass.getResourceAsStream(DMN_DINNER)
    Option(inputStream) should not be (None)

    val decision = dmnEngine.parseDecision("dish", inputStream)

    val vars = Map("season" -> "Spring", "guestCount" -> 10)

    val result = dmnEngine.evaluateDecisionTable(decision, toVariables(vars))

    result.size should be(1)
    result.getSingleEntry.asInstanceOf[String] should be("Stew")
  }

  it should "evaluate a decision table with FEEL output entries" in {

    dmnEngineConfig.setDefaultInputEntryExpressionLanguage("feel")
    dmnEngineConfig.setDefaultOutputEntryExpressionLanguage("feel")

    val dmnEngine =
      dmnEngineConfig.feelEngineFactory(camundaFeelEngineFactory).buildEngine()

    val inputStream = getClass.getResourceAsStream(DMN_DINNER)
    Option(inputStream) should not be (None)

    val decision = dmnEngine.parseDecision("dish", inputStream)

    val vars = Map("season" -> "Summer", "guestCount" -> 6)

    val result = dmnEngine.evaluateDecisionTable(decision, toVariables(vars))

    result.size should be(1)
    result.getSingleEntry.asInstanceOf[String] should be(
      "Light Salad and nice Steak")
  }

  it should "evaluate a decision literal expression" in {

    dmnEngineConfig.setDefaultLiteralExpressionLanguage("feel")

    val dmnEngine =
      dmnEngineConfig.feelEngineFactory(camundaFeelEngineFactory).buildEngine()

    val inputStream = getClass.getResourceAsStream(DMN_VALIDATION)
    Option(inputStream) should not be (None)

    val decision = dmnEngine.parseDecision("validation", inputStream)

    val vars = Map("applicant" -> new Applicant(maritalStatus = "M"))

    val result = dmnEngine.evaluateDecision(decision, toVariables(vars))

    result.size should be(1)
    result.getSingleEntry.asInstanceOf[String] should be("valid")
  }

  case class Applicant(maritalStatus: String)

  private def toVariables(vars: Map[String, Any]) =
    vars.asJava.asInstanceOf[java.util.Map[String, Object]]

}
