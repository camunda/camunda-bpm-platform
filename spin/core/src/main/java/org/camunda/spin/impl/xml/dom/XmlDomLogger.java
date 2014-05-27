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

import org.camunda.spin.spi.SpinXmlDataFormatException;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.xml.tree.SpinXmlTreeAttributeException;
import org.camunda.spin.xml.tree.SpinXmlTreeElementException;

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

  public SpinXmlDataFormatException unableToCreateParser(Exception cause) {
    return new SpinXmlDataFormatException(exceptionMessage("005", "Unable to create DocumentBuilder"), cause);
  }

  public SpinXmlTreeAttributeException unableToFindAttributeWithNamespaceAndName(String namespace, String attributeName) {
    return new SpinXmlTreeAttributeException(exceptionMessage("006", "Unable to find attribute with namespace '{}' and name '{}'", namespace, attributeName));
  }

  public SpinXmlTreeElementException unableToFindChildElementWithNamespaceAndName(String namespace, String elementName) {
    return new SpinXmlTreeElementException(exceptionMessage("007", "Unable to find child element with namespace '{}' and name '{}'", namespace, elementName));
  }

  public SpinXmlTreeElementException moreThanOneChildElementFoundForNamespaceAndName(String namespace, String elementName) {
    return new SpinXmlTreeElementException(exceptionMessage("008", "More than one child element was found for namespace '{}' and name '{}'", namespace, elementName));
  }

  public SpinXmlDataFormatException unableToParseInput(Exception e) {
    return new SpinXmlDataFormatException(exceptionMessage("009", "Unable to parse input into DOM document"), e);
  }

  public SpinXmlTreeAttributeException unableToCreateAttributeWithNullName() {
    return new SpinXmlTreeAttributeException(exceptionMessage("010", "Unable to create attribute with name 'null'"));
  }

  public SpinXmlTreeAttributeException unableToSetAttributeValueToNull(String namespace, String attributeName) {
    return new SpinXmlTreeAttributeException(exceptionMessage("011", "Unable to set value of attribute of namespace '{}' and '{}' to 'null'", namespace, attributeName));
  }

  public SpinXmlTreeAttributeException unableToCheckAttributeWithNullName() {
    return new SpinXmlTreeAttributeException(exceptionMessage("012", "Unable to check for existence of attribute with name 'null'"));
  }

  public SpinXmlTreeAttributeException unableToRemoveAttributeWithNullName() {
    return new SpinXmlTreeAttributeException(exceptionMessage("013", "Unable to remove attribute with name 'null'"));
  }

  public SpinXmlTreeElementException unableToAdoptElement(String namespace, String name) {
    return new SpinXmlTreeElementException(exceptionMessage("014", "Unable to adopt element with namespace '{}' and name '{}'", namespace, name));
  }

}
