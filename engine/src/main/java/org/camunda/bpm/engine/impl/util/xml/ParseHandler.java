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
package org.camunda.bpm.engine.impl.util.xml;

import java.util.ArrayDeque;
import java.util.Deque;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author Tom Baeyens
 */
public class ParseHandler extends DefaultHandler {

  protected String defaultNamespace;
  protected Parse parse;
  protected Locator locator;
  protected Deque<Element> elementStack = new ArrayDeque<>();

  public ParseHandler(Parse parse) {
    this.parse = parse;
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    Element element = new Element(uri, localName, qName, attributes, locator);
    if (elementStack.isEmpty()) {
      parse.rootElement = element;
    } else {
      elementStack.peek().add(element);
    }
    elementStack.push(element);
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    elementStack.peek().appendText(String.valueOf(ch, start, length));
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    elementStack.pop();
  }

  public void error(SAXParseException e) {
    parse.addError(e);
  }
  public void fatalError(SAXParseException e) {
    parse.addError(e);
  }
  public void warning(SAXParseException e) {
    parse.addWarning(e);
  }
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void setDefaultNamespace(String defaultNamespace) {
    this.defaultNamespace = defaultNamespace;
  }


}
