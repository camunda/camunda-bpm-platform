package org.camunda.feel.integration

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.camunda.bpm.dmn.feel.impl.FeelException
import java.time._

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

  it should "return number as Java Double" in {

    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map())

    camundaFeelEngine.evaluateSimpleExpression[Any]("2.4", context) should be (new java.lang.Double(2.4))
  }
  
  it should "return list as Java List" in {

  	val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map())

    val list = new java.util.ArrayList[String]
    list.add("a")
    list.add("b")

    camundaFeelEngine.evaluateSimpleExpression[Any](""" ["a","b"] """, context) should be (list)
  }

  it should "return context as Java Map" in {

  	val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map())

    val map = new java.util.HashMap[String, String]
    map.put("a", "b")

    camundaFeelEngine.evaluateSimpleExpression[Any](""" {a:"b"} """, context) should be (map)
  }

  it should "map joda-local-date as Date" in {

    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map("x" -> org.joda.time.LocalDate.parse("2017-04-02")))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](""" x = date("2017-04-02") """, context) should be (true)
  }

  it should "map joda-local-time as Date" in {

    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map("x" -> org.joda.time.LocalTime.parse("12:04:30")))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](""" x = time("12:04:30") """, context) should be (true)
  }

  it should "map joda-local-date-time as Date" in {

    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map("x" -> org.joda.time.LocalDateTime.parse("2017-04-02T12:04:30")))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](""" x = date and time("2017-04-02T12:04:30") """, context) should be (true)
  }

  it should "map joda-period as Year-Month-Duration" in {

    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map("x" -> org.joda.time.Period.parse("P2Y4M")))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](""" x = duration("P2Y4M") """, context) should be (true)
  }

  it should "map joda-duration as Day-Time-Duration" in {

    val camundaFeelEngine = camundaFeelEngineFactory.createInstance()
    val context = new SimpleTestContext(Map("x" -> org.joda.time.Duration.standardHours(4)))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](""" x = duration("PT4H") """, context) should be (true)
  }

}
