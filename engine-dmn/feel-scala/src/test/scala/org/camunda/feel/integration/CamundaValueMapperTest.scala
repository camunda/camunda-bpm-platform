package org.camunda.feel.integration

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.camunda.bpm.dmn.feel.impl.FeelException
import org.camunda.spin.Spin
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.xml.SpinXmlElement
import scala.collection.JavaConverters._
import java.time._

/**
  * @author Philipp Ossler
  */
class CamundaValueMapperTest extends FlatSpec with Matchers {

  val camundaFeelEngineFactory = new CamundaFeelEngineFactory
  val camundaFeelEngine = camundaFeelEngineFactory.createInstance()

  "A Camunda FEEL Engine" should "return number as Java Double" in {

    val context = new SimpleTestContext(Map())

    camundaFeelEngine.evaluateSimpleExpression[Any]("2.4", context) should be(
      new java.lang.Double(2.4))
  }

  it should "return list as Java List" in {

    val context = new SimpleTestContext(Map())

    val list = new java.util.ArrayList[String]
    list.add("a")
    list.add("b")

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" ["a","b"] """, context) should be(list)
  }

  it should "return context as Java Map" in {

    val context = new SimpleTestContext(Map())

    val map = new java.util.HashMap[String, String]
    map.put("a", "b")

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" {a:"b"} """, context) should be(map)
  }

  it should "map joda-local-date as Date" in {

    val context = new SimpleTestContext(
      Map("x" -> org.joda.time.LocalDate.parse("2017-04-02")))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](
      """ x = date("2017-04-02") """,
      context) should be(true)
  }

  it should "map joda-local-time as Date" in {

    val context = new SimpleTestContext(
      Map("x" -> org.joda.time.LocalTime.parse("12:04:30")))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](
      """ x = time("12:04:30") """,
      context) should be(true)
  }

  it should "map joda-local-date-time as Date-Time" in {

    val context = new SimpleTestContext(
      Map("x" -> org.joda.time.LocalDateTime.parse("2017-04-02T12:04:30")))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](
      """ x = date and time("2017-04-02T12:04:30") """,
      context) should be(true)
  }

  it should "map joda-date-time as Date-Time" in {

    val context = new SimpleTestContext(
      Map("x" -> org.joda.time.DateTime.parse("2017-04-02T12:04:30+05:00")))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](
      """ x = date and time("2017-04-02T12:04:30+05:00") """,
      context) should be(true)
  }

  it should "map joda-period as Year-Month-Duration" in {

    val context =
      new SimpleTestContext(Map("x" -> org.joda.time.Period.parse("P2Y4M")))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](
      """ x = duration("P2Y4M") """,
      context) should be(true)
  }

  it should "map joda-duration as Day-Time-Duration" in {

    val context =
      new SimpleTestContext(Map("x" -> org.joda.time.Duration.standardHours(4)))

    camundaFeelEngine.evaluateSimpleExpression[Boolean](
      """ x = duration("PT4H") """,
      context) should be(true)
  }

  it should "map Camunda Spin JSON object as context" in {

    val json: SpinJsonNode =
      Spin.JSON("""{"customer": "Kermit", "language": "en"}""")

    val context = new SimpleTestContext(Map("json" -> json))

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" json """, context) should be(
      Map(
        "customer" -> "Kermit",
        "language" -> "en"
      ).asJava)
  }

  it should "map Camunda Spin JSON array as list" in {

    val json = Spin.JSON("""{"customer": ["Kermit", "Waldo"]}""")

    val context = new SimpleTestContext(Map("json" -> json))

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" json """, context) should be(
      Map(
        "customer" -> List("Kermit", "Waldo").asJava
      ).asJava)
  }

  it should "map a nested Camunda Spin JSON object as context" in {

    val json: SpinJsonNode = Spin.JSON(
      """{"customer": "Kermit", "address": {"city": "Berlin", "zipCode": 10961}}""")

    val context = new SimpleTestContext(Map("json" -> json))

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" json """, context) should be(
      Map(
        "customer" -> "Kermit",
        "address" -> Map(
          "city" -> "Berlin",
          "zipCode" -> 10961.toLong
        ).asJava
      ).asJava)
  }

  it should "map Camunda Spin XML object with attributes" in {

    val xml: SpinXmlElement = Spin.XML("""
      <customer name="Kermit" language="en" />
    """)

    val context = new SimpleTestContext(Map("xml" -> xml))

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" xml """, context) should be(
      Map(
        "customer" -> Map(
          "@name" -> "Kermit",
          "@language" -> "en"
        ).asJava).asJava)
  }

  it should "map Camunda Spin XML object with child object" in {

    val xml: SpinXmlElement = Spin.XML("""
      <customer>
        <address city="Berlin" zipCode="10961" />
      </customer>
    """)

    val context = new SimpleTestContext(Map("xml" -> xml))

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" xml """, context) should be(
      Map(
        "customer" -> Map(
          "address" -> Map(
            "@city" -> "Berlin",
            "@zipCode" -> "10961"
          ).asJava).asJava).asJava)
  }

  it should "map Camunda Spin XML object with list of child objects" in {

    val xml: SpinXmlElement = Spin.XML("""
      <data>
        <customer name="Kermit" language="en" />
        <customer name="John" language="de" />
        <provider name="Foobar" />
      </data>
    """)

    val context = new SimpleTestContext(Map("xml" -> xml))

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" xml """, context) should be(
      Map("data" -> Map(
        "customer" -> List(
          Map("@name" -> "Kermit", "@language" -> "en").asJava,
          Map("@name" -> "John", "@language" -> "de").asJava
        ).asJava,
        "provider" -> Map("@name" -> "Foobar").asJava
      ).asJava).asJava)
  }

  it should "map Camunda Spin XML object with content" in {

    val xml: SpinXmlElement = Spin.XML("""
      <customer>Kermit</customer>
    """)

    val context = new SimpleTestContext(Map("xml" -> xml))

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" xml """, context) should be(
      Map(
        "customer" -> Map(
          "$content" -> "Kermit"
        ).asJava).asJava)
  }

  it should "map Camunda Spin XML object without content" in {

    val xml: SpinXmlElement = Spin.XML("""
      <customer />
    """)

    val context = new SimpleTestContext(Map("xml" -> xml))

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" xml """, context) should be(
      Map("customer" -> null).asJava)
  }

  it should "map Camunda Spin XML object with prefix" in {

    val xml: SpinXmlElement =
      Spin.XML("""
      <data xmlns:p="http://www.example.org">
        <p:customer p:name="Kermit" language="en" />
      </data>
    """)

    val context = new SimpleTestContext(Map("xml" -> xml))

    camundaFeelEngine
      .evaluateSimpleExpression[Any](""" xml """, context) should be(
      Map("data" -> Map(
        "p$customer" -> Map("@p$name" -> "Kermit", "@language" -> "en").asJava,
        "@xmlns$p" -> "http://www.example.org"
      ).asJava).asJava)
  }

}
