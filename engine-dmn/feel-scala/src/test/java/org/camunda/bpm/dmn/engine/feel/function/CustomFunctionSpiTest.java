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
package org.camunda.bpm.dmn.engine.feel.function;

import org.camunda.bpm.dmn.engine.feel.function.helper.NonSpiFunctionProvider;
import org.camunda.bpm.dmn.engine.feel.function.helper.SpiFunctionProvider;
import org.camunda.bpm.dmn.feel.impl.scala.CamundaFeelEngine;
import org.camunda.bpm.dmn.feel.impl.scala.function.FeelCustomFunctionProvider;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.feel.impl.spi.SpiServiceLoader;
import org.camunda.feel.interpreter.FunctionProvider;
import org.camunda.feel.interpreter.FunctionProvider.CompositeFunctionProvider;
import org.camunda.feel.interpreter.impl.ValueMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import scala.collection.IterableOnceOps;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpiServiceLoader.class})
@PowerMockIgnore("javax.management.*")
public class CustomFunctionSpiTest {

  protected List<FeelCustomFunctionProvider> nonSpiFunctionProviders = new ArrayList<>();

  @Before
  public void setUp() {
    nonSpiFunctionProviders.clear();

    mockStatic(SpiServiceLoader.class);
    when(SpiServiceLoader.loadValueMapper()).thenReturn(ValueMapper.defaultValueMapper());
  }

  @Test
  public void shouldRegisterOneFunctionViaSpiAndOneNonSpi() {
    // given
    SpiFunctionProvider spiFunctionProvider = new SpiFunctionProvider("mySpiFunction", "bar");
    when(SpiServiceLoader.loadFunctionProvider()).thenReturn(spiFunctionProvider);

    NonSpiFunctionProvider functionProvider = new NonSpiFunctionProvider("myNonSpiFunction", "foo");
    nonSpiFunctionProviders.add(functionProvider);

    // when
    String result = evaluateExpression("mySpiFunction() + myNonSpiFunction()");

    // then
    assertThat(result).isEqualTo("barfoo");
  }

  @Test
  public void shouldRegisterOneFunctionViaSpiAndNoNonSpi() {
    // given
    SpiFunctionProvider spiFunctionProvider = new SpiFunctionProvider("mySpiFunction", "bar");
    when(SpiServiceLoader.loadFunctionProvider()).thenReturn(spiFunctionProvider);

    // when
    String result = evaluateExpression("mySpiFunction()");

    // then
    assertThat(result).isEqualTo("bar");
  }

  @Test
  public void shouldRegisterTwoFunctionsViaSpiAndOneNonSpi() {
    // given
    List<FunctionProvider> spiFunctionProviders = new ArrayList<>();
    spiFunctionProviders.add(new SpiFunctionProvider("mySpiFunctionA", "foo"));
    spiFunctionProviders.add(new SpiFunctionProvider("mySpiFunctionB", "bar"));

    CompositeFunctionProvider compositeFunctionProvider =
      new CompositeFunctionProvider(toScalaList(spiFunctionProviders));

    when(SpiServiceLoader.loadFunctionProvider()).thenReturn(compositeFunctionProvider);

    NonSpiFunctionProvider functionProvider = new NonSpiFunctionProvider("myNonSpiFunction", "baz");
    nonSpiFunctionProviders.add(functionProvider);

    // when
    String result = evaluateExpression("mySpiFunctionA() + mySpiFunctionB() + " +
      "myNonSpiFunction()");

    // then
    assertThat(result).isEqualTo("foobarbaz");
  }

  @Test
  public void shouldRegisterTwoFunctionsViaSpiAndNoNonSpi() {
    // given
    List<FunctionProvider> spiFunctionProviders = new ArrayList<>();
    spiFunctionProviders.add(new SpiFunctionProvider("mySpiFunctionA", "foo"));
    spiFunctionProviders.add(new SpiFunctionProvider("mySpiFunctionB", "bar"));

    CompositeFunctionProvider compositeFunctionProvider =
      new CompositeFunctionProvider(toScalaList(spiFunctionProviders));

    when(SpiServiceLoader.loadFunctionProvider()).thenReturn(compositeFunctionProvider);

    // when
    String result = evaluateExpression("mySpiFunctionA() + mySpiFunctionB()");

    // then
    assertThat(result).isEqualTo("foobar");
  }

  @Test
  public void shouldRegisterNoFunctionsViaSpiAndOneNonSpi() {
    // given
    NonSpiFunctionProvider functionProvider = new NonSpiFunctionProvider("myNonSpiFunction", "foo");
    nonSpiFunctionProviders.add(functionProvider);

    when(SpiServiceLoader.loadFunctionProvider())
      .thenReturn(FunctionProvider.EmptyFunctionProvider$.MODULE$);

    // when
    String result = evaluateExpression("myNonSpiFunction()");

    // then
    assertThat(result).isEqualTo("foo");
  }

  // helper ////////////////////////////////////////////////////////////////////////////////////////

  protected <T> T evaluateExpression(String expression) {
    CamundaFeelEngine feelEngine = new CamundaFeelEngine(nonSpiFunctionProviders);

    VariableContext variableCtx = Variables.putValue("variable", null).asVariableContext();

    return feelEngine.evaluateSimpleExpression(expression, variableCtx);
  }

  protected <T> scala.collection.immutable.List toScalaList(List<T> list) {
    Iterator<T> iterator = list.iterator();
    IterableOnceOps iterableOnceOps = JavaConverters.asScalaIteratorConverter(iterator).asScala();
    return iterableOnceOps.toList();
  }

}
