package org.camunda.feel.integration

import org.camunda.bpm.engine.variable.context.VariableContext
import org.camunda.feel.{EvalValue, EvalFailure, ParseFailure}
import org.camunda.feel.interpreter._
import org.camunda.feel.spi._
import scala.collection.JavaConversions._
import org.camunda.bpm.dmn.feel.impl.FeelException

/**
  * @author Philipp Ossler
  */
class CamundaFeelEngine extends org.camunda.bpm.dmn.feel.impl.FeelEngine {

  private lazy val engine =
    new org.camunda.feel.FeelEngine(valueMapper = new CamundaValueMapper)

  private def asVariableProvider(ctx: VariableContext,
                                 valueMapper: ValueMapper): VariableProvider =
    new VariableProvider {
      override def getVariable(name: String): Option[Val] = {
        if (ctx.containsVariable(name)) {
          Some(valueMapper.toVal(ctx.resolve(name).getValue))
        } else {
          None
        }
      }
    }

  override def evaluateSimpleExpression[T](expression: String,
                                           ctx: VariableContext): T = {
    val context = new RootContext(
      variableProvider = asVariableProvider(ctx, engine.valueMapper))
    engine.evalExpression(expression, context) match {
      case EvalValue(value)    => value.asInstanceOf[T]
      case EvalFailure(error)  => throw new FeelException(error)
      case ParseFailure(error) => throw new FeelException(error)
    }
  }

  override def evaluateSimpleUnaryTests(expression: String,
                                        inputVariable: String,
                                        ctx: VariableContext): Boolean = {
    val context = new RootContext(
      Map(RootContext.inputVariableKey -> inputVariable),
      variableProvider = asVariableProvider(ctx, engine.valueMapper))
    engine.evalUnaryTests(expression, context) match {
      case EvalValue(value)    => value.asInstanceOf[Boolean]
      case EvalFailure(error)  => throw new FeelException(error)
      case ParseFailure(error) => throw new FeelException(error)
    }
  }

}
