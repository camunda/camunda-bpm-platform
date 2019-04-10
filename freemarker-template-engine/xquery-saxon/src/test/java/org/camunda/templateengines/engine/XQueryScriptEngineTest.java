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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.camunda.commons.utils.IoUtil;
import org.camunda.templateengines.XQueryOperator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XQueryScriptEngineTest {

  protected static ScriptEngine scriptEngine;
  protected Bindings bindings;

  private static String booksStylesheet = "/xquery/book.xquery";
  private static String xmlResource = "xml/books.xml";

  private XQueryOperator variableXQueryOperator;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void load() throws Exception {
    variableXQueryOperator = XQueryOperator.builder().withStylesheetResource(booksStylesheet).build();
  }

  private Document getDocument(String resource) throws ParserConfigurationException, SAXException, IOException {
    String resourceString = IoUtil.fileAsString(resource);

    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    domFactory.setNamespaceAware(true);
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    return builder.parse(new InputSource(new StringReader(resourceString)));
  }

  @BeforeClass
  public static void getScriptEngine() {
    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    scriptEngine = scriptEngineManager.getEngineByName("xquery");
  }

  @Before
  public void createBindings() {
    bindings = new SimpleBindings();
  }

  protected String evaluate(String template) throws ScriptException {
    return (String) scriptEngine.eval(template, bindings);
  }

  @Test
  public void testScriptEngineExists() {
    assertThat(scriptEngine).isNotNull();
  }

  @Test
  public void testTransformDocumentForFirstTitle() throws Exception {
    String result = variableXQueryOperator.evaluateToString("myDocument", getDocument(xmlResource));
    assertThat(result).containsSequence("XQuery");
  }
}
