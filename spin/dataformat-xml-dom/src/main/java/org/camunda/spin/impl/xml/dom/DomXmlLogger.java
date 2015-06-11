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

import java.util.NoSuchElementException;

import org.camunda.commons.logging.BaseLogger;
import org.camunda.spin.impl.logging.SpinLogger;
import org.camunda.spin.xml.SpinXPathException;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlAttributeException;
import org.camunda.spin.xml.SpinXmlDataFormatException;
import org.camunda.spin.xml.SpinXmlElement;
import org.camunda.spin.xml.SpinXmlElementException;
import org.camunda.spin.xml.SpinXmlElementImplementationException;
import org.w3c.dom.Node;

/**
 * @author Daniel Meyer
 *
 */
public class DomXmlLogger extends SpinLogger {

  public static final String PROJECT_CODE = SpinLogger.PROJECT_CODE + "/DOM-XML";
  public static final DomXmlLogger XML_DOM_LOGGER = BaseLogger.createLogger(DomXmlLogger.class, PROJECT_CODE, "org.camunda.spin.xml", "01");

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

  public SpinXmlAttributeException unableToFindAttributeWithNamespaceAndName(String namespace, String attributeName) {
    return new SpinXmlAttributeException(exceptionMessage("006", "Unable to find attribute with namespace '{}' and name '{}'", namespace, attributeName));
  }

  public SpinXmlElementException unableToFindChildElementWithNamespaceAndName(String namespace, String elementName) {
    return new SpinXmlElementException(exceptionMessage("007", "Unable to find child element with namespace '{}' and name '{}'", namespace, elementName));
  }

  public SpinXmlElementException moreThanOneChildElementFoundForNamespaceAndName(String namespace, String elementName) {
    return new SpinXmlElementException(exceptionMessage("008", "More than one child element was found for namespace '{}' and name '{}'", namespace, elementName));
  }

  public SpinXmlDataFormatException unableToParseInput(Exception e) {
    return new SpinXmlDataFormatException(exceptionMessage("009", "Unable to parse input into DOM document"), e);
  }

  public SpinXmlAttributeException unableToSetAttributeValueToNull(String namespace, String attributeName) {
    return new SpinXmlAttributeException(exceptionMessage("010", "Unable to set value of the attribute '{}:{}' to 'null'", namespace, attributeName));
  }

  public SpinXmlElementException unableToAdoptElement(SpinXmlElement elementToAdopt) {
    return new SpinXmlElementException(exceptionMessage("011", "Unable to adopt element '{}:{}'", elementToAdopt.namespace(), elementToAdopt.name()));
  }

  public UnsupportedOperationException methodNotSupportedByClass(String methodName, Class<?> implementationClass) {
    return new UnsupportedOperationException(exceptionMessage("012", "The method '{}' is not implemented by class '{}'", methodName, implementationClass.getName()));
  }

  public NoSuchElementException iteratorHasNoMoreElements(Class<?> iteratorClass) {
    return new NoSuchElementException(exceptionMessage("013", "The iterator '{}' has no more elements", iteratorClass.getName()));
  }

  public SpinXmlElementException elementHasNoParent(SpinXmlElement element) {
    return new SpinXmlElementException(exceptionMessage("014", "The element '{}:{}' has no parent element.", element.namespace(), element.name()));
  }

  public SpinXmlElementImplementationException unableToReplaceElementInImplementation(SpinXmlElement existingElement, SpinXmlElement newElement, Exception cause) {
    return new SpinXmlElementImplementationException(exceptionMessage("015", "Unable to replace the existing element '{}:{}' by the new element '{}:{}' in the underlying implementation",
      existingElement.namespace(), existingElement.name(), newElement.namespace(), newElement.name()), cause);
  }

  public SpinXmlElementImplementationException unableToSetAttributeInImplementation(SpinXmlElement element, String namespace, String attributeName, String value, Exception cause) {
    return new SpinXmlElementImplementationException(exceptionMessage("016", "Unable to set attribute '{}:{}' to value '{}' on element '{}:{}' in the underlying implementation",
      namespace, attributeName, value, element.namespace(), element.name()), cause);
  }

  public SpinXmlElementImplementationException unableToAppendElementInImplementation(SpinXmlElement element, SpinXmlElement childElement, Exception cause) {
    return new SpinXmlElementImplementationException(exceptionMessage("017", "Unable to append new child element '{}:{}' to element '{}:{}' in the underlying implementation",
      childElement.namespace(), childElement.name(), element.namespace(), element.name()), cause);
  }

