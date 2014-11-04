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
import org.camunda.spin.impl.logging.SpinCoreLogger;
import org.camunda.spin.impl.logging.SpinLogger;
import org.camunda.spin.impl.xml.dom.DomXmlElement;
import org.camunda.spin.xml.SpinXmlElementException;
import org.w3c.dom.Node;


/**
 * A list of generally useful source code assertions provided as static helpers.
 *
 * @author Daniel Meyer
 *
 */
public class DomXmlEnsure extends EnsureUtil {

  private static final SpinCoreLogger LOG = SpinLogger.CORE_LOGGER;

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
}
