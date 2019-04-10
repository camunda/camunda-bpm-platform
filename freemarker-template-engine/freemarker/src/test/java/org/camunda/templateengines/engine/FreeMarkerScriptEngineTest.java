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
package org.camunda.templateengines.engine;

import java.util.Arrays;
import java.util.Collection;

import javax.script.*;

import org.camunda.templateengines.engine.util.Greeter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Sebastian Menski
 */
public class FreeMarkerScriptEngineTest {

  protected static ScriptEngine scriptEngine;
  protected Bindings bindings;

  protected String template;
  protected String expected;

  @BeforeClass
  public static void getScriptEngine() {
    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    scriptEngine = scriptEngineManager.getEngineByName("freemarker");
  }

  @Before
  public void createBindings() {
    bindings = new SimpleBindings();
  }

  @After
  public void checkResult() throws ScriptException {
    if (template != null) {
      assertThat(evaluate(template)).isEqualTo(expected);
    }
  }

  protected String evaluate(String template) throws ScriptException {
    return (String) scriptEngine.eval(template, bindings);
  }

  @Test
  public void testScriptEngineExists() {
    assertThat(scriptEngine).isNotNull();
  }

  @Test
  public void testVariableExpansion() {
    bindings.put("name", "world");
    expected = "Hello world!";
    template = "Hello ${name}!";
  }

  @Test
  public void testJavaProperties() {
    bindings.put("greeter", new Greeter());
    expected = "!";
    template = "${greeter.suffix}";
  }

  @Test
  public void testJavaMethodCall() throws ScriptException {
    bindings.put("greeter", new Greeter());
    expected = "Hello world!";
    template = "${greeter.hello('world')}";
  }

  @Test
  public void testJavaArrays() throws ScriptException {
    bindings.put("myarray", new String[]{"hello", "foo", "world", "bar"});
    expected = "4 hello world!";
    template = "${myarray?size} ${myarray[0]} ${myarray[2]}!";
  }

  @Test
  public void testJavaBoolean() throws ScriptException {
    bindings.put("mybool", false);
    expected = "Hello world!";
    template = "<#if mybool>okey<#else>Hello world!</#if>";
  }

  @Test
  public void testJavaInteger() throws ScriptException {
    bindings.put("myint", 6);
    expected = "42";
    template = "<#assign myint = myint + 36>${myint}";
  }

  @Test
  public void testJavaCollection() throws ScriptException {
    Collection names = Arrays.asList("tweety", "duffy", "tom");
    bindings.put("names", names);
    expected = "tweety, duffy, tom";
    template = "<#list names as name>${name}<#if name_has_next>, </#if></#list>";
  }

  @Test
  public void testDefineBlock() throws ScriptException {
    bindings.put("who", "world");
    expected = "Hello world!";
    template = "<#macro block>Hello ${who}!</#macro><@block/>";
  }
  
  @Test
  public void testFailingEvaluation() {
    try {
      String invalidTemplate = "${}";
      evaluate(invalidTemplate);
      fail("Expected a ScriptException");
    } catch (ScriptException e) {
      // happy path
    }
  }

}
