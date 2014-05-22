/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.spin.impl.xml.dom;

import org.camunda.spin.logging.SpinLogger;

/**
 * @author Daniel Meyer
 *
 */
public class XmlDomLogger extends SpinLogger {

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

  public XmlDomDataFormatException unableToCreateParser(Exception cause) {
    return new XmlDomDataFormatException(exceptionMessage("005", "Unable to create DocumentBuilder"), cause);
  }

  public SpinXmlDomAttributeException unableToFindAttributeWithNamespaceAndName(String namespace, String attributeName) {
    return new SpinXmlDomAttributeException(exceptionMessage("006", "Unable to find attribute with namespace '{}' and name '{}'", namespace, attributeName));
  }

  public SpinXmlDomElementException unableToFindChildElementWithNamespaceAndName(String namespace, String elementName) {
    return new SpinXmlDomElementException(exceptionMessage("007", "Unable to find child element with namespace '{}' and name '{}'", namespace, elementName));
  }

  public SpinXmlDomElementException moreThanOneChildElementFoundForNamespaceAndName(String namespace, String elementName) {
    return new SpinXmlDomElementException(exceptionMessage("008", "More than one child element was found for namespace '{}' and name '{}'", namespace, elementName));
  }

  public XmlDomDataFormatException unableToParseInput(Exception e) {
    return new XmlDomDataFormatException(exceptionMessage("009", "Unable to parse input into DOM document"), e);
  }

}
