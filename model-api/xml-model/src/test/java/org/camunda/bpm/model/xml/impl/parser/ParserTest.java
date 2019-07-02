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
package org.camunda.bpm.model.xml.impl.parser;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import org.camunda.bpm.model.xml.ModelParseException;
import org.camunda.bpm.model.xml.testmodel.TestModelParser;
import org.junit.Test;

public class ParserTest {

  @Test
  public void shouldThrowExceptionForTooManyAttributes() {
    TestModelParser modelParser = new TestModelParser();
    String testXml = "org/camunda/bpm/model/xml/impl/parser/FeatureSecureProcessing.xml";
    InputStream testXmlAsStream = this.getClass().getClassLoader().getResourceAsStream(testXml);
    try {
      modelParser.parseModelFromStream(testXmlAsStream);
    } catch (ModelParseException mpe) {
      assertThat(mpe.getMessage(), is("SAXException while parsing input stream"));
      assertThat(mpe.getCause().getMessage(), containsString("JAXP00010002"));
    }
  }

  @Test
  public void shouldNotOverrideExternalSchemaAccess() {
    System.setProperty("javax.xml.accessExternalSchema", "");
    try {
      TestModelParser modelParser = new TestModelParser();
      String testXml = "org/camunda/bpm/model/xml/impl/parser/ExternalSchemaAccess.xml";
      InputStream testXmlAsStream = this.getClass().getClassLoader().getResourceAsStream(testXml);
      modelParser.parseModelFromStream(testXmlAsStream);
    } finally {
      System.clearProperty("javax.xml.accessExternalSchema");
    }
  }

  @Test
  public void shouldThrowExceptionForDoctype() {
    TestModelParser modelParser = new TestModelParser();
    String testXml = "org/camunda/bpm/model/xml/impl/parser/XxeProcessing.xml";
    InputStream testXmlAsStream = this.getClass().getClassLoader().getResourceAsStream(testXml);
    try {
      modelParser.parseModelFromStream(testXmlAsStream);
    } catch (ModelParseException mpe) {
      assertThat(mpe.getMessage(), is("SAXException while parsing input stream"));
      assertThat(mpe.getCause().getMessage(), containsString("DOCTYPE"));
      assertThat(mpe.getCause().getMessage(), containsString("http://apache.org/xml/features/disallow-doctype-decl"));
    }
  }

}
