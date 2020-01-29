/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.dmn.feel.impl.scala;

import org.camunda.bpm.dmn.feel.impl.FeelEngine;
import org.camunda.bpm.dmn.feel.impl.FeelException;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.feel.FeelEngine.Failure;
import org.camunda.feel.FeelEngine.UnaryTests$;
import org.camunda.feel.impl.spi.CustomContext;
import org.camunda.feel.impl.spi.SpiServiceLoader;
import org.camunda.feel.interpreter.impl.VariableProvider;
import org.camunda.feel.interpreter.impl.VariableProvider.StaticVariableProvider;
import scala.collection.immutable.List;
import scala.collection.immutable.Map;
import scala.runtime.BoxesRunTime;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;

import java.util.Arrays;

import static org.camunda.feel.interpreter.impl.VariableProvider.CompositeVariableProvider;
import static scala.jdk.CollectionConverters.ListHasAsScala;

public class CamundaFeelEngine implements FeelEngine {

  protected org.camunda.feel.FeelEngine feelEngine;

  {
    feelEngine = new org.camunda.feel.FeelEngine(
      SpiServiceLoader.loadFunctionProvider(),
      SpiServiceLoader.loadValueMapper()
    );
  }

  public <T> T evaluateSimpleExpression(String expression, VariableContext variableContext) {

    CustomContext context = new CustomContext() {
      public VariableProvider variableProvider() {
        return new ContextVariableWrapper(variableContext);
      }
    };

    Either either = feelEngine.evalExpression(expression, context);

    if (either instanceof Right) {
      Right right = (Right) either;

      return (T) right.value();

    } else {
      Left left = (Left) either;
      Failure failure = (Failure) left.value();
      String message = failure.message();

      throw new FeelException(message);

    }
  }

  public boolean evaluateSimpleUnaryTests(String expression, String inputVariable, VariableContext variableContext) {
    Map inputVariableMap = new Map.Map1(UnaryTests$.MODULE$.inputVariable(), inputVariable);

    StaticVariableProvider inputVariableContext = new StaticVariableProvider(inputVariableMap);

    ContextVariableWrapper contextVariableWrapper = new ContextVariableWrapper(variableContext);

    java.util.List<? extends VariableProvider> variableProviders =
      Arrays.asList(inputVariableContext, contextVariableWrapper);

    List list = ListHasAsScala(variableProviders).asScala().toList();

    CustomContext context = new CustomContext() {
      public VariableProvider variableProvider() {
        return new CompositeVariableProvider(list);
      }
    };

    Either either = feelEngine.evalUnaryTests(expression, context);

    if (either instanceof Right) {
      Right right = (Right) either;
      Object value = right.value();

      return BoxesRunTime.unboxToBoolean(value);

    } else {
      Left left = (Left) either;
      Failure failure = (Failure) left.value();
      String message = failure.message();

      throw new FeelException(message);

    }
  }

}

