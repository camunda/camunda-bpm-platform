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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.dmn.engine.feel.function.helper.FunctionProvider;
import org.camunda.bpm.dmn.engine.feel.function.helper.MyPojo;
import org.camunda.bpm.dmn.engine.feel.helper.FeelRule;
import org.camunda.bpm.dmn.feel.impl.FeelException;
import org.camunda.bpm.dmn.feel.impl.scala.function.CustomFunction;
import org.camunda.bpm.dmn.feel.impl.scala.function.builder.CustomFunctionBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class CustomFunctionTest {

  protected FeelRule feelRule = FeelRule.buildWithFunctionProvider();
  protected ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(feelRule).around(thrown);

  protected FunctionProvider functionProvider;

  @Before
  public void assign() {
    functionProvider = feelRule.getFunctionProvider();
  }

  @Test
  public void shouldThrowExceptionBothFunctionAndReturnValueSet() {
    // given
    CustomFunctionBuilder myFunctionBuilder = CustomFunction.create()
      .setParams("x")
      .setFunction(args -> "")
      .setReturnValue("foo");

    // then
    thrown.expect(FeelException.class);
    thrown.expectMessage("Only set one return value or a function.");

    // when
    myFunctionBuilder.build();
  }

  @Test
  public void shouldSetMultipleArgs() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setParams("x", "y", "z")
      .setFunction(args -> {
        String argX = (String) args.get(0);
        boolean argY = (boolean) args.get(1);
        List<String> argZ = (List<String>) args.get(2);

        return argX + "-" + argY + "-" + argZ;
      })
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    String result = feelRule.evaluateExpression("myFunction(\"foo\", true, [\"elem\"])");

    // then
    assertThat(result).isEqualTo("foo-true-[elem]");
  }

  @Test
  public void shouldRegisterMultipleFunctions() {
    // given
    CustomFunction myFunctionA = CustomFunction.create()
      .setReturnValue("A")
      .build();

    CustomFunction myFunctionB = CustomFunction.create()
      .setReturnValue("B")
      .build();

    CustomFunction myFunctionC = CustomFunction.create()
      .setReturnValue("C")
      .build();

    functionProvider.register("myFunctionA", myFunctionA);
    functionProvider.register("myFunctionB", myFunctionB);
    functionProvider.register("myFunctionC", myFunctionC);

    // when
    String result = feelRule.evaluateExpression("myFunctionA()+myFunctionB()+myFunctionC()");

    // then
    assertThat(result).isEqualTo("ABC");
  }

  @Test
  public void shouldCallBean() {
    // given
    MyPojo testBean = new MyPojo(3);
    CustomFunction myFunction = CustomFunction.create()
      .setFunction(args -> testBean.call(2))
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    long result = feelRule.evaluateExpression("myFunction()");

    // then
    assertThat(result).isEqualTo(6);
  }

  @Test
  public void shouldPassInteger() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setParams("x")
      .setFunction(args -> {
        Object argX = args.get(0);

        // then
        assertThat(argX).isEqualTo((long) 12);

        return "";
      })
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    feelRule.evaluateExpression("myFunction(variable)", 12);
  }

  @Test
  public void shouldPassDouble() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setParams("x")
      .setFunction(args -> {
        Object argX = args.get(0);

        // then
        assertThat(argX).isEqualTo((double)12.1);

        return "";
      })
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    feelRule.evaluateExpression("myFunction(variable)", 12.1);
  }

  @Test
  public void shouldPassString() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setParams("x")
      .setFunction(args -> {
        Object argX = args.get(0);

        // then
        assertThat(argX).isEqualTo("foo");

        return "";
      })
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    feelRule.evaluateExpression("myFunction(variable)", "foo");
  }

  @Test
  public void shouldPassNull() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setParams("x")
      .setFunction(args -> {
        Object argX = args.get(0);

        // then
        assertThat(argX).isNull();

        return "";
      })
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    feelRule.evaluateExpression("myFunction(variable)", null);
  }

  @Test
  public void shouldPassTrue() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setParams("x")
      .setFunction(args -> {
        Object argX = args.get(0);

        // then
        assertThat(argX).isEqualTo(true);

        return "";
      })
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    feelRule.evaluateExpression("myFunction(variable)", true);
  }

  @Test
  public void shouldPassDate() {
    // given
    Date now = new Date();
    LocalDateTime localDateTime = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());

    CustomFunction myFunction = CustomFunction.create()
      .setParams("x")
      .setFunction(args -> {
        Object argX = args.get(0);

        // then
        assertThat(argX).isEqualTo(localDateTime);

        return "";
      })
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    feelRule.evaluateExpression("myFunction(variable)", now);
  }

  @Test
  public void shouldPassList() {
    // given
    List<String> list = Arrays.asList("foo", "bar", "bazz");

    CustomFunction myFunction = CustomFunction.create()
      .setParams("x")
      .setFunction(args -> {
        Object argX = args.get(0);

        // then
        assertThat(argX).isEqualTo(list);

        return "";
      })
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    feelRule.evaluateExpression("myFunction(variable)", list);
  }

  @Test
  public void shouldPassMap() {
    // given
    Map<String, String> map = Collections.singletonMap("foo", "bar");

    CustomFunction myFunction = CustomFunction.create()
      .setParams("x")
      .setFunction(args -> {
        Object argX = args.get(0);

        // then
        assertThat(argX).isEqualTo(map);

        return "";
      })
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    feelRule.evaluateExpression("myFunction(variable)", map);
  }

  @Test
  public void shouldReturnString() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setReturnValue("foo")
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    String result = feelRule.evaluateExpression("myFunction()");

    // then
    assertThat(result).isEqualTo("foo");
  }

  @Test
  public void shouldReturnDouble() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setReturnValue(1.7976931348623157)
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    double result = feelRule.evaluateExpression("myFunction()");

    // then
    assertThat(result).isEqualTo(1.7976931348623157);
  }

  @Test
  public void shouldReturnNull() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setReturnValue(null)
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    Object result = feelRule.evaluateExpression("myFunction()");

    // then
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnTrue() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setReturnValue(true)
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    boolean result = feelRule.evaluateExpression("myFunction()");

    // then
    assertThat(result).isTrue();
  }

  @Test
  public void shouldReturnDate() {
    // given
    Date now = new Date();

    CustomFunction myFunction = CustomFunction.create()
      .setReturnValue(now)
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    LocalDateTime result = feelRule.evaluateExpression("myFunction()");

    LocalDateTime localDateTime = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());

    // then
    assertThat(result).isEqualTo(localDateTime);
  }

  @Test
  public void shouldReturnList() {
    // given
    List<String> list = Arrays.asList("foo", "bar", "bazz");

    CustomFunction myFunction = CustomFunction.create()
      .setReturnValue(list)
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    List<String> result = feelRule.evaluateExpression("myFunction()");

    // then
    assertThat(result).containsExactly("foo", "bar", "bazz");
  }

  @Test
  public void shouldReturnList_Nested() {
    // given
    List<Object> list = Arrays.asList("foo", Arrays.asList("bar", "bazz"));

    CustomFunction myFunction = CustomFunction.create()
      .setReturnValue(list)
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    List<Object> result = feelRule.evaluateExpression("myFunction()");

    // then
    assertThat(result).containsExactly("foo", Arrays.asList("bar", "bazz"));
  }

  @Test
  public void shouldReturnMap() {
    // given
    Map<String, String> map = Collections.singletonMap("foo", "bar");

    CustomFunction myFunction = CustomFunction.create()
      .setReturnValue(map)
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    Map<String, String> result = feelRule.evaluateExpression("myFunction()");

    // then
    assertThat(result).containsExactly(entry("foo", "bar"));
  }

  @Test
  public void shouldReturnMap_Nested() {
    // given
    Map<String, Object> map = Collections.singletonMap("foo",
      Collections.singletonMap("bar", "bazz"));

    CustomFunction myFunction = CustomFunction.create()
      .setReturnValue(map)
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    Map<String, Object> result = feelRule.evaluateExpression("myFunction()");

    // then
    assertThat(result).containsExactly(entry("foo", Collections.singletonMap("bar", "bazz")));
  }

  @Test
  public void shouldPassVarargs() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .enableVarargs()
      .setFunction(args -> args)
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    List<String> result = feelRule.evaluateExpression("myFunction(\"foo\", \"bar\", \"baz\")");

    // then
    assertThat(result).containsExactly("foo", "bar", "baz");
  }

  @Test
  public void shouldThrowExceptionDueToDisabledVarargs() {
    // given
    CustomFunction myFunction = CustomFunction.create()
      .setFunction(args -> args)
      .build();

    functionProvider.register("myFunction", myFunction);

    // when
    var result = feelRule.evaluateExpression("myFunction(\"foo\", \"bar\", \"baz\")");

    // then
    assertThat(result).isNull();
  }

  @Test
  public void shouldPassExplicitParamsAndVarargs() {
    // given
    CustomFunction customFunction = CustomFunction.create()
      .enableVarargs()
      .setParams("x", "y")
      .setFunction(args -> args)
      .build();

    functionProvider.register("myFunction", customFunction);

    // when
    List<Object> result = feelRule.evaluateExpression("myFunction(\"foo\", \"bar\", \"baz\")");

    // then
    assertThat(result).containsExactly("foo", Arrays.asList("bar", "baz"));
  }

  @Test
  public void shouldPassExplicitParamsWithVarargsEnabled() {
    // given
    CustomFunction customFunction = CustomFunction.create()
      .enableVarargs()
      .setParams("x", "y")
      .setFunction(args -> args)
      .build();

    functionProvider.register("myFunction", customFunction);

    // when
    List<Object> result = feelRule.evaluateExpression("myFunction(y: \"bar\", x: \"foo\")");

    // then
    assertThat(result).containsExactly("foo", "bar");
  }

}
