package org.camunda.feel.integration

import org.camunda.bpm.dmn.feel.impl.FeelException
import org.camunda.bpm.engine.variable.context.VariableContext
import org.camunda.feel.FeelEngine.UnaryTests
import org.camunda.feel.interpreter._
import org.camunda.feel.spi._

import scala.jdk.CollectionConverters._

/**
  * @author Philipp Ossler
  */
class CamundaFeelEngine extends org.camunda.bpm.dmn.feel.impl.FeelEngine {

  private lazy val engine =
    new org.camunda.feel.FeelEngine(
      valueMapper = SpiServiceLoader.loadValueMapper,
      functionProvider = SpiServiceLoader.loadFunctionProvider
    )

  override def evaluateSimpleExpression[T](expression: String,
                                           ctx: VariableContext): T = {

    val context = new CustomContext {
      override val variableProvider = ContextVariableWrapper(ctx)
    }

    engine.evalExpression(expression, context) match {
      case Right(value)  => value.asInstanceOf[T]
      case Left(failure) => throw new FeelException(failure.message)
    }
  }

  override def evaluateSimpleUnaryTests(expression: String,
                                        inputVariable: String,
                                        ctx: VariableContext): Boolean = {

    val inputVariableContext = VariableProvider.StaticVariableProvider(
      Map(
        UnaryTests.inputVariable -> inputVariable
      ))

    val context = new CustomContext {
      override val variableProvider =
        VariableProvider.CompositeVariableProvider(
          List(inputVariableContext, ContextVariableWrapper(ctx)))
    }

    engine.evalUnaryTests(expression, context) match {
      case Right(value)  => value
      case Left(failure) => throw new FeelException(failure.message)
    }
  }

  case class ContextVariableWrapper(context: VariableContext)
      extends VariableProvider {

    override def getVariable(name: String): Option[Any] = {
      if (context.containsVariable(name)) {
        Some(context.resolve(name).getValue)
      } else {
        None
      }
    }

    override def keys: Iterable[String] = context.keySet().asScala
  }

}
