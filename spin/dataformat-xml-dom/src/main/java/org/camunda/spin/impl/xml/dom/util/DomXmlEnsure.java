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
package org.camunda.spin.impl.xml.dom.util;

import org.camunda.commons.utils.EnsureUtil;
import org.camunda.spin.impl.xml.dom.DomXmlElement;
import org.camunda.spin.impl.xml.dom.DomXmlLogger;
import org.camunda.spin.xml.SpinXPathException;
import org.camunda.spin.xml.SpinXmlElementException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A list of generally useful source code assertions provided as static helpers.
 *
 * @author Daniel Meyer
 *
 */
public class DomXmlEnsure extends EnsureUtil {

  private static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  private static final String ROOT_EXPRESSION = "/";

  /**
   * Ensures that the element is child element of the parent element.
   *
   * @param parentElement the parent xml dom element
   * @param childElement the child element
   * @throws SpinXmlElementException if the element is not child of the parent element
   */
  public static void ensureChildElement(DomXmlElement parentElement, DomXmlElement childElement) {
    Node parent = childElement.unwrap().getParentNode();
    if (parent == null || !parentElement.unwrap().isEqualNode(parent)) {
      throw LOG.elementIsNotChildOfThisElement(childElement, parentElement);
    }
  }

  /**
   * Ensures that the expression is not the root expression '/'.
   *
   * @param expression the expression to ensure to be not the root expression '/'
   * @throws SpinXPathException if the expression is the root expression '/'
   */
  public static void ensureNotDocumentRootExpression(String expression) {
    if (ROOT_EXPRESSION.equals(expression)) {
      throw LOG.notAllowedXPathExpression(expression);
    }
  }

  /**
   * Ensure that the node is not null.
   *
   * @param node the node to ensure to be not null
   * @param expression the expression was used to find the node
   * @throws SpinXPathException if the node is null
   */
  public static void ensureXPathNotNull(Node node, String expression) {
    if (node == null) {
      throw LOG.unableToFindXPathExpression(expression);
    }
  }

  /**
   * Ensure that the nodeList is either null or empty.
   *
   * @param nodeList the nodeList to ensure to be either null or empty
   * @param expression the expression was used to fine the nodeList
   * @throws SpinXPathException if the nodeList is either null or empty
   */
  public static void ensureXPathNotEmpty(NodeList nodeList, String expression) {
    if (nodeList == null || nodeList.getLength() == 0) {
      throw LOG.unableToFindXPathExpression(expression);
    }
  }

}
