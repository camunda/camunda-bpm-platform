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
import org.camunda.bpm.dmn.feel.impl.scala.function.CustomFunctionTransformer;
import org.camunda.bpm.dmn.feel.impl.scala.function.FeelCustomFunctionProvider;
import org.camunda.bpm.dmn.feel.impl.scala.spin.SpinValueMapperFactory;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.feel.FeelEngine$;
import org.camunda.feel.FeelEngine.Builder;
import org.camunda.feel.FeelEngine.Failure;
import org.camunda.feel.context.CustomContext;
import org.camunda.feel.context.VariableProvider;
import org.camunda.feel.context.VariableProvider.StaticVariableProvider;
import org.camunda.feel.impl.JavaValueMapper;
import org.camunda.feel.valuemapper.CustomValueMapper;
import org.camunda.feel.valuemapper.ValueMapper.CompositeValueMapper;
import camundajar.impl.scala.collection.immutable.List;
import camundajar.impl.scala.collection.immutable.Map;
import camundajar.impl.scala.runtime.BoxesRunTime;
import camundajar.impl.scala.util.Either;
import camundajar.impl.scala.util.Left;
import camundajar.impl.scala.util.Right;

import java.util.Arrays;

import static org.camunda.feel.context.VariableProvider.CompositeVariableProvider;
import static camundajar.impl.scala.jdk.CollectionConverters.ListHasAsScala;

public class ScalaFeelEngine implements FeelEngine {

  protected static final String INPUT_VARIABLE_NAME = "inputVariableName";

  protected static final ScalaFeelLogger LOGGER = ScalaFeelLogger.LOGGER;

  protected org.camunda.feel.FeelEngine feelEngine;

  public ScalaFeelEngine(java.util.List<FeelCustomFunctionProvider> functionProviders) {
    List<CustomValueMapper> valueMappers = getValueMappers();

    CompositeValueMapper compositeValueMapper = new CompositeValueMapper(valueMappers);

    CustomFunctionTransformer customFunctionTransformer =
      new CustomFunctionTransformer(functionProviders, compositeValueMapper);

    feelEngine = buildFeelEngine(customFunctionTransformer, compositeValueMapper);
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

      throw LOGGER.evaluationException(message);

    }
  }

  public boolean evaluateSimpleUnaryTests(String expression,
                                          String inputVariable,
                                          VariableContext variableContext) {
    Map inputVariableMap = new Map.Map1(INPUT_VARIABLE_NAME, inputVariable);

    StaticVariableProvider inputVariableContext = new StaticVariableProvider(inputVariableMap);

    ContextVariableWrapper contextVariableWrapper = new ContextVariableWrapper(variableContext);

    CustomContext context = new CustomContext() {
      public VariableProvider variableProvider() {
        return new CompositeVariableProvider(toScalaList(inputVariableContext, contextVariableWrapper));
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

      throw LOGGER.evaluationException(message);

    }
  }

  protected List<CustomValueMapper> getValueMappers() {
    SpinValueMapperFactory spinValueMapperFactory = new SpinValueMapperFactory();

    CustomValueMapper javaValueMapper = new JavaValueMapper();

    CustomValueMapper spinValueMapper = spinValueMapperFactory.createInstance();
    if (spinValueMapper != null) {
      return toScalaList(javaValueMapper, spinValueMapper);

    } else {
      return toScalaList(javaValueMapper);

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

  protected org.camunda.feel.FeelEngine buildFeelEngine(CustomFunctionTransformer transformer,
                                                        CompositeValueMapper valueMapper) {
    return new Builder()
      .functionProvider(transformer)
      .valueMapper(valueMapper)
      .enableExternalFunctions(false)
      .build();
  }

}

