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
import org.camunda.bpm.dmn.feel.impl.scala.function.CustomFunctionTransformer;
import org.camunda.bpm.dmn.feel.impl.scala.function.FeelCustomFunctionProvider;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.feel.FeelEngine.Failure;
import org.camunda.feel.FeelEngine.UnaryTests$;
import org.camunda.feel.context.CustomContext;
import org.camunda.feel.context.FunctionProvider;
import org.camunda.feel.context.FunctionProvider.CompositeFunctionProvider;
import org.camunda.feel.context.VariableProvider;
import org.camunda.feel.context.VariableProvider.StaticVariableProvider;
import org.camunda.feel.impl.SpiServiceLoader;
import org.camunda.feel.valuemapper.ValueMapper;
import scala.collection.immutable.List;
import scala.collection.immutable.Map;
import scala.runtime.BoxesRunTime;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;

import java.util.Arrays;

import static org.camunda.feel.context.FunctionProvider.EmptyFunctionProvider$;
import static org.camunda.feel.context.VariableProvider.CompositeVariableProvider;
import static scala.jdk.CollectionConverters.ListHasAsScala;

public class CamundaFeelEngine implements FeelEngine {

  protected org.camunda.feel.FeelEngine feelEngine;

  public CamundaFeelEngine(java.util.List<FeelCustomFunctionProvider> functionProviders) {
    ValueMapper valueMapper = SpiServiceLoader.loadValueMapper();

    CustomFunctionTransformer customFunctionTransformer =
      new CustomFunctionTransformer(functionProviders, valueMapper);

    FunctionProvider functionProvider = getFunctionProvider(customFunctionTransformer);

    feelEngine = new org.camunda.feel.FeelEngine(functionProvider, valueMapper);
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

    List list = toList(variableProviders);

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

  protected FunctionProvider getFunctionProvider(CustomFunctionTransformer customFunctionTransformer) {
    FunctionProvider functionProvidersFromSpi = SpiServiceLoader.loadFunctionProvider();

    if (functionProvidersFromSpi instanceof EmptyFunctionProvider$) {
      List<FunctionProvider> functionProviders = toScalaList(customFunctionTransformer);

      return new CompositeFunctionProvider(functionProviders);

    } else if (functionProvidersFromSpi instanceof CompositeFunctionProvider) {
      CompositeFunctionProvider compositeFunctionProviderFromSpi =
        (CompositeFunctionProvider) functionProvidersFromSpi;

      List<FunctionProvider> providersFromSpiAsList = compositeFunctionProviderFromSpi.providers();

      List<FunctionProvider> functionProviders =
        providersFromSpiAsList.$colon$colon(customFunctionTransformer);

      return new CompositeFunctionProvider(functionProviders);

    } else if (functionProvidersFromSpi != null) {
      List<FunctionProvider> functionProviders =
        toScalaList(functionProvidersFromSpi, customFunctionTransformer);

      return new CompositeFunctionProvider(functionProviders);

    } else { // should not happen
      throw new FeelException("No function provider found!");

    }
  }

  @SafeVarargs
  protected final <T> List<T> toScalaList(T... elements) {
    java.util.List<T> listAsJava = Arrays.asList(elements);

    return toList(listAsJava);
  }

  protected <T> List<T> toList(java.util.List list) {
    return ListHasAsScala(list).asScala().toList();
  }

}

