package org.camunda.feel.integration

import scala.collection.JavaConverters._
import org.camunda.bpm.engine.variable.context.VariableContext
import org.camunda.bpm.engine.variable.value.TypedValue
import java.util.Set
import org.camunda.bpm.engine.variable.Variables

/**
 * @author Philipp Ossler
 */
class SimpleTestContext(variables: Map[String, Any]) extends VariableContext {

  def resolve(variableName: String): TypedValue = variables.get(variableName) match {
    case Some(variable) => Variables.untypedValue(variable)
    case None => Variables.untypedNullValue()
  }

  def containsVariable(variableName: String): Boolean = variables.contains(variableName)

  def keySet: Set[String] = variables.keySet.asJava

}
