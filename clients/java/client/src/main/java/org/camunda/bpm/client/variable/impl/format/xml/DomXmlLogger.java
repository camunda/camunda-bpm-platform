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
package org.camunda.bpm.client.variable.impl.format.xml;

import org.camunda.bpm.client.exception.DataFormatException;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.w3c.dom.Node;

public class DomXmlLogger extends ExternalTaskClientLogger {

  public void usingDocumentBuilderFactory(String name) {
    logDebug("001", "Using document builder factory '{}'", name);
  }

  public void createdDocumentBuilder() {
    logDebug("002", "Successfully created new document builder");
  }

  public void documentBuilderFactoryConfiguration(String property, String value) {
    logDebug("003", "DocumentBuilderFactory configuration '{}' '{}'", property, value);
  }

  public void parsingInput() {
    logDebug("004", "Parsing input into DOM document.");
  }

  public DataFormatException unableToCreateParser(Exception cause) {
    return new DataFormatException(exceptionMessage("005", "Unable to create DocumentBuilder"), cause);
  }

  public DataFormatException unableToParseInput(Exception e) {
    return new DataFormatException(exceptionMessage("006", "Unable to parse input into DOM document"), e);
  }

  public DataFormatException unableToCreateTransformer(Exception cause) {
    return new DataFormatException(exceptionMessage("007", "Unable to create a transformer to write element"), cause);
  }

  public DataFormatException unableToTransformElement(Node element, Exception cause) {
    return new DataFormatException(exceptionMessage("008", "Unable to transform element '{}:{}'", element.getNamespaceURI(), element.getNodeName()), cause);
  }

  public DataFormatException unableToWriteInput(Object parameter, Throwable cause) {
    return new DataFormatException(exceptionMessage("009", "Unable to write object '{}' to xml element", parameter.toString()), cause);
  }

  public DataFormatException unableToDeserialize(Object node, String canonicalClassName, Throwable cause) {
    return new DataFormatException(
      exceptionMessage("010", "Cannot deserialize '{}...' to java class '{}'", node.toString(), canonicalClassName), cause);
  }

  public DataFormatException unableToCreateMarshaller(Throwable cause) {
    return new DataFormatException(exceptionMessage("011", "Cannot create marshaller"), cause);
  }

  public DataFormatException unableToCreateContext(Throwable cause) {
    return new DataFormatException(exceptionMessage("012", "Cannot create context"), cause);
  }

  public DataFormatException unableToCreateUnmarshaller(Throwable cause) {
    return new DataFormatException(exceptionMessage("013", "Cannot create unmarshaller"), cause);
  }

  public DataFormatException classNotFound(String classname, ClassNotFoundException cause) {
    return new DataFormatException(exceptionMessage("014", "Class {} not found ", classname), cause);
  }

}