  public SpinXmlElementImplementationException unableToInsertElementInImplementation(SpinXmlElement element, SpinXmlElement childElement, Exception cause) {
    return new SpinXmlElementImplementationException(exceptionMessage("018", "Unable to insert new child element '{}:{}' to element '{}:{}' in the underlying implementation",
      childElement.namespace(), childElement.name(), element.namespace(), element.name()), cause);
  }

  public SpinXmlElementImplementationException unableToRemoveChildInImplementation(SpinXmlElement element, SpinXmlElement childElement, Exception cause) {
    return new SpinXmlElementImplementationException(exceptionMessage("019", "Unable to remove child element '{}:{}' from element '{}:{}' in the underlying implementation",
      childElement.namespace(), childElement.name(), element.namespace(), element.name()), cause);
  }

  public SpinXmlAttributeException unableToWriteAttribute(SpinXmlAttribute attribute, Exception cause) {
    return new SpinXmlAttributeException(exceptionMessage("020", "Unable to write attribute '{}:{}'", attribute.namespace(), attribute.name()), cause);
  }

  public SpinXmlElementException unableToCreateTransformer(Exception cause) {
    return new SpinXmlElementException(exceptionMessage("021", "Unable to create a transformer to write element"), cause);
  }

  public SpinXmlElementException unableToTransformElement(Node element, Exception cause) {
    return new SpinXmlElementException(exceptionMessage("022", "Unable to transform element '{}:{}'", element.getNamespaceURI(), element.getNodeName()), cause);
  }

  public SpinXPathException unableToEvaluateXPathExpressionOnElement(SpinXmlElement element, Exception cause) {
    return new SpinXPathException(exceptionMessage("024", "Unable to evaluate XPath expression on element '{}:{}'", element.namespace(), element.name()), cause);
  }

  public SpinXPathException unableToCastXPathResultTo(Class<?> castClass, Exception cause) {
    return new SpinXPathException(exceptionMessage("025", "Unable to cast XPath expression to class '{}'", castClass.getName()), cause);
  }

  public SpinXmlDataFormatException unableToDetectCanonicalType(Object parameter) {
    return new SpinXmlDataFormatException(exceptionMessage("026", "Cannot detect canonical data type for parameter '{}'", parameter));
  }

  public SpinXmlDataFormatException unableToMapInput(Object parameter, Throwable cause) {
    return new SpinXmlDataFormatException(exceptionMessage("027", "Unable to map object '{}' to xml element", parameter.toString()), cause);
  }

  public SpinXmlDataFormatException unableToDeserialize(Object node, String canonicalClassName, Throwable cause) {
    return new SpinXmlDataFormatException(
      exceptionMessage("028", "Cannot deserialize '{}...' to java class '{}'", node.toString(), canonicalClassName), cause);
  }

  public SpinXmlDataFormatException unableToCreateMarshaller(Throwable cause) {
    return new SpinXmlDataFormatException(exceptionMessage("029", "Cannot create marshaller"), cause);
  }

  public SpinXmlDataFormatException unableToCreateContext(Throwable cause) {
    return new SpinXmlDataFormatException(exceptionMessage("030", "Cannot create context"), cause);
  }

  public SpinXmlDataFormatException unableToCreateUnmarshaller(Throwable cause) {
    return new SpinXmlDataFormatException(exceptionMessage("031", "Cannot create unmarshaller"), cause);
  }

  public SpinXmlDataFormatException unableToSetEventHandler(String className, Throwable cause) {
    return new SpinXmlDataFormatException(exceptionMessage("032", "Cannot set event handler to '{}'", className), cause);
  }

  public SpinXmlDataFormatException unableToSetProperty(String propertyName, String className, Throwable cause) {
    return new SpinXmlDataFormatException(exceptionMessage("033", "Cannot set property '{}' to '{}'", propertyName, className), cause);
  }

  public SpinXPathException notAllowedXPathExpression(String expression) {
    return new SpinXPathException(exceptionMessage("034", "XPath expression '{}' not allowed", expression));
  }

  public SpinXPathException unableToFindXPathExpression(String expression) {
    return new SpinXPathException(exceptionMessage("035", "Unable to find XPath expression '{}'", expression));
  }

  public SpinXmlElementException elementIsNotChildOfThisElement(SpinXmlElement existingChildElement, SpinXmlElement parentDomElement) {
    return new SpinXmlElementException(exceptionMessage("036", "The element with namespace '{}' and name '{}' " +
        "is not a child element of the element with namespace '{}' and name '{}'",
      existingChildElement.namespace(), existingChildElement.name(),
      parentDomElement.namespace(), parentDomElement.name()
    ));
  }

}
