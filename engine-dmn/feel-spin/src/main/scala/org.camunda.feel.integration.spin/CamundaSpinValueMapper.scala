package org.camunda.feel.integration

import org.camunda.feel.interpreter._
import org.camunda.feel.spi._
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.xml.{SpinXmlAttribute, SpinXmlElement, SpinXmlNode}

import scala.jdk.CollectionConverters._

/**
  * Transform objects from Camunda Spin JSON/XML into FEEL types.
  */
class CamundaSpinValueMapper extends CustomValueMapper {

  override def toVal(x: Any, innerValueMapper: Any => Val): Option[Val] =
    x match {
      case x: SpinJsonNode   => Some(spinJsonToVal(x, innerValueMapper))
      case x: SpinXmlElement => Some(spinXmlToVal(x))

      case _ => None
    }

  private def spinJsonToVal(node: SpinJsonNode,
                            innerValueMapper: Any => Val): Val = node match {
    case n if (n.isObject()) => {
      val fields = n.fieldNames().asScala
      val pairs =
        fields.map(field =>
          field -> spinJsonToVal(n.prop(field), innerValueMapper))

      ValContext(Context.StaticContext(pairs.toMap))
    }
    case n if (n.isArray()) => {
      val elements = n.elements().asScala
      val values = elements.map(e => spinJsonToVal(e, innerValueMapper)).toList

      ValList(values)
    }
    case n if (n.isNull()) => ValNull
    case n                 => innerValueMapper(n.value())
  }

  private def spinXmlToVal(e: SpinXmlElement): Val = {
    val name = nodeName(e)
    val value = spinXmlElementToVal(e)

    ValContext(Context.StaticContext(variables = Map(nodeName(e) -> value)))
  }

  private def spinXmlElementToVal(e: SpinXmlElement): Val = {
    val content = Option(e.textContent)
      .map(_.trim)
      .filterNot(_.isEmpty)
      .map(c => Map("$content" -> ValString(c)))
      .getOrElse(Map.empty)

    val attributes = e.attrs.asScala.map(spinXmlAttributeToVal).toMap

    val children = e.childElements.asScala
      .map(c => nodeName(c) -> spinXmlElementToVal(c))
      .toList
      .groupBy(_._1)
      .map {
        case (name, elements) =>
          elements match {
            case e :: Nil => name -> e._2
            case list     => name -> ValList(list.map(_._2))
          }
      }

    val members: Map[String, Val] = attributes ++ content ++ children

    if (members.isEmpty) {
      ValNull
    } else {
      ValContext(Context.StaticContext(variables = members))
    }
  }

  private def spinXmlAttributeToVal(a: SpinXmlAttribute): (String, Val) =
    ("@" + nodeName(a)) -> ValString(a.value)

  private def nodeName(n: SpinXmlNode[_]): String = {
    Option(n.prefix)
      .map(_ + "$" + n.name)
      .getOrElse(n.name)
  }

  override def unpackVal(value: Val,
                         innerValueMapper: Val => Any): Option[Any] =
    None

  override val priority: Int = 30

}
