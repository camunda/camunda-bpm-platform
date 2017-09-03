package org.camunda.feel.integration

import org.camunda.bpm.dmn.feel.impl.FeelEngine
import org.camunda.bpm.engine.variable.context.VariableContext
import org.camunda.feel.{ FeelEngine, EvalValue, EvalFailure, ParseFailure }
import org.camunda.feel.interpreter.Context
import org.camunda.feel.spi.VariableContext._
import scala.collection.JavaConversions._
import org.camunda.bpm.dmn.feel.impl.FeelException

/**
 * @author Philipp Ossler
 */
class CamundaFeelEngine extends org.camunda.bpm.dmn.feel.impl.FeelEngine {

  lazy val engine = new org.camunda.feel.FeelEngine(valueMapper = new CamundaValueMapper)

  private implicit def asValueProvider(ctx: VariableContext): (String => Option[Any]) = key => {
 		if (ctx.containsVariable(key)) {
 			Some(ctx.resolve(key).getValue)
 		}
 		else {
 			None
 		}
 	}
  
  def evaluateSimpleExpression[T](expression: String, context: VariableContext): T = {
     engine.evalExpression(expression, DynamicVariableContext(context)) match {
      case EvalValue(value) => value.asInstanceOf[T]
      case EvalFailure(error) => throw new FeelException(error)
      case ParseFailure(error) => throw new FeelException(error)
    }
  }

  def evaluateSimpleUnaryTests(expression: String, inputVariable: String, context: VariableContext): Boolean = {
    val variables = StaticVariableContext(Map(Context.inputVariableKey -> inputVariable))

    engine.evalUnaryTests(expression, DynamicVariableContext(context, variables)) match {
      case EvalValue(value) => value.asInstanceOf[Boolean]
      case EvalFailure(error) => throw new FeelException(error)
      case ParseFailure(error) => throw new FeelException(error)
    }
  }

}