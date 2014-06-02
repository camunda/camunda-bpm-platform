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
package org.camunda.spin.impl.util;

import org.camunda.spin.impl.xml.dom.SpinXmlDomElement;
import org.camunda.spin.logging.SpinCoreLogger;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.xml.tree.SpinXmlTreeElementException;
import org.w3c.dom.Node;


/**
 * A list of generally useful source code assertions provided as static helpers.
 *
 * @author Daniel Meyer
 *
 */
public class SpinEnsure {

  private final static SpinCoreLogger LOG = SpinLogger.CORE_LOGGER;

  /**
   * Ensures that the parameter is not null.
   *
   * @param parameterName the parameter name
   * @param value the value to ensure to be not null
   * @throws IllegalArgumentException if the parameter value is null
   */
  public static void ensureNotNull(String parameterName, Object value) {
    if(value == null) {
      throw LOG.parameterIsNullException(parameterName);
    }
  }

  /**
   * Ensures that the element is child element of the parent element.
   *
   * @param parentElement the parent xml dom element
   * @param childElement the child element
   * @throws SpinXmlTreeElementException if the element is not child of the parent element
   */
  public static void ensureChildElement(SpinXmlDomElement parentElement, SpinXmlDomElement childElement) {
    Node parent = childElement.unwrap().getParentNode();
    if (parent == null || !parentElement.unwrap().isEqualNode(parent)) {
      throw LOG.elementIsNotChildOfThisElement(childElement, parentElement);
    }

  }


  /**
   * Ensure the object is of a given type and return the casted object
   *
   * @param objectName the name of the parameter
   * @param object the parameter value
   * @param type the expected type
   * @return the parameter casted to the requested type
   * @throws IllegalArgumentException in case object cannot be casted to type
   */
  @SuppressWarnings("unchecked")
  public static <T> T ensureParamInstanceOf(String objectName, Object object, Class<T> type) {
    if(type.isAssignableFrom(object.getClass())) {
      return (T) object;

    } else {
      throw LOG.unsupportedParameterType(objectName, object, type);

    }
  }
}
