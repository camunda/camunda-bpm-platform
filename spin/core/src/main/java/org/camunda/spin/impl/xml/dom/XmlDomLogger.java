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
import org.camunda.spin.spi.SpinXmlDataFormatException;
import org.camunda.spin.xml.tree.*;

import javax.xml.xpath.XPathExpressionException;
import java.util.NoSuchElementException;

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

  public SpinXmlTreeAttributeException unableToSetAttributeValueToNull(String namespace, String attributeName) {
    return new SpinXmlTreeAttributeException(exceptionMessage("010", "Unable to set value of the attribute '{}:{}' to 'null'", namespace, attributeName));
  }

  public SpinXmlTreeElementException unableToAdoptElement(SpinXmlTreeElement elementToAdopt) {
    return new SpinXmlTreeElementException(exceptionMessage("011", "Unable to adopt element '{}:{}'", elementToAdopt.namespace(), elementToAdopt.name()));
  }

  public UnsupportedOperationException methodNotSupportedByClass(String methodName, Class<?> implementationClass) {
    return new UnsupportedOperationException(exceptionMessage("012", "The method '{}' is not implemented by class '{}'", methodName, implementationClass.getName()));
  }

  public NoSuchElementException iteratorHasNoMoreElements(Class<?> iteratorClass) {
    return new NoSuchElementException(exceptionMessage("013", "The iterator '{}' has no more elements", iteratorClass.getName()));
  }

  public SpinXmlTreeElementException elementHasNoParent(SpinXmlTreeElement element) {
    return new SpinXmlTreeElementException(exceptionMessage("014", "The element '{}:{}' has no parent element.", element.namespace(), element.name()));
  }

  public SpinXmlTreeElementImplementationException unableToReplaceElementInImplementation(SpinXmlTreeElement existingElement, SpinXmlTreeElement newElement, Exception cause) {
    return new SpinXmlTreeElementImplementationException(exceptionMessage("015", "Unable to replace the existing element '{}:{}' by the new element '{}:{}' in the underlying implementation",
      existingElement.namespace(), existingElement.name(), newElement.namespace(), newElement.name()), cause);
  }

  public SpinXmlTreeElementImplementationException unableToSetAttributeInImplementation(SpinXmlTreeElement element, String namespace, String attributeName, String value, Exception cause) {
    return new SpinXmlTreeElementImplementationException(exceptionMessage("016", "Unable to set attribute '{}:{}' to value '{}' on element '{}:{}' in the underlying implementation",
      namespace, attributeName, value, element.namespace(), element.name()), cause);
  }

  public SpinXmlTreeElementImplementationException unableToAppendElementInImplementation(SpinXmlTreeElement element, SpinXmlTreeElement childElement, Exception cause) {
    return new SpinXmlTreeElementImplementationException(exceptionMessage("017", "Unable to append new child element '{}:{}' to element '{}:{}' in the underlying implementation",
      childElement.namespace(), childElement.name(), element.namespace(), element.name()), cause);
  }

  public SpinXmlTreeElementImplementationException unableToInsertElementInImplementation(SpinXmlTreeElement element, SpinXmlTreeElement childElement, Exception cause) {
    return new SpinXmlTreeElementImplementationException(exceptionMessage("018", "Unable to insert new child element '{}:{}' to element '{}:{}' in the underlying implementation",
      childElement.namespace(), childElement.name(), element.namespace(), element.name()), cause);
  }

  public SpinXmlTreeElementImplementationException unableToRemoveChildInImplementation(SpinXmlTreeElement element, SpinXmlTreeElement childElement, Exception cause) {
    return new SpinXmlTreeElementImplementationException(exceptionMessage("019", "Unable to remove child element '{}:{}' from element '{}:{}' in the underlying implementation",
      childElement.namespace(), childElement.name(), element.namespace(), element.name()), cause);
  }

  public SpinXmlTreeAttributeException unableToWriteAttribute(SpinXmlTreeAttribute attribute, Exception cause) {
    return new SpinXmlTreeAttributeException(exceptionMessage("020", "Unable to write attribute '{}:{}'", attribute.namespace(), attribute.name()), cause);
  }

  public SpinXmlTreeElementException unableToCreateTransformer(Exception cause) {
    return new SpinXmlTreeElementException(exceptionMessage("021", "Unable to create a transformer to write element"), cause);
  }

  public SpinXmlTreeElementException unableToTransformElement(SpinXmlTreeElement element, Exception cause) {
    return new SpinXmlTreeElementException(exceptionMessage("022", "Unable to transform element '{}:{}'", element.namespace(), element.name()), cause);
  }

  public SpinXmlTreeXPathException unableToCompileXPathQuery(String expression, XPathExpressionException cause) {
    return new SpinXmlTreeXPathException(exceptionMessage("023", "Unable to compile xPath query '{}'", expression), cause);
  }

  public SpinXmlTreeXPathException unableToEvaluateXPathExpressionOnElement(SpinXmlTreeElement element, Exception cause) {
    return new SpinXmlTreeXPathException(exceptionMessage("024", "Unable to evaluate XPath expression on element '{}:{}'", element.namespace(), element.name()), cause);
  }

  public SpinXmlTreeXPathException unableToCastXPathResultTo(Class<?> castClass, Exception cause) {
    return new SpinXmlTreeXPathException(exceptionMessage("025", "Unable to cast XPath expression to class '{}'", castClass.getName()), cause);
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
}
