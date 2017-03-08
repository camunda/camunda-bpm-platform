package org.camunda.feel.integration

import org.camunda.bpm.dmn.feel.impl.FeelEngine
import org.camunda.bpm.engine.variable.context.VariableContext
import org.camunda.feel.{ FeelEngine, EvalValue, EvalFailure, ParseFailure }
import org.camunda.feel.interpreter.Context
import scala.collection.JavaConversions._
import org.camunda.bpm.dmn.feel.impl.FeelException

/**
 * @author Philipp Ossler
 */
class CamundaFeelEngine extends org.camunda.bpm.dmn.feel.impl.FeelEngine {

  lazy val engine = new org.camunda.feel.FeelEngine

  def evaluateSimpleExpression[T](expression: String, context: VariableContext): T = {
    val variables = unpack(context)

    engine.evalExpression(expression, variables) match {
      case EvalValue(value) => value.asInstanceOf[T]
      case EvalFailure(error) => throw new FeelException(error)
      case ParseFailure(error) => throw new FeelException(error)
    }
  }

  def evaluateSimpleUnaryTests(expression: String, inputVariable: String, context: VariableContext): Boolean = {
    val ctx = unpack(context)
    val variables = Map(Context.inputVariableKey -> inputVariable) ++ ctx

    engine.evalSimpleUnaryTests(expression, variables) match {
      case EvalValue(value) => value.asInstanceOf[Boolean]
      case EvalFailure(error) => throw new FeelException(error)
      case ParseFailure(error) => throw new FeelException(error)
    }
  }

  private def unpack(context: VariableContext): Map[String, Any] = {
    context.keySet map (key => key -> context.resolve(key).getValue) toMap
  }

}