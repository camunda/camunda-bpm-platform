package org.camunda.feel.integration

import org.camunda.feel.interpreter._
import org.camunda.feel.spi._
import scala.collection.JavaConverters._
import java.time._
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.xml.SpinXmlNode
import org.camunda.spin.xml.SpinXmlElement
import org.camunda.spin.xml.SpinXmlAttribute

class CamundaValueMapper extends JavaValueMapper {

  override def toVal(x: Any): Val = x match {
    // joda-time
    case x: org.joda.time.LocalDate =>
      ValDate(LocalDate.of(x.getYear, x.getMonthOfYear, x.getDayOfMonth))
    case x: org.joda.time.LocalTime =>
      ValLocalTime(
        LocalTime.of(x.getHourOfDay, x.getMinuteOfHour, x.getSecondOfMinute))
    case x: org.joda.time.LocalDateTime =>
      ValLocalDateTime(
        LocalDateTime.of(x.getYear,
                         x.getMonthOfYear,
                         x.getDayOfMonth,
                         x.getHourOfDay,
                         x.getMinuteOfHour,
                         x.getSecondOfMinute))
    case x: org.joda.time.Duration =>
      ValDayTimeDuration(Duration.ofMillis(x.getMillis))
    case x: org.joda.time.Period =>
      ValYearMonthDuration(Period.of(x.getYears, x.getMonths, 0))
    // Camunda Spin
    case x: SpinJsonNode   => spinJsonToVal(x)
    case x: SpinXmlElement => spinXmlToVal(x)
    // else
    case _ => super.toVal(x)
  }

  private def spinJsonToVal(node: SpinJsonNode): Val = node match {
    case n if (n.isObject()) => {
      val fields = n.fieldNames().asScala
      val pairs = fields.map(field => field -> spinJsonToVal(n.prop(field)))

      ValContext(DefaultContext(pairs.toMap))
    }
    case n if (n.isArray()) => {
      val elements = n.elements().asScala
      val values = elements.map(spinJsonToVal).toList

      ValList(values)
    }
    case n if (n.isNull()) => ValNull
    case n                 => toVal(n.value())
  }

  private def spinXmlToVal(e: SpinXmlElement): Val = {
    val name = nodeName(e)
    val value = spinXmlElementToVal(e)

    ValContext(DefaultContext(variables = Map(nodeName(e) -> value)))
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
      ValContext(DefaultContext(variables = members))
    }
  }

  private def spinXmlAttributeToVal(a: SpinXmlAttribute): (String, Val) =
    ("@" + nodeName(a)) -> ValString(a.value)

  private def nodeName(n: SpinXmlNode[_]): String = {
    Option(n.prefix)
      .map(_ + "$" + n.name)
      .getOrElse(n.name)
  }

}
