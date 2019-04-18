package org.camunda.feel.integration

import org.camunda.bpm.engine.variable.context.VariableContext
import org.camunda.feel.interpreter._
import org.camunda.feel.spi._
import org.camunda.bpm.dmn.feel.impl.FeelException

/**
  * @author Philipp Ossler
  */
class CamundaFeelEngine extends org.camunda.bpm.dmn.feel.impl.FeelEngine {

  private lazy val engine =
    new org.camunda.feel.FeelEngine(
      valueMapper = new CamundaValueMapper,
      functionProvider = SpiServiceLoader.loadFunctionProvider
    )

  private def asVariableProvider(ctx: VariableContext,
                                 valueMapper: ValueMapper): VariableProvider = (name: String) => {
    if (ctx.containsVariable(name)) {
      Some(valueMapper.toVal(ctx.resolve(name).getValue))
    } else {
      None
    }
  }

  override def evaluateSimpleExpression[T](expression: String,
                                           ctx: VariableContext): T = {
    val context = new RootContext(
      variableProvider = asVariableProvider(ctx, engine.valueMapper))
    engine.evalExpression(expression, context) match {
      case Right(value) => value.asInstanceOf[T]
      case Left(failure) => throw new FeelException(failure.message)
    }
  }

  override def evaluateSimpleUnaryTests(expression: String,
                                        inputVariable: String,
                                        ctx: VariableContext): Boolean = {
    val context = new RootContext(
      Map(RootContext.inputVariableKey -> inputVariable),
      variableProvider = asVariableProvider(ctx, engine.valueMapper))
    engine.evalUnaryTests(expression, context) match {
      case Right(value) => value
      case Left(failure) => throw new FeelException(failure.message)
    }
  }

}
