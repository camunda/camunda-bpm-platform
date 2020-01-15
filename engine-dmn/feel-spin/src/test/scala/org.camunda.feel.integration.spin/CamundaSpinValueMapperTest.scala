package org.camunda.feel.integration

import org.camunda.feel.interpreter._
import org.camunda.spin.Spin
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.xml.SpinXmlElement
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Philipp Ossler
  */
class CamundaSpinValueMapperTest extends FlatSpec with Matchers {

  val valueMapper =
    ValueMapper.CompositeValueMapper(
      List(DefaultValueMapper.instance, new CamundaSpinValueMapper))

  "The Camunda Spin Value Mapper" should "map Camunda Spin JSON object as context" in {

    val json: SpinJsonNode =
      Spin.JSON("""{"customer": "Kermit", "language": "en"}""")

    valueMapper.toVal(json) should be(
      ValContext(
        Context.StaticContext(
          Map(
            "customer" -> ValString("Kermit"),
            "language" -> ValString("en")
          )))
    )
  }

  it should "map Camunda Spin JSON array as list" in {

    val json = Spin.JSON("""{"customer": ["Kermit", "Waldo"]}""")

    valueMapper.toVal(json) should be(
      ValContext(
        Context.StaticContext(
          Map(
            "customer" -> ValList(List(
              ValString("Kermit"),
              ValString("Waldo")
            ))
          )))
    )
  }

  it should "map a nested Camunda Spin JSON object as context" in {

    val json: SpinJsonNode = Spin.JSON(
      """{"customer": "Kermit", "address": {"city": "Berlin", "zipCode": 10961}}""")

    valueMapper.toVal(json) should be(
      ValContext(
        Context.StaticContext(
          Map(
            "customer" -> ValString("Kermit"),
            "address" -> ValContext(
              Context.StaticContext(
                Map(
                  "city" -> ValString("Berlin"),
                  "zipCode" -> ValNumber(10961)
                )
              )
            )
          )))
    )
  }

  it should "map Camunda Spin XML object with attributes" in {

    val xml: SpinXmlElement = Spin.XML(
      """
      <customer name="Kermit" language="en" />
    """)

    valueMapper.toVal(xml) should be(
      ValContext(
        Context.StaticContext(
          Map(
            "customer" -> ValContext(
              Context.StaticContext(
                Map(
                  "@name" -> ValString("Kermit"),
                  "@language" -> ValString("en")
                )
              )
            )
          )))
    )
  }

  it should "map Camunda Spin XML object with child object" in {

    val xml: SpinXmlElement = Spin.XML(
      """
      <customer>
        <address city="Berlin" zipCode="10961" />
      </customer>
    """)

    valueMapper.toVal(xml) should be(
      ValContext(Context.StaticContext(Map(
        "customer" -> ValContext(Context.StaticContext(Map(
          "address" -> ValContext(Context.StaticContext(Map(
            "@city" -> ValString("Berlin"),
            "@zipCode" -> ValString("10961")
          )))
        )))
      )))
    )
  }

  it should "map Camunda Spin XML object with list of child objects" in {

    val xml: SpinXmlElement = Spin.XML(
      """
      <data>
        <customer name="Kermit" language="en" />
        <customer name="John" language="de" />
        <provider name="Foobar" />
      </data>
    """)

    valueMapper.toVal(xml) should be(
      ValContext(Context.StaticContext(Map(
        "data" -> ValContext(Context.StaticContext(Map(
          "customer" -> ValList(List(
            ValContext(Context.StaticContext(Map(
              "@name" -> ValString("Kermit"),
              "@language" -> ValString("en")
            ))),
            ValContext(Context.StaticContext(Map(
              "@name" -> ValString("John"),
              "@language" -> ValString("de")
            )))
          )),
          "provider" -> ValContext(Context.StaticContext(Map(
            "@name" -> ValString("Foobar")
          )))
        )))
      )))
    )
  }

  it should "map Camunda Spin XML object with content" in {

    val xml: SpinXmlElement = Spin.XML(
      """
      <customer>Kermit</customer>
    """)

    valueMapper.toVal(xml) should be(
      ValContext(Context.StaticContext(Map(
        "customer" -> ValContext(Context.StaticContext(Map(
          "$content" -> ValString("Kermit")
        )))
      )))
    )
  }

  it should "map Camunda Spin XML object without content" in {

    val xml: SpinXmlElement = Spin.XML(
      """
      <customer />
    """)

    valueMapper.toVal(xml) should be(
      ValContext(Context.StaticContext(Map(
        "customer" -> ValNull
      )))
    )
  }

  it should "map Camunda Spin XML object with prefix" in {

    val xml: SpinXmlElement =
      Spin.XML(
        """
      <data xmlns:p="http://www.example.org">
        <p:customer p:name="Kermit" language="en" />
      </data>
    """)

    valueMapper.toVal(xml) should be(
      ValContext(Context.StaticContext(Map(
        "data" -> ValContext(Context.StaticContext(Map(
          "p$customer" -> ValContext(Context.StaticContext(Map(
            "@p$name" -> ValString("Kermit"),
            "@language" -> ValString("en")
          ))),
          "@xmlns$p" -> ValString("http://www.example.org")
        )))
      )))
    )
  }

}
